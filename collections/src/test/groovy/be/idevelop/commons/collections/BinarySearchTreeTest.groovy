package be.idevelop.commons.collections

import spock.lang.Specification

/**
 * Test for {@link BinarySearchTree}
 *
 * @author steven
 */
class BinarySearchTreeTest extends Specification {

    def "test basic use"() {
        given:
        def tree = new BinarySearchTree<Integer>()

        when:
        tree.add 6
        tree.add 7
        tree.add 2
        tree.add 4
        tree.add 1
        tree.add 8

        then:
        tree.contains 8
        tree.contains 1
        !tree.contains(5)
    }

    def "test iterator iterates in descending order"() {
        given:
        def tree = new BinarySearchTree<Integer>()

        tree.add 7
        tree.add 2
        tree.add 4
        tree.add 1
        tree.add 12
        tree.add 8
        tree.add 11
        tree.add 6

        when:
        Iterator<Integer> iterator = tree.iterator()
        Integer prev = 0
        def current

        def result = true
        while (iterator.hasNext()) {
            current = iterator.next()
            result &= (!prev || current > prev)

            prev = current
        }

        then:
        result
    }
}
