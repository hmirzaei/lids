package org.uci.lids.graph;

import java.util.Set;

/**
 * Created by Hamid Mirzaei on 3/22/15.
 */
public class CliqueNode<E> extends JunctionTreeNode<E> {


    public CliqueNode(Set<E> members) {
        super(members);
    }

    public String nodeType() {
        return "Clique";
    }

}
