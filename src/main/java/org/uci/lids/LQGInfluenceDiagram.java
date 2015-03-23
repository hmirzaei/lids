package org.uci.lids;

import org.uci.lids.graph.DirectedGraph;

/**
 * Created by hamid on 3/9/15.
 */
public class LQGInfluenceDiagram {

    private DirectedGraph<Node> bayesianNetwork = new DirectedGraph<Node>();


    public LQGInfluenceDiagram(DirectedGraph<Node> bayesianNetwork) {
        this.bayesianNetwork = bayesianNetwork;
    }


}
