package org.uci.lids;

import org.uci.lids.graph.*;
import org.uci.lids.utils.Potential;

import java.util.*;

/**
 * Created by hamid on 3/9/15.
 */
public class LQGInfluenceDiagram {

    private DirectedGraph<Node> bayesianNetwork = new DirectedGraph<Node>();


    public LQGInfluenceDiagram(DirectedGraph<Node> bayesianNetwork) {
        this.bayesianNetwork = bayesianNetwork;
    }

    public Potential getNodePotential(Node node) {
        return new Potential(bayesianNetwork.getFamily(node), node.getPotential());
    }

    public Map<Node, Potential> getMarginals() {
        Map<JunctionTreeNode<Node>, Potential> cliqueMarginals = new HashMap<JunctionTreeNode<Node>, Potential>();

        List<DirectedGraph<Node>> connectedComponents = this.bayesianNetwork.getConnectedComponents();
        for (DirectedGraph<Node> graph : connectedComponents) {
            getConnectedComponentMarginal(graph, cliqueMarginals);
        }
        Map<Node, Potential> result = new HashMap<Node, Potential>();
        for (Map.Entry<JunctionTreeNode<Node>, Potential> entry : cliqueMarginals.entrySet()) {
            for (Node node : entry.getKey().getMembers()) {
                Set<Node> s = new HashSet<Node>();
                s.add(node);
                result.put(node, entry.getValue().project(s));
            }
        }
        return result;
    }

    private void getConnectedComponentMarginal(DirectedGraph<Node> graph, Map<JunctionTreeNode<Node>, Potential> cliqueMarginals) {
        UndirectedGraph<Node> moralized = graph.getMoralizedUndirectedCopy();
        moralized.triangulate();
        UndirectedGraph<JunctionTreeNode<Node>> jt = moralized.getJunctionTree();


        Iterator<JunctionTreeNode<Node>> it = jt.getNodes().iterator();
        JunctionTreeNode<Node> root;
        do {
            root = it.next();
        } while (jt.getAdjacents(root).size() > 1);

        Map<JunctionTreeNode<Node>, Set<Potential>> cliquePotentials = getCliquePotentials(graph, jt);
        Map<Edge<JunctionTreeNode<Node>>, Potential> messages = getInitializedMessages(jt);

        DirectedGraph<JunctionTreeNode<Node>> jtd = jt.getTreeSinkTo(root);
        List<JunctionTreeNode<Node>> topologicalSorted = jtd.getTopologicalOrderedNodes();
        performMessagePassing(jt, jtd, cliquePotentials, cliqueMarginals, topologicalSorted, messages, false);

        jtd = jt.getTreeSourceFrom(root);

        topologicalSorted = jtd.getTopologicalOrderedNodes();
        performMessagePassing(jt, jtd, cliquePotentials, cliqueMarginals, topologicalSorted, messages, true);
    }

    private void performMessagePassing(UndirectedGraph<JunctionTreeNode<Node>> jt, DirectedGraph<JunctionTreeNode<Node>> jtd,
                                       Map<JunctionTreeNode<Node>, Set<Potential>> cliquePotentials, Map<JunctionTreeNode<Node>,
            Potential> cliqueMarginals, List<JunctionTreeNode<Node>> topologicalSorted,
                                       Map<Edge<JunctionTreeNode<Node>>, Potential> messages, boolean calculateMarginals) {


        for (JunctionTreeNode<Node> jtNode : topologicalSorted) {
            if (jtNode instanceof CliqueNode) {
                Set<JunctionTreeNode<Node>> children = jtd.getChildren(jtNode);
                Set<JunctionTreeNode<Node>> adjacents = jt.getAdjacents(jtNode);

                for (JunctionTreeNode<Node> child : children) {
                    Set<JunctionTreeNode<Node>> parents = new HashSet<JunctionTreeNode<Node>>(adjacents);
                    parents.remove(child);

                    Set<Potential> s = new HashSet<Potential>(cliquePotentials.get(jtNode));
                    for (JunctionTreeNode<Node> parent : parents) {
                        s.add(messages.get(new Edge<JunctionTreeNode<Node>>(parent, jtNode)));
                    }

                    Potential p = Potential.multiply(s);
                    Potential p2 = p.project(child.getMembers());
                    JunctionTreeNode<Node> grandChild = jtd.getChildren(child).iterator().next();
                    Edge e = new Edge<JunctionTreeNode<Node>>(child, grandChild);
                    Potential message = messages.get(e);
                    messages.put(e, message.multiply(p2));
                }

                if (calculateMarginals) {
                    Set<JunctionTreeNode<Node>> parents = new HashSet<JunctionTreeNode<Node>>(adjacents);

                    Set<Potential> s = new HashSet<Potential>(cliquePotentials.get(jtNode));
                    for (JunctionTreeNode<Node> parent : parents) {
                        s.add(messages.get(new Edge<JunctionTreeNode<Node>>(parent, jtNode)));
                    }
                    Potential p = Potential.multiply(s);
                    cliqueMarginals.put(jtNode, p);
                }
            }
        }
    }

    private Map<Edge<JunctionTreeNode<Node>>, Potential> getInitializedMessages(UndirectedGraph<JunctionTreeNode<Node>> jt) {
        Map<Edge<JunctionTreeNode<Node>>, Potential> messages = new HashMap<Edge<JunctionTreeNode<Node>>, Potential>();

        for (JunctionTreeNode<Node> jtNode : jt.getNodes()) {
            if (jtNode instanceof SeparatorNode)
                for (JunctionTreeNode<Node> adjacent : jt.getAdjacents(jtNode))
                    messages.put(new Edge<JunctionTreeNode<Node>>(jtNode, adjacent), Potential.unityPotential());
        }
        return messages;
    }

    private Map<JunctionTreeNode<Node>, Set<Potential>> getCliquePotentials(DirectedGraph<Node> graph, UndirectedGraph<JunctionTreeNode<Node>> jt) {
        Map<JunctionTreeNode<Node>, Set<Potential>> cliquePotentials = new HashMap<JunctionTreeNode<Node>, Set<Potential>>();
        Set<Node> nodesCopy = new HashSet<Node>(graph.getNodes());
        for (JunctionTreeNode<Node> jtNode : jt.getNodes()) {
            if (jtNode instanceof CliqueNode) {
                Set<Node> nodesToRemove = new HashSet<Node>();
                for (Node node : nodesCopy) {
                    if (jtNode.getMembers().containsAll(graph.getFamily(node))) {
                        Potential p = getNodePotential(node);
                        if (cliquePotentials.containsKey(jtNode))
                            cliquePotentials.get(jtNode).add(p);
                        else {
                            Set<Potential> s = new HashSet<Potential>();
                            s.add(p);
                            cliquePotentials.put(jtNode, s);
                        }
                        nodesToRemove.add(node);
                    }
                }
                nodesCopy.removeAll(nodesToRemove);
                if (!cliquePotentials.containsKey(jtNode)) {
                    Set<Potential> s = new HashSet<Potential>();
                    s.add(Potential.unityPotential());
                    cliquePotentials.put(jtNode, s);
                }
            }
        }
        return cliquePotentials;
    }

}
