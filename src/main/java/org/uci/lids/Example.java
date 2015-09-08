package org.uci.lids;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.uci.lids.graph.DirectedGraph;
import org.uci.lids.utils.Misc;


public class Example {

    final static Logger logger = Logger.getLogger(Example.class);

    public static void main(String[] args) {
        DirectedGraph<Node> bn = new DirectedGraph<Node>();

        Node A = new Node(Node.VariableType.Categorical, Node.Category.Chance, "A");
        Node B = new Node(Node.VariableType.Categorical, Node.Category.Chance, "B");
        Node C = new Node(Node.VariableType.Categorical, Node.Category.Chance, "C");
        Node T = new Node(Node.VariableType.Categorical, Node.Category.Chance, "T");
        Node D1 = new Node(Node.VariableType.Categorical, Node.Category.Decision, "D1");
        Node D2 = new Node(Node.VariableType.Categorical, Node.Category.Decision, "D2");
        Node V1 = new Node(Node.VariableType.Categorical, Node.Category.Utility, "V1");
        Node V2 = new Node(Node.VariableType.Categorical, Node.Category.Utility, "V2");

        A.setStates(new String[]{"Y", "N"});
        B.setStates(new String[]{"Y", "N"});
        C.setStates(new String[]{"Y", "N"});
        T.setStates(new String[]{"Y", "N"});
        D1.setStates(new String[]{"D1_1", "D1_2"});
        D2.setStates(new String[]{"D2_1", "D2_2"});

        A.setPotential(new double[]{0.2, 0.8, 0.8, 0.2});
        B.setPotential(new double[]{0.8, 0.2, 0.2, 0.8});
        C.setPotential(new double[]{0.9, 0.5, 0.5, 0.9, 0.1, 0.5, 0.5, 0.1});
        T.setPotential(new double[]{0.9, 0.5, 0.5, 0.1, 0.1, 0.5, 0.5, 0.9});
        V1.setPotential(new double[]{3, 0, 0, 2});
        V2.setPotential(new double[]{10, 0});

        bn.addNode(A);
        bn.addNode(B);
        bn.addNode(C);
        bn.addNode(T);
        bn.addNode(D1);
        bn.addNode(D2);
        bn.addNode(V1);
        bn.addNode(V2);

        bn.addLink(D1, A);
        bn.addLink(A, B);
        bn.addLink(B, C);
        bn.addLink(D2, C);
        bn.addLink(B, T);
        bn.addLink(A, T);
        bn.addLink(A, V1);
        bn.addLink(D2, V1);
        bn.addLink(C, V2);
        bn.addLink(T, D2);

       if (logger.getEffectiveLevel() == Level.DEBUG)
            Misc.saveGraphOnDisk("graph.html", bn);
        LQGInfluenceDiagram lid = new LQGInfluenceDiagram(bn);
        lid.getOptimalStrategy();
    }
}
