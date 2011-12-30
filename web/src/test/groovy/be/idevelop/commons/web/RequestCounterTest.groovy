package be.idevelop.commons.web

import org.springframework.mock.web.MockHttpServletRequest
import spock.lang.Specification
import static be.idevelop.commons.web.RequestCounter.COUNTER

/**
 * Test for {@link RequestCounter}.
 *
 * @author steven
 */
class RequestCounterTest extends Specification {

    def "test increment"() {
        given:
        def request = new MockHttpServletRequest()
        request.remoteAddr = '192.168.0.1'

        when:
        COUNTER.increment(request)
        COUNTER.increment(request)
        COUNTER.increment(request)
        COUNTER.increment(request)
        def count = COUNTER.increment(request)

        then:
        count == 5
    }

    def "test reset"() {
        given:
        def request = new MockHttpServletRequest()
        request.remoteAddr = '192.168.0.1'

        when:
        COUNTER.increment(request)
        COUNTER.increment(request)
        COUNTER.increment(request)
        COUNTER.increment(request)
        COUNTER.increment(request)
        def count = COUNTER.reset(request)

        then:
        count == 0
    }
}
