package org.uci.lids;

import org.apache.log4j.Logger;
import org.uci.lids.graph.DirectedGraph;
import org.uci.lids.utils.Misc;

import java.util.*;


public class Example {
    final static Logger logger = Logger.getLogger(LQGInfluenceDiagram.class);

    public static void main(String[] args) {
        List<Node> nodes = new ArrayList<Node>();
        int N = 25;
        final int NO_STATES = 2;
        DirectedGraph<Node> bn = new DirectedGraph<Node>();

        Random r = new Random(30);
        for (int i = 0; i < N; i++) {
            double randDouble = r.nextDouble();
            Node node;
            if (randDouble < 2d / 3)
                node = createNode(Node.Category.Chance, NO_STATES, i);
            else if (randDouble < 5d / 6)
                node = createNode(Node.Category.Decision, NO_STATES, i);
            else
                node = createNode(Node.Category.Utility, NO_STATES, i);
            nodes.add(node);
            bn.addNode(node);
        }

        for (int i = 0; i < N; i++) {
            for (int j = i + 1; j < N; j++) {
                if (nodes.get(i).getCategory() != Node.Category.Utility)
                    bn.addLink(nodes.get(i), nodes.get(j));
            }
        }
        for (int k = 0; k < .8 * (N * N); k++) {
            int i = r.nextInt(N);
            int j = r.nextInt(N);
            bn.removeLink(nodes.get(i), nodes.get(j));
        }


        Node prevNode = null;
        LinkedList<Node> topologicalOrderedNodes = bn.getTopologicalOrderedNodes();
        for (Node node : topologicalOrderedNodes)
            if (node.getCategory() == Node.Category.Decision) {
                if (prevNode != null)
                    bn.addLink(prevNode, node);
                prevNode = node;

                boolean hasChanceChildren = false;
                for (Node child : bn.getChildren(node))
                    if (child.getCategory() == Node.Category.Chance) {
                        hasChanceChildren = true;
                        break;
                    }
                if (!hasChanceChildren)
                    for (int i = topologicalOrderedNodes.indexOf(node) + 1; i < topologicalOrderedNodes.size(); i++) {
                        if (topologicalOrderedNodes.get(i).getCategory() == Node.Category.Chance) {
                            bn.addLink(node, topologicalOrderedNodes.get(i));
                            hasChanceChildren = true;
                            break;
                        }
                    }
                if (!hasChanceChildren) {
                    Node newNode = createNode(Node.Category.Chance, NO_STATES, N++);
                    nodes.add(newNode);
                    bn.addNode(newNode);
                    bn.addLink(node, newNode);
                }
            }

        List<DirectedGraph<Node>> connectedComponents = bn.getConnectedComponents();
        List<Node> connectingNodes = new ArrayList<Node>();
        for (DirectedGraph<Node> graph : connectedComponents) {
            boolean foundNonUtilityNode = false;
            for (Node node : graph.getNodes())
                if (node.getCategory() != Node.Category.Utility) {
                    connectingNodes.add(node);
                    foundNonUtilityNode = true;
                    break;
                }
            if (!foundNonUtilityNode) {
                bn.removeSubGraph(graph);
                nodes.removeAll(graph.getNodes());
            }

        }

        for (int i = 0; i < connectingNodes.size() - 1; i++) {
            bn.addLink(connectingNodes.get(i), connectingNodes.get(i + 1));
        }

//        Integer[] nodesToRemove = new Integer[]{24, 22, 21, 20, 19, 18, 17, 14, 9, 6, 3};
//        Arrays.sort(nodesToRemove, Collections.reverseOrder());
//        for (int i : nodesToRemove)
//            bn.removeNode(nodes.get(i));
//
//        for (int i: nodesToRemove)
//            nodes.remove(i);

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

        for (int i = 0; i < nodes.size(); i++) {
            Set<Node> parents = bn.getParents(nodes.get(i));
            if (nodes.get(i).getCategory() == Node.Category.Chance) {
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
            } else if (nodes.get(i).getCategory() == Node.Category.Utility) {
                int size = (int) Math.round(Math.pow(NO_STATES, parents.size()));
                double[] potential = new double[size];
                for (int j = 0; j < size; j++) {
                    potential[j] = r.nextDouble();
                }
                nodes.get(i).setPotential(potential);
            }

        }


        Misc.saveGraphOnDisk("graph", bn);
        Misc.writeHuginNet("hugin.net", bn, nodes);

        for (Node node : nodes) {
            String[] a;
            if (node.getPotential() != null) {
                a = new String[node.getPotential().length];
                int i = 0;
                for (double v : node.getPotential())
                    a[i++] = String.format("%.3f", v);
            } else
                a = null;
            logger.debug("bn.getParents(node) = " + bn.getParents(node));
            logger.debug(node.toString() + "'s Pot. : " + Arrays.toString(a));
        }

        LQGInfluenceDiagram lid = new LQGInfluenceDiagram(bn);
        lid.getOptimalStrategy();
    }

    private static Node createNode(Node.Category category, int NO_STATES, int i) {
        Node node;
        node = new Node(Node.VariableType.Categorical, category, Integer.toString(i));


        String[] sa;
        if (category == Node.Category.Utility) {
            sa = new String[1];
            for (int j = 0; j < 1; j++) {
                sa[j] = Integer.toString(j);
            }
        } else {
            sa = new String[NO_STATES];
            for (int j = 0; j < NO_STATES; j++) {
                sa[j] = Integer.toString(j);
            }

        }
        node.setStates(sa);
        return node;
    }
}