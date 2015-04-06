package org.uci.lids.graph;

/**
 * Project: 3/18/15
 * Package: ${PACKAGE_NAME}
 * Created by Hamid Mirzaei on 3/18/15.
 */
@SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
public class Edge<E> {
    private E node1;
    private E node2;

    public Edge(E node1, E node2) {
        this.node1 = node1;
        this.node2 = node2;
    }

    public E getNode1() {
        return node1;
    }

    public E getNode2() {
        return node2;
    }

    @Override
    public boolean equals(Object obj) {
        return node1.equals(((Edge<E>) obj).getNode1()) && node2.equals(((Edge<E>) obj).getNode2());
    }

    @Override
    public int hashCode() {
        int hash = 17;
        hash = hash * 31 + node1.hashCode();
        hash = hash * 31 + node2.hashCode();
        return hash;
    }

    @Override
    public String toString() {
        return node1.toString() + "->" + node2.toString();
    }
}
