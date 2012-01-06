package be.idevelop.commons.web

import be.idevelop.commons.collections.TimedCollectionWrapper
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletResponse
import org.joda.time.DateTimeUtils
import org.joda.time.Duration
import org.springframework.mock.web.MockFilterConfig
import org.springframework.mock.web.MockHttpServletRequest
import spock.lang.Specification
import spock.lang.Unroll
import static org.apache.commons.lang.RandomStringUtils.randomNumeric
import static org.joda.time.LocalDateTime.now
import static org.springframework.test.util.ReflectionTestUtils.getField

/**
 * Test for {@link AggressiveUserFilter}.
 *
 * @author steven
 */
class AggressiveUserFilterTest extends Specification {

    def cleanup() {
        DateTimeUtils.setCurrentMillisSystem()
    }

    def "test init config uses default"() {
        given:
        AggressiveUserFilter filter = new AggressiveUserFilter()

        when:
        filter.init(new MockFilterConfig('AggressiveUserFilter'))

        then:
        2000 == getField(filter, 'millisBetweenCalls')
        10 == getField(filter, 'maxTooFastRequests')
        '/*' == getField(filter, 'urlPattern')
        '' == getField(filter, 'excludeUrlPattern')
    }

    def "test init config uses FilterConfig when specified"() {
        given:
        AggressiveUserFilter filter = new AggressiveUserFilter()
        MockFilterConfig filterConfig = new MockFilterConfig('AggressiveUserFilter')
        filterConfig.addInitParameter(AggressiveUserFilter.MILLIS_BETWEEN_CALLS, String.valueOf(3000))
        filterConfig.addInitParameter(AggressiveUserFilter.MAX_TOO_FAST_REQUESTS, String.valueOf(20))
        filterConfig.addInitParameter(AggressiveUserFilter.URL_PATTERN, '*.html')
        filterConfig.addInitParameter(AggressiveUserFilter.EXCLUDE_URL_PATTERN, '/admin/*, *.xhtml')

        when:
        filter.init(filterConfig);

        then:
        3000 == getField(filter, 'millisBetweenCalls')
        20 == getField(filter, 'maxTooFastRequests')
        '*.html' == getField(filter, 'urlPattern')
        '/admin/*, *.xhtml' == getField(filter, 'excludeUrlPattern')
    }

    def "test init config uses default when specified with invalid Integer"() {
        given:
        AggressiveUserFilter filter = new AggressiveUserFilter()
        MockFilterConfig filterConfig = new MockFilterConfig('AggressiveUserFilter')
        filterConfig.addInitParameter(AggressiveUserFilter.MILLIS_BETWEEN_CALLS, 'sads')
        filterConfig.addInitParameter(AggressiveUserFilter.MAX_TOO_FAST_REQUESTS, 'asda')

        when:
        filter.init(filterConfig);

        then:
        2000 == getField(filter, 'millisBetweenCalls')
        10 == getField(filter, 'maxTooFastRequests')
    }

    @SuppressWarnings(["GroovyPointlessArithmetic", "GroovyAssignabilityCheck"])
    def "doing calls every 3 seconds handles request nicely"() {
        given:
        AggressiveUserFilter filter = new AggressiveUserFilter()
        filter.init(new MockFilterConfig('AggressiveUserFilter'))
        def response = Mock(HttpServletResponse)
        def chain = Mock(FilterChain)

        when:
        (1..10).each {
            filter.doFilter(generateServletHttpRequest(), response, chain)
            DateTimeUtils.setCurrentMillisFixed(now().plusSeconds(3).toDate().time)
        }

        then:
        10 * chain.doFilter(_, _)
        0 * response.sendError(429, _)
    }

    @SuppressWarnings(["GroovyPointlessArithmetic", "GroovyAssignabilityCheck"])
    def "doing calls every 0,2 seconds returns http error code 429 on the 10th requests"() {
        given:
        AggressiveUserFilter filter = new AggressiveUserFilter()
        filter.init(new MockFilterConfig('AggressiveUserFilter'))
        def response = Mock(HttpServletResponse)
        def chain = Mock(FilterChain)
        def request = generateServletHttpRequest()

        when:
        (1..21).each {
            filter.doFilter(request, response, chain)
            if (it % 10 == 0) {
                DateTimeUtils.currentMillisFixed = now().plusSeconds(32).toDate().time
            }
        }

        then:
        12 * chain.doFilter(_, _)
        9 * response.sendError(429, _)
    }

    @SuppressWarnings("GroovyAssignabilityCheck")
    def "test speed of 5000 calls"() {
        given:
        AggressiveUserFilter filter = new AggressiveUserFilter()
        filter.init(new MockFilterConfig('AggressiveUserFilter'))
        def response = Mock(HttpServletResponse)
        def chain = Mock(FilterChain)
        def request = generateServletHttpRequest()

        when:
        Date start = new Date()
        (1..5000).each {
            if (it % 5 == 0) {
                filter.doFilter(request, response, chain)
            } else {
                filter.doFilter(generateServletHttpRequest(), response, chain)
            }
            if (it % 200 == 0) {
                DateTimeUtils.currentMillisFixed = now().plusSeconds(32).toDate().time
            }
        }
        Date end = new Date()
        def duration = new Duration(start.time, end.time)

        then:
        duration.millis < 3000
    }

    def "destroy clears cache"() {
        given:
        AggressiveUserFilter filter = new AggressiveUserFilter()
        filter.init(new MockFilterConfig('AggressiveUserFilter'))
        def response = Mock(HttpServletResponse)
        def chain = Mock(FilterChain)
        def request = generateServletHttpRequest()

        (1..30).each {
            filter.doFilter(request, response, chain)
        }

        when:
        filter.destroy()

        then:
        ((TimedCollectionWrapper) getField(TimedRequestCache.CACHE, 'timedCache')).isEmpty()
        ((TimedCollectionWrapper) getField(PunishedCache.JAIL, 'timedCache')).isEmpty()
        ((Map) getField(RequestCounter.COUNTER, 'counterMap')).isEmpty()
    }

    @Unroll({"Test filter path for $path -> should filter: $expected"})
    def "test filter paths"() {
        given:
        AggressiveUserFilter filter = new AggressiveUserFilter()
        def config = new MockFilterConfig('AggressiveUserFilter')
        config.addInitParameter(AggressiveUserFilter.URL_PATTERN, '/, /**/*.html')
        config.addInitParameter(AggressiveUserFilter.EXCLUDE_URL_PATTERN, '/admin/**, /**/*.xhtml')
        filter.init(config)

        when:
        def result = filter.shouldFilter(path)

        then:
        result == expected

        where:
        path                     | expected
        '/'                      | true
        '/index.html'            | true
        '/folder/index.html'     | true
        '/folder/map/index.html' | true
        '/index.xhtml'           | false
        '/admin/index.html'      | false
        '/admin/index.xhtml'     | false
        '/admin/map/index.html'  | false
    }

    private MockHttpServletRequest generateServletHttpRequest() {
        def request = new MockHttpServletRequest()
        request.remoteAddr = randomNumeric(9)
        request.servletPath = '/'
        return request
    }
}
