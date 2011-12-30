package be.idevelop.commons.web

import org.joda.time.DateTimeUtils
import org.springframework.mock.web.MockHttpServletRequest
import spock.lang.Specification
import static be.idevelop.commons.web.PunishedCache.JAIL
import static org.joda.time.LocalDateTime.now

/**
 * Test for {@link PunishedCache}.
 *
 * @author steven
 */
class PunishedCacheTest extends Specification {

    def cleanup() {
        DateTimeUtils.setCurrentMillisSystem()
        JAIL.clear()
    }

    def "test once punished stays punished for 30 seconds"() {
        given:
        def request = new MockHttpServletRequest()
        request.remoteAddr = '192.168.0.1'
        JAIL.punish(request)

        when:
        DateTimeUtils.currentMillisFixed = now().plusSeconds(29).toDate().time
        def result = JAIL.isPunished(request)

        then:
        result
    }

    def "test once punished is no longer punished after 30 seconds"() {
        given:
        def request = new MockHttpServletRequest()
        request.remoteAddr = '192.168.0.1'
        JAIL.punish(request)

        when:
        DateTimeUtils.currentMillisFixed = now().plusSeconds(31).toDate().time
        def result = JAIL.isPunished(request)

        then:
        !result
    }

    def "test once punished is no longer punished after 20 seconds"() {
        given:
        def request = new MockHttpServletRequest()
        request.remoteAddr = '192.168.0.1'
        JAIL.punishedTimeInMillis = 20000
        JAIL.punish(request)

        when:
        DateTimeUtils.currentMillisFixed = now().plusSeconds(21).toDate().time
        def result = JAIL.isPunished(request)

        then:
        !result
    }
}
