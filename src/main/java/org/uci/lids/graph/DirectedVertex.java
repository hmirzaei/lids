package org.uci.lids.graph;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Created by hamid on 3/16/15.
 */
public class DirectedVertex<E> extends AbstractVertex<E, DirectedVertex<E>> {

    private Set<E> parents = new LinkedHashSet<E>();
    private Set<E> children = new LinkedHashSet<E>();


    public DirectedVertex(E e) {
        super(e);
    }

    @Override
    public int getDegree() {
        return children.size() + parents.size();
    }

    public Set<E> getParents() {
        return parents;
    }

    public Set<E> getChildren() {
        return children;
    }

    @Override
    public Set<E> getAdjacents() {
        Set<E> adjacents = new LinkedHashSet<E>();
        adjacents.addAll(parents);
        adjacents.addAll(children);
        return adjacents;
    }

    @Override
    public void addLinkTo(DirectedVertex<E> vertex) {
        this.children.add(vertex.getContent());
        vertex.parents.add(this.getContent());
    }

    @Override
    public void removeLinkTo(DirectedVertex<E> vertex) {
        this.children.remove(vertex.getContent());
        vertex.parents.remove(this.getContent());
    }


    @Override
    public String toString() {
        return "{" + this.content.toString()
                + ";P:" + Arrays.toString(this.parents.toArray())
                + ";C:" + Arrays.toString(this.children.toArray())
                + "}";
    }


}
