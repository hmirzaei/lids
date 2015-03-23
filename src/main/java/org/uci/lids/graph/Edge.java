package org.uci.lids.graph;

/**
 * Project: 3/18/15
 * Package: ${PACKAGE_NAME}
 * Created by Hamid Mirzaei on 3/18/15.
 */
public class Edge<E> {
    private AbstractVertex vertex1;
    private AbstractVertex vertex2;

    public Edge(AbstractVertex vertex1, AbstractVertex vertex2) {
        this.vertex1 = vertex1;
        this.vertex2 = vertex2;
    }

    public AbstractVertex getVertex1() {
        return vertex1;
    }

    public AbstractVertex getVertex2() {
        return vertex2;
    }

}
