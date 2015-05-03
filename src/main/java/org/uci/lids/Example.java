package org.uci.lids;

import org.uci.lids.graph.DirectedGraph;
import org.uci.lids.utils.Misc;
import org.uci.lids.utils.Potential;

import java.util.*;


public class Example {

    public static void main(String[] args) {
        List<Node> nodes = new ArrayList<Node>();
        final int N = 6;
        DirectedGraph<Node> bn = new DirectedGraph<Node>();

        for (int i = 0; i < N; i++) {
            Node node = new Node(Node.VariableType.Categorical, Node.Category.Chance, Character.toString((char) ('a' + i)));
            node.setStates(new String[]{"0", "1"});
            nodes.add(node);
            bn.addNode(node);
        }
        nodes.get(4).setStates(new String[]{"0", "1", "3"});
        Node a = nodes.get(0);
        Node b = nodes.get(1);
        Node c = nodes.get(2);
        Node d = nodes.get(3);
        Node e = nodes.get(4);
        Node f = nodes.get(5);


        a.setPotential(new double[]{0.5, 0.2, 0.2, 0.7, 0.5, 0.8, 0.8, 0.3});
        b.setPotential(new double[]{0.1, 0.2, 0.6, 0.9, 0.8, 0.4});
        c.setPotential(new double[]{0.1, 0.6, 0.9, 0.4});
        d.setPotential(new double[]{0.1, 0.6, 0.9, 0.4});
        e.setPotential(new double[]{0.2, 0.5, 0.3});
        f.setPotential(new double[]{0.1, 0.6, 0.9, 0.4});

        bn.addLink(a, d);
        bn.addLink(b, a);
        bn.addLink(b, c);
        bn.addLink(c, a);
        bn.addLink(c, f);
        bn.addLink(e, b);



        LQGInfluenceDiagram lid = new LQGInfluenceDiagram(bn);
        Map<Node, Integer> evidences = new HashMap<Node, Integer>();
        evidences.put(b, 1);
        evidences.put(c, 1);
        Map<Node, Potential> marginals = lid.getMarginals(evidences);

        marginals = new TreeMap<Node, Potential>(marginals);
        Misc.saveGraphOnDisk("graph.htm", bn);
        System.out.println("marginals = " + marginals);
        Misc.writeBntScript("bnt.m", bn, nodes, 2, marginals);
    }
}
