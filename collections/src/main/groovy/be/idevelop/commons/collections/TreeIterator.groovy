package be.idevelop.commons.collections

/**
 * Tree iterator class.
 *
 * @author steven
 */
class TreeIterator<T extends Comparable<T>> implements Iterator<T> {

    private Integer expectedModCount

    private Stack<Node<T>> toVisit

    private BinarySearchTree tree;

    TreeIterator(BinarySearchTree tree) {
        this.tree = tree
        this.expectedModCount = tree.modCount

        toVisit = new Stack<Node<T>>()
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

        Node<T> node = toVisit.pop()
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