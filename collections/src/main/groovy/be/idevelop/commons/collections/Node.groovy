package be.idevelop.commons.collections

/**
 * Binary Tree node. Used in {@link BinarySearchTree}.
 *
 * @author steven
 */
private class Node<T extends Comparable> {

    Node<T> left
    Node<T> right
    T data

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