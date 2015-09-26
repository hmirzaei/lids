package org.uci.lids;

import org.apache.log4j.Logger;
import org.uci.lids.graph.DirectedGraph;
import org.uci.lids.utils.Misc;

import java.util.*;


public class Example {
    final static Logger logger = Logger.getLogger(LQGInfluenceDiagram.class);

    public static void main(String[] args) {
        DirectedGraph<Node> bn = new DirectedGraph<Node>();

        Node A = new Node(Node.VariableType.Categorical, Node.Category.Chance, "A");
        Node B = new Node(Node.VariableType.Categorical, Node.Category.Chance, "B");
        Node D = new Node(Node.VariableType.Categorical, Node.Category.Decision, "D");
        Node E = new Node(Node.VariableType.Categorical, Node.Category.Decision, "E");
        Node Y = new Node(Node.VariableType.Categorical, Node.Category.Utility, "Y");

        A.setStates(new String[]{"0", "1"});
        B.setStates(new String[]{"0", "1"});
        D.setStates(new String[]{"0", "1"});
        E.setStates(new String[]{"0", "1"});

        A.setPotential(new double[]{0, 0.5, 1, 0.5});
        B.setPotential(new double[]{1, 0, 0, 1, 0, 1, 1, 0});
        Y.setPotential(new double[]{1, 0});

        bn.addNode(A);
        bn.addNode(B);
        bn.addNode(D);
        bn.addNode(E);
        bn.addNode(Y);

        bn.addLink(D, A);
        bn.addLink(E, B);
        bn.addLink(D, E);
        bn.addLink(A, B);
        bn.addLink(B, Y);

        List<Set<Node>> wholeNetworkTemporalOrder = LQGInfluenceDiagram.getTemporalOrder(bn);
        for (int i = 1; i < wholeNetworkTemporalOrder.size(); i++) {
            if (!wholeNetworkTemporalOrder.get(i).isEmpty()) {
                Node dn = wholeNetworkTemporalOrder.get(i).iterator().next();
                if (dn.getCategory() == Node.Category.Decision) {
                    for (Set<Node> nodeSet2 : wholeNetworkTemporalOrder.subList(0, i - 1)) {
                        for (Node n : nodeSet2)
                            bn.addLink(n, dn);
                    }
                }
            }
        }

        Misc.saveGraphOnDisk("graph", bn);
        Misc.writeHuginNet("hugin.net", bn, new ArrayList<Node>(bn.getNodes()));

        LQGInfluenceDiagram lid = new LQGInfluenceDiagram(bn);
        lid.getOptimalStrategy();
    }


}