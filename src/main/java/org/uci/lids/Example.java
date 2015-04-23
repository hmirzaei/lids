package org.uci.lids;

import org.uci.lids.graph.DirectedGraph;
import org.uci.lids.utils.Misc;
import org.uci.lids.utils.Potential;

import java.util.*;


public class Example {

    public static void main(String[] args) {
        List<Node> nodes = new ArrayList<Node>();
        final int N = 1000;
        final int NO_STATES = 3;
        DirectedGraph<Node> bn = new DirectedGraph<Node>();

        for (int i = 0; i < N; i++) {
            Node node = new Node(Node.VariableType.Categorical, Node.Category.Chance, Integer.toString(i));

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
                bn.addLink(nodes.get(i), nodes.get(j));
            }
        }

        Random r = new Random(10);
        for (int k = 0; k < 6.56 * (N * N); k++) {
            int i = r.nextInt(N);
            int j = r.nextInt(N);
            bn.removeLink(nodes.get(i), nodes.get(j));
        }
        Misc.saveGraphOnDisk("graph.htm", "Bayesian Network", bn);


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


        LQGInfluenceDiagram lid = new LQGInfluenceDiagram(bn);

        long startTime = System.currentTimeMillis();
        Map<Node, Potential> marginals = lid.getMarginals();
        long estimatedTime = System.currentTimeMillis() - startTime;

        marginals = new TreeMap<Node, Potential>(marginals);
        System.out.println("marginals = " + marginals);
        System.out.println("marginals.size() = " + marginals.size());

        System.out.println("estimatedTime = " + estimatedTime / 1000.0 + " (s)");
        Misc.writeBntScript("bnt.m", bn, nodes, NO_STATES, marginals);

    }
}
