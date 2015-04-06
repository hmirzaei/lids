package org.uci.lids;

import org.uci.lids.graph.DirectedGraph;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;


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
        Node a = nodes.get(0);
        Node b = nodes.get(1);
        Node c = nodes.get(2);
        Node d = nodes.get(3);
        Node e = nodes.get(4);
        Node f = nodes.get(5);

        a.setPotential(new double[]{0.5f, 0.2f, 0.2f, 0.7f, 0.5f, 0.8f, 0.8f, 0.3f});
        b.setPotential(new double[]{0.1f, 0.6f, 0.9f, 0.4f});
        c.setPotential(new double[]{0.1f, 0.6f, 0.9f, 0.4f});
        d.setPotential(new double[]{0.1f, 0.6f, 0.9f, 0.4f});
        e.setPotential(new double[]{0.2f, 0.8f});
        f.setPotential(new double[]{0.1f, 0.6f, 0.9f, 0.4f});

        bn.addLink(a, d);
        bn.addLink(b, a);
        bn.addLink(b, c);
        bn.addLink(c, a);
        bn.addLink(c, f);
        bn.addLink(e, b);

        try {
            PrintWriter writer = new PrintWriter("graph.htm", "UTF-8");
            writer.println(bn.generateVisualizationHtml("Bayesian Network"));
            writer.close();
        } catch (Exception e1) {
            e1.printStackTrace();
        }

        LQGInfluenceDiagram lid = new LQGInfluenceDiagram(bn);
        System.out.println("lid.getMarginals() = " + lid.getMarginals());
    }
}
