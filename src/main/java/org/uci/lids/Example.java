package org.uci.lids;

import org.apache.log4j.Logger;
import org.uci.lids.graph.DirectedGraph;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;


public class Example {
    final static Logger logger = Logger.getLogger(InfluenceDiagram.class);

    public static void main(String[] args) {
        DirectedGraph<Node> bn = new DirectedGraph<Node>();
        final int N = 100;
        final int NO_STATES = 30;
        List<Node> nodes = new ArrayList<Node>();

        String[] states = new String[NO_STATES];
        for (int j = 0; j < NO_STATES; j++) {
            states[j] = Integer.toString(j);
        }
        for (int i = 0; i < N; i++) {
            nodes.add(new Node(Node.VariableType.Categorical, Node.Category.Chance, "X" + i));
            nodes.get(nodes.size() - 1).setStates(states);
        }
        for (int i = 0; i < N; i++) {
            nodes.add(new Node(Node.VariableType.Categorical, Node.Category.Decision, "U" + i));
            nodes.get(nodes.size() - 1).setStates(states);
        }
        for (int i = 0; i < N; i++) {
            nodes.add(new Node(Node.VariableType.Categorical, Node.Category.Utility, "J" + i));
        }
        for (Node node : nodes) {
            bn.addNode(node);
        }
        for (int i = 0; i < N; i++) {
            bn.addLink(nodes.get(i + N), nodes.get(i));
            bn.addLink(nodes.get(i + N), nodes.get(i + 2 * N));
            bn.addLink(nodes.get(i), nodes.get(i + 2 * N));
        }

        for (int i = 1; i < N; i++) {
            bn.addLink(nodes.get(i - 1), nodes.get(i));
            bn.addLink(nodes.get(i - 1), nodes.get(N + i));
        }


//        List<Set<Node>> wholeNetworkTemporalOrder = InfluenceDiagram.getTemporalOrder(bn);
//        for (int i = 1; i < wholeNetworkTemporalOrder.size(); i++) {
//            if (!wholeNetworkTemporalOrder.get(i).isEmpty()) {
//                Node dn = wholeNetworkTemporalOrder.get(i).iterator().next();
//                if (dn.getCategory() == Node.Category.Decision) {
//                    for (Set<Node> nodeSet2 : wholeNetworkTemporalOrder.subList(0, i - 1)) {
//                        for (Node n : nodeSet2)
//                            bn.addLink(n, dn);
//                    }
//                }
//            }
//        }

        Random r = new Random(30);
        for (Node node : nodes) {
            Set<Node> parents = bn.getParents(node);
            if (node.getCategory() == Node.Category.Chance) {
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
                node.setPotentialArray(potential);
            } else if (node.getCategory() == Node.Category.Utility) {
                int size = (int) Math.round(Math.pow(NO_STATES, parents.size()));
                double[] potential = new double[size];
                for (int j = 0; j < size; j++) {
                    potential[j] = r.nextDouble();
                }
                node.setPotentialArray(potential);
            }

        }

        //Misc.saveGraphOnDisk("graph", bn);
        //Misc.writeHuginNet("hugin.net", bn, new ArrayList<Node>(bn.getNodes()));

        logger.info("Start");
        InfluenceDiagram lid = new InfluenceDiagram(bn);
        lid.getOptimalStrategy();
        logger.info("End");
    }


}