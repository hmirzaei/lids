package org.uci.lids.graph;

import java.util.Set;
import java.util.UUID;

/**
 * Created by Hamid Mirzaei on 3/22/15.
 */
@SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
public abstract class JunctionTreeNode<E> implements Visualizable {
    private UUID uid;
    private Set<E> members;

    public JunctionTreeNode(Set<E> members) {
        this.uid = UUID.randomUUID();
        this.members = members;
    }

    public Set<E> getMembers() {
        return members;
    }

    public UUID getUid() {
        return uid;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        String prefix = "";
        for (E e : members) {
            sb.append(prefix).append(e.toString());
            prefix = "_";
        }
        return sb.toString();
    }

    public abstract String nodeType();

    @Override
    public boolean equals(Object obj) {
        return this.uid.equals(((JunctionTreeNode) obj).getUid());
    }

    @Override
    public int hashCode() {
        return uid.hashCode();
    }

}
