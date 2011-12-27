package be.idevelop.commons.collections

import java.util.concurrent.ConcurrentLinkedQueue
import org.joda.time.LocalDateTime
import static org.joda.time.LocalDateTime.now

/**
 * Wrapper that removes elements
 *
 * @author steven
 */
class TimedCollectionWrapper<E> implements Collection<E> {

    private Collection<E> collection
    private Queue<TimedEntry> queue

    private static final DEFAULT_MILLIS_TO_REMAIN_IN_COLLECTION = 60000

    private Integer millisToRemainInCollection

    TimedCollectionWrapper() {
        this.collection = []

        this.queue = new ConcurrentLinkedQueue<TimedEntry>()
    }

    TimedCollectionWrapper(Collection<E> collection) {
        assert collection

        this.collection = collection
        this.queue = new ConcurrentLinkedQueue<TimedEntry>()
    }

    @Override
    int size() {
        removeTooOldElements()
        return collection.size()
    }

    @Override
    boolean isEmpty() {
        removeTooOldElements()
        return collection.isEmpty()
    }

    @Override
    boolean contains(Object o) {
        removeTooOldElements()
        return collection.contains(o)
    }

    @Override
    Iterator<E> iterator() {
        removeTooOldElements()
        return collection.iterator()
    }

    @Override
    Object[] toArray() {
        removeTooOldElements()
        return collection.toArray()
    }

    @Override
    def <T> T[] toArray(T[] ts) {
        removeTooOldElements()
        return collection.toArray(ts)
    }

    @Override
    boolean add(E e) {
        removeTooOldElements()
        return addElement(e)
    }

    @Override
    boolean remove(Object o) {
        removeTooOldElements()
        return collection.remove(o)
    }

    @Override
    boolean containsAll(Collection<?> objects) {
        removeTooOldElements()
        return collection.containsAll(objects)
    }

    @Override
    boolean addAll(Collection<? extends E> es) {
        removeTooOldElements()
        return addAllElements(es)
    }

    @Override
    boolean removeAll(Collection<?> objects) {
        removeTooOldElements()
        return collection.removeAll(objects)
    }

    @Override
    boolean retainAll(Collection<?> objects) {
        removeTooOldElements()
        return collection.retainAll(objects)
    }

    @Override
    void clear() {
        queue.clear()
        collection.clear()
    }

    private def addElement(E e) {
        def time = now()
        insertTimedElement(time, e)
        return this.collection.add(e)
    }

    private def addAllElements(Collection<? extends E> es) {
        def time = now()
        es.each {e ->
            insertTimedElement(time, e)
        }
        return this.collection.addAll(es)
    }

    private insertTimedElement(LocalDateTime time, E e) {
        this.queue.add(new TimedEntry(time, e))
    }

    private def removeTooOldElements() {
        def limit = now().minusMillis(getMillisToRemainInCollection())

        def entry = queue.peek()
        def toRemove = []
        while (entry?.time?.isBefore(limit)) {
            toRemove.add(queue.poll().value)

            entry = queue.peek()
        }
        this.collection.removeAll(toRemove)
    }

    Integer getMillisToRemainInCollection() {
        if (millisToRemainInCollection == null) {
            millisToRemainInCollection = DEFAULT_MILLIS_TO_REMAIN_IN_COLLECTION
        }
        return millisToRemainInCollection
    }

    void setMillisToRemainInCollection(Integer millisToRemainInCollection) {
        this.millisToRemainInCollection = millisToRemainInCollection
    }

}
