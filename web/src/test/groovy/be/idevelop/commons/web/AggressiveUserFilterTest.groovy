package be.idevelop.commons.web

import javax.servlet.FilterChain
import javax.servlet.http.HttpServletResponse
import org.joda.time.DateTimeUtils
import org.joda.time.Duration
import org.joda.time.LocalDateTime
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
        1000 == getField(filter, 'millisBetweenCalls')
        '/*' == getField(filter, 'urlPattern')
    }

    def "test init config uses FilterConfig when specified"() {
        given:
        AggressiveUserFilter filter = new AggressiveUserFilter()
        MockFilterConfig filterConfig = new MockFilterConfig('AggressiveUserFilter')
        filterConfig.addInitParameter(AggressiveUserFilter.MILLIS_BETWEEN_CALLS, String.valueOf(3000))
        filterConfig.addInitParameter(AggressiveUserFilter.URL_PATTERN, '*.html, /**/*.jsp')

        when:
        filter.init(filterConfig);

        then:
        3000 == getField(filter, 'millisBetweenCalls')
        '*.html, /**/*.jsp' == getField(filter, 'urlPattern')
    }

    def "test init config uses default when specified with invalid Integer"() {
        given:
        AggressiveUserFilter filter = new AggressiveUserFilter()
        MockFilterConfig filterConfig = new MockFilterConfig('AggressiveUserFilter')
        filterConfig.addInitParameter(AggressiveUserFilter.MILLIS_BETWEEN_CALLS, 'sads')

        when:
        filter.init(filterConfig);

        then:
        1000 == getField(filter, 'millisBetweenCalls')
    }

    def "test init config throws exception when url pattern is invalid"() {
        given:
        AggressiveUserFilter filter = new AggressiveUserFilter()
        MockFilterConfig filterConfig = new MockFilterConfig('AggressiveUserFilter')
        filterConfig.addInitParameter(AggressiveUserFilter.URL_PATTERN, 'this is not a pattern, I think')

        when:
        filter.init(filterConfig);

        then:
        thrown IllegalArgumentException
    }

    @SuppressWarnings(["GroovyPointlessArithmetic", "GroovyAssignabilityCheck"])
    def "doing calls every 2 seconds handles request nicely"() {
        given:
        AggressiveUserFilter filter = new AggressiveUserFilter()
        filter.init(new MockFilterConfig('AggressiveUserFilter'))
        def response = Mock(HttpServletResponse)
        def chain = Mock(FilterChain)

        when:
        (1..10).each {
            filter.doFilter(generateServletHttpRequest(), response, chain)
            DateTimeUtils.setCurrentMillisFixed(now().plusSeconds(2).toDate().time)
        }

        then:
        10 * chain.doFilter(_, _)
        0 * response.sendError(429, _)
    }

    @SuppressWarnings(["GroovyPointlessArithmetic", "GroovyAssignabilityCheck"])
    def "doing calls every 0,2 seconds returns http error code 429"() {
        given:
        AggressiveUserFilter filter = new AggressiveUserFilter()
        filter.init(new MockFilterConfig('AggressiveUserFilter'))
        def response = Mock(HttpServletResponse)
        def chain = Mock(FilterChain)
        def request = generateServletHttpRequest()

        when:
        (1..10).each {
            filter.doFilter(request, response, chain)
        }

        then:
        1 * chain.doFilter(_, _)
        9 * response.sendError(429, _)
    }

    def "test speed of 5000 calls"() {
        given:
        AggressiveUserFilter filter = new AggressiveUserFilter()
        filter.init(new MockFilterConfig('AggressiveUserFilter'))
        def response = Mock(HttpServletResponse)
        def chain = Mock(FilterChain)
        def request = generateServletHttpRequest()

        when:
        LocalDateTime start = now()
        (1..5000).each {
            if (it % 5) {
                filter.doFilter(request, response, chain)
            } else {
                filter.doFilter(generateServletHttpRequest(), response, chain)
            }
        }
        LocalDateTime end = now()
        def duration = new Duration(start.toDate().time, end.toDate().time)

        then:
        duration.millis < 2000
        1001 * chain.doFilter(_, _)
        3999 * response.sendError(429, _)
    }

    @Unroll({"Url: $url, ContextRoot: $contextRoot, Pattern: $pattern, expected: $expected"})
    def "test url pattern matches"() {
        given:
        AggressiveUserFilter filter = new AggressiveUserFilter()
        def config = new MockFilterConfig('AggressiveUserFilter')
        config.addInitParameter(AggressiveUserFilter.URL_PATTERN, pattern)
        filter.init(config)

        when:
        def result = filter.matchesUrlPattern(url, contextRoot)

        then:
        result == expected

        where:
        url                          | contextRoot  | pattern      | expected
        '/altiplano/url.html'        | '/altiplano' | '/*'         | true
        '/altiplano/url.html'        | '/altiplano' | '/**/*.html' | true
        '/altiplano/url.jsp'         | '/altiplano' | '/**/*.jsp'  | true
        '/altiplano/path/url.jsp'    | '/altiplano' | '/**/*.jsp'  | true
        '/url.jsp'                   | '/'          | '/**/*.jsp'  | true
        '/path/url.jsp'              | '/'          | '/**/*.jsp'  | true
        '/path/url.jsp'              | '/'          | '/*.html'    | false
        '/path/url.html'             | '/'          | '/**/*.html' | true
        '/path/deep/deeper/url.html' | '/'          | '/**/*.html' | true
        '/url.html'                  | '/'          | '/**/*.html' | true
        '/'                          | '/'          | ''           | true
        ''                           | '/'          | ''           | true
    }

    private MockHttpServletRequest generateServletHttpRequest() {
        def request = new MockHttpServletRequest()
        request.remoteAddr = randomNumeric(9)
        request.requestURI = '/index.html'
        return request
    }
}
