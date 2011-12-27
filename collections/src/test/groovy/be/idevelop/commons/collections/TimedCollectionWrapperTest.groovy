package be.idevelop.commons.collections

import org.joda.time.DateTimeUtils
import spock.lang.Specification
import static org.joda.time.DateTimeUtils.setCurrentMillisSystem
import static org.joda.time.LocalDateTime.now

/**
 * Test for {@link TimedCollectionWrapper}.
 *
 * @author steven
 */
class TimedCollectionWrapperTest extends Specification {


    def setupSpec() {

    }

    def cleanupSpec() {
        setCurrentMillisSystem()
    }

    def "test timeout not reached"() {
        given:
        def collectionWrapper = new TimedCollectionWrapper<Integer>()

        collectionWrapper.add(1)
        collectionWrapper.add(2)
        collectionWrapper.add(3)
        collectionWrapper.add(4)
        collectionWrapper.add(5)
        collectionWrapper.add(6)
        collectionWrapper.add(7)
        collectionWrapper.add(8)
        collectionWrapper.add(9)
        collectionWrapper.add(10)

        when:
        def result = true
        (1..10).each {
            result &= collectionWrapper.contains(it)
        }

        then:
        result
    }

    def "test timeout reached"() {
        given:
        def collectionWrapper = new TimedCollectionWrapper<Integer>()

        collectionWrapper.add(1)
        collectionWrapper.add(2)
        collectionWrapper.add(3)
        collectionWrapper.add(4)
        collectionWrapper.add(5)
        collectionWrapper.add(6)
        collectionWrapper.add(7)
        collectionWrapper.add(8)
        collectionWrapper.add(9)
        collectionWrapper.add(10)

        assert collectionWrapper.size() == 10

        // move time one hour forward -> no need to sleep in test
        DateTimeUtils.setCurrentMillisFixed(now().plusHours(1).toDate().time)

        when:
        def result = false

        (1..10).each {
            result |= collectionWrapper.contains(it)
        }

        then:
        !result
    }
}
