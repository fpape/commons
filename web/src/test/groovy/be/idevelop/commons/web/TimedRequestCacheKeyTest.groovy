package be.idevelop.commons.web

import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpSession
import org.springframework.mock.web.MockServletContext
import spock.lang.Specification
import static org.springframework.test.util.ReflectionTestUtils.getField
import static org.springframework.test.util.ReflectionTestUtils.setField

/**
 * Test for {@link TimedRequestCacheKey}.
 *
 * @author steven
 */
class TimedRequestCacheKeyTest extends Specification {

    def "test constructor"() {
        given:
        def request = new MockHttpServletRequest()
        request.remoteAddr = '192.168.0.1'
        request.addHeader('User-Agent', 'MSIE')
        request.session = new MockHttpSession(new MockServletContext(), 'sessionId1234')

        when:
        def key = new TimedRequestCacheKey(request)

        then:
        getField(key, 'internalValue') == '192.168.0.1#MSIE#sessionId1234'
    }

    @SuppressWarnings("GroovyPointlessArithmetic")
    def "test compare delegate to string compare"() {
        given:
        def request = new MockHttpServletRequest()
        def internalValue = Mock(Comparable)

        def key = new TimedRequestCacheKey(request)
        setField(key, 'internalValue', internalValue)

        when:
        key.compareTo(key)

        then:
        1 * internalValue.compareTo(internalValue)
    }
}
