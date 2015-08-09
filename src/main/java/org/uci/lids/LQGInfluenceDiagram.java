package org.uci.lids;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.uci.lids.graph.*;
import org.uci.lids.utils.Misc;
import org.uci.lids.utils.Potential;

import java.util.*;

/**
 * Created by hamid on 3/9/15.
 */
public class LQGInfluenceDiagram {

    final static Logger logger = Logger.getLogger(LQGInfluenceDiagram.class);
    private DirectedGraph<Node> bayesianNetwork;
    private Map<Node, Potential> nodePotentialMap;

    public LQGInfluenceDiagram(DirectedGraph<Node> bayesianNetwork) {
        this.bayesianNetwork = bayesianNetwork;
    }

    private void updateNodePotentials(Map<Node, Integer> evidences) {
        nodePotentialMap = new HashMap<Node, Potential>();
        for (Node n : bayesianNetwork.getNodes()) {
            nodePotentialMap.put(n, new Potential((LinkedHashSet<Node>) bayesianNetwork.getFamily(n), n.getPotential()));
        }
        for (Map.Entry<Node, Integer> entry : evidences.entrySet()) {
            Node n = entry.getKey();
            for (Node member : bayesianNetwork.getChildren(n))
                this.getNodePotential(member).applyEvidence(n, entry.getValue());
            this.getNodePotential(n).applyEvidence(n, entry.getValue());
        }
    }


    public void getOptimalPolicy() {
        List<DirectedGraph<Node>> connectedComponents = this.bayesianNetwork.getConnectedComponents();
        for (DirectedGraph<Node> graph : connectedComponents) {
            getConnectedComponent‌OptimalPolicy(graph);
        }
    }

    public void getConnectedComponent‌OptimalPolicy(DirectedGraph<Node> bayesianNetwork) {

        List<Set<Node>> temporalOrder = getTemporalOrder(bayesianNetwork);
        logger.debug("temporalOrder = \n" + temporalOrder.toString().replace(']', '\n'));
        UndirectedGraph<Node> moralized = getMoralizedInfluenceDiagram(bayesianNetwork);
        if (logger.getEffectiveLevel() == Level.DEBUG)
            Misc.saveGraphOnDisk("moralized.html", moralized);

        List<UndirectedGraph<Node>> connectedComponents = moralized.getConnectedComponents();

        for (UndirectedGraph<Node> graph : connectedComponents) {
            graph.triangulate(temporalOrder);
            if (logger.getEffectiveLevel() == Level.DEBUG)
                Misc.saveGraphOnDisk("triangulated.html", graph);
            UndirectedGraph<Node>.JunctionTreeAndRoot jtAndRoot = graph.getJunctionTree(temporalOrder);
            logger.debug("RootClique = " + jtAndRoot.rootClique);
            DirectedGraph<JunctionTreeNode<Node>> jt = jtAndRoot.junctionTree;
            if (logger.getEffectiveLevel() == Level.DEBUG)
                Misc.saveGraphOnDisk("jtree.html", jt);
        }
    }

    private UndirectedGraph<Node> getMoralizedInfluenceDiagram(DirectedGraph<Node> bayesianNetwork) {
        List<Edge<Node>> edges = bayesianNetwork.getEdgeList();
        for (Edge<Node> e : edges)
            if (e.getNode2().getCategory() == Node.Category.Decision)
                bayesianNetwork.removeLink(e.getNode1(), e.getNode2());

        UndirectedGraph<Node> moralized = bayesianNetwork.getMoralizedUndirectedCopy();
        for (Node n : bayesianNetwork.getNodes())
            if (n.getCategory() == Node.Category.Utility)
                moralized.removeNode(n);
        return moralized;
    }

    private List<Set<Node>> getTemporalOrder(DirectedGraph<Node> bayesianNetwork) {
        List<Set<Node>> temporalOrder = new ArrayList<Set<Node>>();
        LinkedList<Node> topologicalOrderedNodes = bayesianNetwork.getTopologicalOrderedNodes();

        Set<Node> lastNodes = new HashSet<Node>();
        temporalOrder.add(new HashSet<Node>());

        for (Node n : topologicalOrderedNodes) {
            if (n.getCategory() == Node.Category.Chance)
                lastNodes.add(n);
        }


        for (Node n : topologicalOrderedNodes) {
            if (n.getCategory() == Node.Category.Decision) {
                boolean hasChanceParent = false;
                for (Node parent : bayesianNetwork.getParents(n)) {
                    if (parent.getCategory() == Node.Category.Chance) {
                        if (lastNodes.contains(parent)) {
                            lastNodes.remove(parent);
                            temporalOrder.get(temporalOrder.size() - 1).add(parent);
                            hasChanceParent = true;
                        }
                    }
                }
                if (hasChanceParent) temporalOrder.add(new HashSet<Node>());
                temporalOrder.get(temporalOrder.size() - 1).add(n);
                temporalOrder.add(new HashSet<Node>());
            }
        }
        temporalOrder.set(temporalOrder.size() - 1, lastNodes);
        return temporalOrder;
    }

    public Potential getNodePotential(Node node) {
        return nodePotentialMap.get(node);
    }

    public Map<Node, Potential> getMarginals() {
        return getMarginals(null);
    }

    public Map<Node, Potential> getMarginals(Map<Node, Integer> evidences) {
        this.updateNodePotentials(evidences);
        Map<JunctionTreeNode<Node>, Potential> cliqueMarginals = new HashMap<JunctionTreeNode<Node>, Potential>();

        List<DirectedGraph<Node>> connectedComponents = this.bayesianNetwork.getConnectedComponents();
        for (DirectedGraph<Node> graph : connectedComponents) {
            getConnectedComponentMarginal(graph, cliqueMarginals);
        }
        Map<Node, Potential> result = new HashMap<Node, Potential>();
        for (Map.Entry<JunctionTreeNode<Node>, Potential> entry : cliqueMarginals.entrySet()) {
            for (Node node : entry.getKey().getMembers()) {
                LinkedHashSet<Node> s = new LinkedHashSet<Node>();
                s.add(node);
                result.put(node, entry.getValue().project(s));
            }
        }
        if (evidences != null) {
            for (Map.Entry<Node, Potential> entry : result.entrySet()) {
                entry.getValue().normalize(entry.getKey());
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
                    Edge<JunctionTreeNode<Node>> e = new Edge<JunctionTreeNode<Node>>(child, grandChild);
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

