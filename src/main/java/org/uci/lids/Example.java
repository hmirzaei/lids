package org.uci.lids;

import org.uci.lids.graph.DirectedGraph;
import org.uci.lids.utils.Misc;



import java.util.*;


public class Example {

    public static void main(String[] args) {
        List<Node> nodes = new ArrayList<Node>();
        final int N = 10;
        final int NO_STATES = 3;
        DirectedGraph<Node> bn = new DirectedGraph<Node>();

        Random r = new Random(10);
        for (int i = 0; i < N; i++) {
            double randDouble = r.nextDouble();
            Node node;
            if (randDouble <2d/3)
                node = new Node(Node.VariableType.Categorical, Node.Category.Chance, Integer.toString(i));
            else if (randDouble<5d/6)
                node = new Node(Node.VariableType.Categorical, Node.Category.Decision, Integer.toString(i));
            else
                node = new Node(Node.VariableType.Categorical, Node.Category.Utility, Integer.toString(i));

            String[] sa = new String[NO_STATES];
            for (int j = 0; j < NO_STATES; j++) {
                sa[j] = Integer.toString(j);
            }
            node.setStates(sa);
            nodes.add(node);
            bn.addNode(node);
        }


        for (int i = 0; i < N; i++) {
            for (int j = i + 1; j < N; j++) {
                if (nodes.get(i).getCategory() != Node.Category.Utility)
                bn.addLink(nodes.get(i), nodes.get(j));
            }
        }


        for (int k = 0; k < 1.4 * (N * N); k++) {
            int i = r.nextInt(N);
            int j = r.nextInt(N);
            bn.removeLink(nodes.get(i), nodes.get(j));
        }
        bn.addLink(nodes.get(4), nodes.get(7));


        for (int i = 0; i < N; i++) {
            Set<Node> parents = bn.getParents(nodes.get(i));
            int size = (int) Math.round(Math.pow(NO_STATES, parents.size() + 1));
            double[] potential = new double[size];
            for (int j = 0; j < size; j++) {
                potential[j] = r.nextDouble();
            }

            for (int k = 0; k < size / NO_STATES; k++) {
                double sum = 0;
                for (int j = k; j < size; j += size / NO_STATES) {
                    sum += potential[j];
                }
                for (int j = k; j < size; j += size / NO_STATES) {
                    potential[j] /= sum;
                }
            }
            nodes.get(i).setPotential(potential);
        }

        Misc.saveGraphOnDisk("graph.html", bn);
        LQGInfluenceDiagram lid = new LQGInfluenceDiagram(bn);
        lid.getOptimalPolicy();
    }
}
