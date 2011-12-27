package be.idevelop.commons.collections

import org.joda.time.LocalDateTime

/**
 * Timed entry for {@link TimedCollectionWrapper}.
 *
 * @author steven
 */
class TimedEntry {

    LocalDateTime time
    def value

    TimedEntry(LocalDateTime time, value) {
        this.time = time
        this.value = value
    }

}
