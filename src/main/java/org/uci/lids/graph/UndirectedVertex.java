package org.uci.lids.graph;


import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Hamid Mirzaei on 3/16/15.
 */
public class UndirectedVertex<E> extends AbstractVertex<E, UndirectedVertex<E>> {

    private Set<E> adjacents = new HashSet<E>();


    public UndirectedVertex(E e) {
        super(e);
    }

    @Override
    public int getDegree() {
        return adjacents.size();
    }

    public Set<E> getAdjacents() {
        return adjacents;
    }

    @Override
    public void addLinkTo(UndirectedVertex<E> vertex) {
        this.adjacents.add(vertex.getContent());
        vertex.adjacents.add(this.getContent());
    }

    @Override
    public void removeLinkTo(UndirectedVertex<E> vertex) {
        this.adjacents.remove(vertex.getContent());
        vertex.adjacents.remove(this.getContent());
    }


    @Override
    public String toString() {
        return "{" + this.content.toString()
                + ";A:" + Arrays.toString(this.adjacents.toArray())
                + "}";
    }


}
