package org.uci.lids;

import org.apache.log4j.Logger;
import org.uci.lids.graph.DirectedGraph;
import org.uci.lids.utils.Misc;

import java.util.*;


public class Example {
    final static Logger logger = Logger.getLogger(LQGInfluenceDiagram.class);

    public static void main(String[] args) {
        DirectedGraph<Node> bn = new DirectedGraph<Node>();

        Node S = new Node(Node.VariableType.Categorical, Node.Category.Chance, "S");
        Node T = new Node(Node.VariableType.Categorical, Node.Category.Chance, "T");
        Node Tp = new Node(Node.VariableType.Categorical, Node.Category.Chance, "Tp");
        Node Test = new Node(Node.VariableType.Categorical, Node.Category.Decision, "Test");
        Node Drill = new Node(Node.VariableType.Categorical, Node.Category.Decision, "Drill");
        Node Cost = new Node(Node.VariableType.Categorical, Node.Category.Utility, "Cost");
        Node Util = new Node(Node.VariableType.Categorical, Node.Category.Utility, "Util");

        S.setStates(new String[]{"Dry", "Wet", "Soaked"});
        T.setStates(new String[]{"Closed", "Open", "Diffuse"});
        Tp.setStates(new String[]{"Closed", "Open", "Diffuse","NoTest"});
        Test.setStates(new String[]{"Test", "NoTest"});
        Drill.setStates(new String[]{"Drill", "NoDrill"});

        S.setPotential(new double[]{0.5, 0.3, 0.2});
        T.setPotential(new double[]{
                0.1, 0.3, 0.5,
                0.3, 0.4, 0.4,
                0.6, 0.3, 0.1});
        Tp.setPotential(new double[]{
                1, 0, 0, 0, 0, 0,
                0, 1, 0, 0, 0, 0,
                0, 0, 1, 0, 0, 0,
                0, 0, 0, 1, 1, 1});
        Cost.setPotential(new double[]{-10, 0});
        Util.setPotential(new double[]{-70, 50, 200, 0, 0, 0});

        bn.addNode(S);
        bn.addNode(T);
        bn.addNode(Tp);
        bn.addNode(Test);
        bn.addNode(Drill);
        bn.addNode(Cost);
        bn.addNode(Util);

        bn.addLink(S, Util);
        bn.addLink(Drill, Util);
        bn.addLink(S, T);
        bn.addLink(T, Tp);
        bn.addLink(Test, Tp);
        bn.addLink(Test, Cost);
        bn.addLink(Tp, Drill);

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