package org.uci.lids.graph;


/**
 * Created by hamid on 3/16/15.
 */
public abstract class AbstractVertex<E, V> {

    protected E content;


    public AbstractVertex(E e) {
        this.content = e;
    }

    public abstract void addLinkTo(V e);

    public abstract void removeLinkTo(V e);

    public abstract int getDegree();

    public E getContent() {
        return content;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof AbstractVertex) {
            return content.equals(((AbstractVertex<E, V>) obj).getContent());
        } else {
            return false;
        }

    }

    @Override
    public int hashCode() {
        return content.hashCode();
    }

}
