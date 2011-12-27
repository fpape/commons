package be.idevelop.commons.collections

/**
 * Binary Search Tree.
 *
 * Ordering is based on implementation of Comparable interface.
 *
 * @author steven
 */
class BinarySearchTree<T extends Comparable> implements Collection<T> {

    Node<T> root
    Integer modCount

    BinarySearchTree() {
        root = null
        modCount = 0
    }

    boolean contains(T data) {
        data && root && root.find(data)
    }

    T find(T data) {
        if (data && root) {
            return root.find(data).data
        }
        return null
    }

    @Override
    int size() {
        return root ? root.size() : 0
    }

    @Override
    boolean isEmpty() {
        return !root
    }

    @Override
    boolean contains(Object o) {
        return o && o instanceof T && this.contains((T) o)
    }

    @Override
    Iterator<T> iterator() {
        return new TreeIterator<T>(this)
    }

    @Override
    Object[] toArray() {
        def result = new Object[this.size()]
        def iterator = this.iterator()
        def i = 0
        while (iterator.hasNext()) {
            result[iterator.next()]
            i++
        }
        return result
    }

    @Override
    def <T> T[] toArray(T[] ts) {
        if (!ts) {
            throw new NullPointerException()
        } else {
            def size = size()
            if (ts.length < size) {
                ts = new T[size]
            }
            def iterator = this.iterator()
            def i = 0
            while (iterator.hasNext()) {
                ts[iterator.next()]
                i++
            }
            for (; i < ts.length; i++) {
                ts[i] = null
            }
            return ts
        }
    }

    @Override
    boolean add(T data) {
        if (!root) {
            root = new Node<T>(data)
            modCount++
            return true
        } else {
            def changed = root.add(data)
            if (changed) {
                modCount++
            }
            return changed
        }
    }

    @Override
    boolean remove(Object o) {
        if (!root) {
            return false
        } else {
            if (root.data == o) {
                Node auxRoot = new Node(0)
                auxRoot.left = root
                boolean result = root.remove(o, auxRoot)
                root = auxRoot.getLeft()
                if (result) {
                    modCount++
                }
                return result
            } else {
                def changed = root.remove(o, null)
                if (changed) {
                    modCount++
                }
                return changed
            }
        }
    }

    @Override
    boolean containsAll(Collection<?> objects) {
        boolean result = false
        if (objects) {
            objects.each {
                if (!this.contains(it)) {
                    result = false
                    return false // break each
                }
            }
            result = true
        }
        return result
    }

    @Override
    boolean addAll(Collection<? extends T> es) {
        boolean result = false
        if (es) {
            es.each {
                result |= add(it)
            }
        }
        return result
    }

    @Override
    boolean removeAll(Collection<?> objects) {
        def result = false
        if (objects) {
            objects.each {
                result |= remove(it)
            }
        }
        return result
    }

    @Override
    boolean retainAll(Collection<?> objects) {
        this.clear()
        this.addAll(objects)
    }

    @Override
    void clear() {
        this.root = null
    }
}
