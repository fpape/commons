package be.idevelop.commons.web

import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpSession
import org.springframework.mock.web.MockServletContext
import spock.lang.Specification
import static RequestKey.getKEY
import static org.springframework.test.util.ReflectionTestUtils.getField
import static org.springframework.test.util.ReflectionTestUtils.setField

/**
 * Test for {@link RequestKey}.
 *
 * @author steven
 */
class RequestKeyTest extends Specification {

    def "test constructor"() {
        given:
        def request = new MockHttpServletRequest()
        request.remoteAddr = '192.168.0.1'
        request.addHeader('User-Agent', 'MSIE')
        request.session = new MockHttpSession(new MockServletContext(), 'sessionId1234')

        when:
        def key = new RequestKey(request)

        then:
        getField(key, 'internalValue') == '192.168.0.1#MSIE#sessionId1234'
        request.getAttribute(KEY) == '192.168.0.1#MSIE#sessionId1234'
    }

    def "test constructor with already defined key"() {
        given:
        def request = new MockHttpServletRequest()
        request.setAttribute(KEY, 'some key')

        when:
        def key = new RequestKey(request)

        then:
        getField(key, 'internalValue') == 'some key'
    }


    @SuppressWarnings("GroovyPointlessArithmetic")
    def "test compare delegate to string compare"() {
        given:
        def request = new MockHttpServletRequest()
        def internalValue = Mock(Comparable)

        def key = new RequestKey(request)
        setField(key, 'internalValue', internalValue)

        when:
        key.compareTo(key)

        then:
        1 * internalValue.compareTo(internalValue)
    }

    def "test equals()"() {
        given:
        def request = new MockHttpServletRequest()
        request.setAttribute(KEY, 'some key')
        def equalsRequest = new MockHttpServletRequest()
        equalsRequest.setAttribute(KEY, 'some key')
        def diffRequest = new MockHttpServletRequest()
        diffRequest.setAttribute(KEY, 'other key')

        def key = new RequestKey(request)

        expect:
        key == key
        key == new RequestKey(equalsRequest)
        key != new RequestKey(diffRequest)
        key != null
        key != 'some string'
    }

    def "test hashcode()"() {
        given:
        def request = new MockHttpServletRequest()
        request.setAttribute(KEY, 'some key')
        def equalsRequest = new MockHttpServletRequest()
        equalsRequest.setAttribute(KEY, 'some key')
        def diffRequest = new MockHttpServletRequest()
        diffRequest.setAttribute(KEY, 'other key')
        def key = new RequestKey(request)

        expect:
        key.hashCode() == key.hashCode()
        key.hashCode() == new RequestKey(equalsRequest).hashCode()
        key.hashCode() != new RequestKey(diffRequest).hashCode()
        key.hashCode() != 0
    }
}
