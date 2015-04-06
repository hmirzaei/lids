package org.uci.lids.graph;

import java.util.Set;

/**
 * Created by Hamid Mirzaei on 3/22/15.
 */
public class SeparatorNode<E> extends JunctionTreeNode<E> {

    public SeparatorNode(Set<E> members) {
        super(members);
    }

    public String nodeType() {
        return "Separator";
    }

}
