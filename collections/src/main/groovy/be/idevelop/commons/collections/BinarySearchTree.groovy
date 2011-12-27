package be.idevelop.commons.collections

/**
 * Binary Search Tree.
 *
 * Ordering is based on implementation of Comparable interface.
 *
 * @author steven
 */
class BinarySearchTree<T extends Comparable> implements Collection<T> {

    private Node<T> root
    private Integer modCount

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

    private static class Node<T extends Comparable> {

        private Node<T> left
        private Node<T> right
        private T data

        Node(T data) {
            assert data

            this.data = data
            left = null
            right = null
        }


        Node<T> find(T data) {
            return (this.data == data) ? this :
                ((this.left && data < this.data) ? this.left.find(data) :
                    ((this.right && data > this.data) ? this.right.find(data) : null))
        }

        boolean add(T data) {
            if (data < this.data) {
                if (left) {
                    left.add(data)
                } else {
                    left = new Node(data)
                }
                return true
            } else if (data > this.data) {
                if (right) {
                    right.add(data)
                } else {
                    right = new Node(data)
                }
                return true
            } else {
                return false
            }
        }

        boolean remove(Object o, Node parent) {
            if (data < this.data) {
                return left ? left.remove(data, this) : false
            } else if (data > this.data) {
                return right ? right.remove(data, this) : false
            } else {
                if (left && right) {
                    this.data = right.minValue()
                    right.remove(this.data, this)
                } else if (parent.left == this) {
                    parent.left = left ?: right
                } else if (parent.right == this) {
                    parent.right = left ?: right
                }
                return true;
            }
        }

        T minValue() {
            return left ? left.minValue() : data;
        }

        Integer size() {
            return 1 + (left ? left.size() : 0) + (right ? right.size() : 0)
        }

        Node<T> findLeftMost() {
            return left ? left.findLeftMost() : this;
        }

    }

    private static class TreeIterator<T extends Comparable<T>> implements Iterator<T> {

        private Integer expectedModCount

        private Stack<BinarySearchTree.Node<T>> toVisit

        private BinarySearchTree tree;

        TreeIterator(BinarySearchTree tree) {
            this.tree = tree
            this.expectedModCount = tree.modCount

            toVisit = new Stack<BinarySearchTree.Node<T>>()
            if (tree.root) {
                pushLeftNodes tree.root
            }
        }

        @Override
        boolean hasNext() {
            !toVisit.empty()
        }


        @Override
        T next() {
            checkForModification()

            BinarySearchTree.Node<T> node = toVisit.pop()
            pushLeftNodes(node.right)
            return node.data
        }


        private def pushLeftNodes(Node<T> node) {
            if (node) {
                this.toVisit.push(node)
                pushLeftNodes node.left
            }
        }

        @Override
        void remove() {
            throw new java.lang.UnsupportedOperationException("remove")
        }

        private void checkForModification() {
            if (expectedModCount != tree.modCount) {
                throw new ConcurrentModificationException()
            }
        }
    }
}
