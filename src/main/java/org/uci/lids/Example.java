package org.uci.lids;

import org.uci.lids.graph.DirectedGraph;
import org.uci.lids.utils.Misc;

import java.util.*;


public class Example {

    public static void main(String[] args) {
        List<Node> nodes = new ArrayList<Node>();
        int N = 100;
        final int NO_STATES = 3;
        DirectedGraph<Node> bn = new DirectedGraph<Node>();

        Random r = new Random(30);
        for (int i = 0; i < N; i++) {
            double randDouble = r.nextDouble();
            Node node;
            if (randDouble < 2d / 3)
                node = createNode(Node.Category.Chance, NO_STATES, i);
            else if (randDouble < 5d / 6)
                node = createNode(Node.Category.Decision, NO_STATES
                        , i);
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
        for (int k = 0; k < 5 * (N * N); k++) {
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

        for (int i = 0; i < nodes.size(); i++) {
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

    private static Node createNode(Node.Category category, int NO_STATES, int i) {
        Node node;
        node = new Node(Node.VariableType.Categorical, category, Integer.toString(i));

        String[] sa = new String[NO_STATES];
        for (int j = 0; j < NO_STATES; j++) {
            sa[j] = Integer.toString(j);
        }
        node.setStates(sa);
        return node;
    }
}
