package be.idevelop.commons.web

import org.apache.commons.lang.RandomStringUtils
import org.joda.time.DateTimeUtils
import org.springframework.mock.web.MockHttpServletRequest
import spock.lang.Specification
import spock.lang.Stepwise
import static be.idevelop.commons.web.TimedRequestCache.CACHE
import static org.joda.time.LocalDateTime.now
import static org.springframework.test.util.ReflectionTestUtils.setField

/**
 * Test for {@link TimedRequestCache}.
 *
 * @author steven
 */
@Stepwise
class TimedRequestCacheTest extends Specification {

    def setup() {
        setField(CACHE, 'initialized', false)
    }

    def cleanup() {
        DateTimeUtils.setCurrentMillisSystem()
    }

    def "test contains when not initialized throws IllegalStateExceptions"() {
        when:
        CACHE.contains(getRequest())

        then:
        thrown IllegalStateException
    }

    def "test put when not initialized throws IllegalStateException"() {
        when:
        CACHE.put(getRequest())

        then:
        thrown IllegalStateException
    }

    def "test add and contains before timeout"() {
        given:
        CACHE.init(2000)

        def request = getRequest()
        CACHE.put(request)

        when:
        def result = CACHE.contains(request)

        then:
        result
    }

    def "test add and contains after timeout"() {
        given:
        CACHE.init(1000)

        def request = getRequest()
        CACHE.put(request)
        DateTimeUtils.setCurrentMillisFixed(now().plusHours(1).toDate().time)

        when:
        def result = CACHE.contains(request)

        then:
        !result
    }

    def "test clear empties cache"() {
        given:
        CACHE.init(2000)

        def request = getRequest()
        CACHE.put(request)
        CACHE.clear()

        when:
        def result = CACHE.contains(request)

        then:
        !result
    }

    def getRequest() {
        def request = new MockHttpServletRequest()
        request.remoteAddr = RandomStringUtils.randomNumeric(10)

        return request
    }

}
