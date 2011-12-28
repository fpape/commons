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
        collectionWrapper.millisToRemainInCollection = 10000

        // will expire after 10 sec
        collectionWrapper.add(1)
        collectionWrapper.add(2)
        collectionWrapper.add(3)
        collectionWrapper.add(4)
        collectionWrapper.add(5)

        DateTimeUtils.setCurrentMillisFixed(now().plusSeconds(8).toDate().time)

        // will still be there after 13 sec
        collectionWrapper.add(6)
        collectionWrapper.add(7)
        collectionWrapper.add(8)
        collectionWrapper.add(9)
        collectionWrapper.add(10)

        assert collectionWrapper.size() == 10

        when:
        def found = true
        def firstSize = 0
        (1..10).each {
            found &= collectionWrapper.contains(it)
            firstSize = collectionWrapper.size()
        }

        DateTimeUtils.setCurrentMillisFixed(now().plusSeconds(3).toDate().time)

        def notFound = false
        def secondSize = 0
        (6..10).each {
            found &= collectionWrapper.contains(it)
        }
        (1..5).each {
            found |= collectionWrapper.contains(it)
            secondSize = collectionWrapper.size()
        }

        then:
        found
        firstSize == 10

        !notFound
        secondSize == 5
    }
}
