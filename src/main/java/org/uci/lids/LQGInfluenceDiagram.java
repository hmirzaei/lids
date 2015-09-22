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

    public static List<Set<Node>> getTemporalOrder(DirectedGraph<Node> bayesianNetwork) {
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

    private void updateNodePotentials(Map<Node, Integer> evidences) {
        nodePotentialMap = new HashMap<Node, Potential>();
        for (Node n : bayesianNetwork.getNodes()) {
            if (n.getCategory() == Node.Category.Chance)
                nodePotentialMap.put(n, new Potential((LinkedHashSet<Node>) bayesianNetwork.getFamily(n), n.getPotential()));
            else if (n.getCategory() == Node.Category.Utility)
                nodePotentialMap.put(n, new Potential((LinkedHashSet<Node>) bayesianNetwork.getParents(n), n.getPotential()));
        }
        if (evidences != null) {
            for (Map.Entry<Node, Integer> entry : evidences.entrySet()) {
                Node n = entry.getKey();
                for (Node member : bayesianNetwork.getChildren(n))
                    this.getNodePotential(member).applyEvidence(n, entry.getValue());
                this.getNodePotential(n).applyEvidence(n, entry.getValue());
            }
        }
    }

    public Map<Node, Potential> getOptimalStrategy() {
        return this.getOptimalStrategy(null);
    }

    public Map<Node, Potential> getOptimalStrategy(Map<Node, Integer> evidences) {
        this.updateNodePotentials(evidences);
        Map<Node, Potential> strategy = new HashMap<Node, Potential>();

        List<DirectedGraph<Node>> connectedComponents = this.bayesianNetwork.getConnectedComponents();
        for (DirectedGraph<Node> graph : connectedComponents) {
            getConnectedComponent‌OptimalStrategy(graph, strategy);
        }
        return strategy;
    }

    private void getConnectedComponent‌OptimalStrategy(DirectedGraph<Node> bayesianNetwork, Map<Node, Potential> strategy) {
        UndirectedGraph<Node> moralized = getMoralizedInfluenceDiagram(bayesianNetwork);
        List<Set<Node>> wholeNetworkTemporalOrder = getTemporalOrder(bayesianNetwork);

        if (logger.getEffectiveLevel() == Level.DEBUG)
            Misc.saveGraphOnDisk("moralized_" + Misc.asSortedList(bayesianNetwork.getNodes()).toString(), moralized);

        List<UndirectedGraph<Node>> connectedComponents = moralized.getConnectedComponents();

        for (UndirectedGraph<Node> graph : connectedComponents) {
            List<Node> orderedDecisionNodes = new ArrayList<Node>();
            for (Set<Node> nodeSet : wholeNetworkTemporalOrder) {
                if (!nodeSet.isEmpty()) {
                    Node dn = nodeSet.iterator().next();
                    if (dn.getCategory() == Node.Category.Decision)
                        if (graph.getNodes().contains(dn))
                            orderedDecisionNodes.add(dn);
                }
            }
            DirectedGraph<Node> directedGraph = bayesianNetwork.getSubGraph(graph.getNodes());
            for (int i = 0; i < orderedDecisionNodes.size() - 1; i++) {
                directedGraph.addLink(orderedDecisionNodes.get(i), orderedDecisionNodes.get(i + 1));
            }

            List<Set<Node>> temporalOrder = getTemporalOrder(directedGraph);
            Map<Node, Integer> temporalGroupNumbers = new HashMap<Node, Integer>();
            int groupNumber = 0;
            for (Set<Node> group : temporalOrder) {
                for (Node n : group)
                    temporalGroupNumbers.put(n, groupNumber);
                groupNumber++;
            }

            logger.debug("temporalOrder = \n" + temporalOrder.toString().replace(']', '\n'));
            graph.triangulate(temporalOrder);
            if (logger.getEffectiveLevel() == Level.DEBUG)
                Misc.saveGraphOnDisk("triangulated_" + Misc.asSortedList(graph.getNodes()), graph);
            UndirectedGraph<Node>.JunctionTreeAndRoot jtAndRoot = graph.getJunctionTree(temporalOrder);
            logger.debug("RootClique = " + jtAndRoot.rootClique);
            DirectedGraph<JunctionTreeNode<Node>> jt = jtAndRoot.junctionTree;
            if (logger.getEffectiveLevel() == Level.DEBUG)
                Misc.saveGraphOnDisk("jtree_" + Misc.asSortedList(graph.getNodes()), jt);


            DirectedGraph<Node> subGraph = bayesianNetwork.getSubGraph(graph.getNodes());
            Map<JunctionTreeNode<Node>, Set<Potential>> chanceCliquePotentials = getChanceCliquePotentials(subGraph, jt);
            Map<JunctionTreeNode<Node>, Set<Potential>> utilityCliquePotentials = getUtilityCliquePotentials(bayesianNetwork, jt);
            logger.debug("chanceCliquePotentials = " + chanceCliquePotentials);
            logger.debug("utilityCliquePotentials = " + utilityCliquePotentials);
            Map<JunctionTreeNode<Node>, Potential> chanceMessages = getChanceInitializedMessages(jt); // might be redundant
            Map<JunctionTreeNode<Node>, Potential> utilityMessages = getUtilityInitializedMessages(jt); // might be redundant

            List<JunctionTreeNode<Node>> topologicalSorted = jt.getTopologicalOrderedNodes();
            performDecisionMessagePassing(jt, chanceCliquePotentials, utilityCliquePotentials, strategy,
                    topologicalSorted, chanceMessages, utilityMessages, temporalGroupNumbers);
        }
    }

    private UndirectedGraph<Node> getMoralizedInfluenceDiagram(DirectedGraph<Node> bayesianNetwork) {

        try {
            DirectedGraph<Node> bn = bayesianNetwork.clone();
            List<Edge<Node>> edges = bn.getEdgeList();
            for (Edge<Node> e : edges)
                if (e.getNode2().getCategory() == Node.Category.Decision)
                    bn.removeLink(e.getNode1(), e.getNode2());

            UndirectedGraph<Node> moralized = bn.getMoralizedUndirectedCopy();
            for (Node n : bn.getNodes())
                if (n.getCategory() == Node.Category.Utility)
                    moralized.removeNode(n);
            return moralized;
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
            return null;
        }
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
        // find a leave
        do {
            root = it.next();
        } while (jt.getAdjacents(root).size() > 1);

        Map<JunctionTreeNode<Node>, Set<Potential>> cliquePotentials = getChanceCliquePotentials(graph, jt);
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

    private void performDecisionMessagePassing(DirectedGraph<JunctionTreeNode<Node>> sjt,
                                               Map<JunctionTreeNode<Node>, Set<Potential>> chanceCliquePotentials,
                                               Map<JunctionTreeNode<Node>, Set<Potential>> utilityCliquePotentials,
                                               Map<Node, Potential> strategy, List<JunctionTreeNode<Node>> topologicalSorted,
                                               Map<JunctionTreeNode<Node>, Potential> chanceMessages,
                                               Map<JunctionTreeNode<Node>, Potential> utilityMessages,
                                               final Map<Node, Integer> temporalGroupNumber) {


        for (JunctionTreeNode<Node> jtNode : topologicalSorted) {
            if (jtNode instanceof CliqueNode) {
                logger.debug(" ======================================= ");
                logger.debug("jtNode = " + jtNode);
                Set<JunctionTreeNode<Node>> parents = sjt.getParents(jtNode);
                logger.debug("parents = " + parents);

                Set<Potential> nodeChanceMessages = new HashSet<Potential>(chanceCliquePotentials.get(jtNode));
                Set<Potential> nodeUtilityMessages = new HashSet<Potential>(utilityCliquePotentials.get(jtNode));
                for (JunctionTreeNode<Node> member : parents) {
                    nodeChanceMessages.add(chanceMessages.get(member));
                    nodeUtilityMessages.add(utilityMessages.get(member));
                }

                logger.debug("nodeChanceMessages = " + nodeChanceMessages);
                logger.debug("nodeUtilityMessages = " + nodeUtilityMessages);
                Potential pChance = Potential.multiply(nodeChanceMessages).multiply(Potential.unityPotential(jtNode.getMembers()));
                Potential pUtility = Potential.sum(nodeUtilityMessages).multiply(Potential.unityPotential(jtNode.getMembers()));
                logger.debug("pChance = " + pChance);
                logger.debug("pUtility = " + pUtility);

                Set<Node> projectionNodes;
                if (!sjt.getChildren(jtNode).isEmpty()) {
                    JunctionTreeNode<Node> child = sjt.getChildren(jtNode).iterator().next();
                    projectionNodes = child.getMembers();
                } else { // [sigh] we are in root now
                    projectionNodes = new HashSet<Node>();
                }

                Set<Node> eliminatedNodes = new HashSet<Node>(pChance.getVariables());
                eliminatedNodes.removeAll(projectionNodes);
                logger.debug("eliminatedNodes = " + eliminatedNodes);

                // init projectionNodes for step by step elimination
                projectionNodes = new HashSet<Node>(pChance.getVariables());
                List<Node> orderedEliminatedNodes = Misc.asSortedList(eliminatedNodes, new Comparator<Node>() {
                    public int compare(Node o1, Node o2) {
                        return temporalGroupNumber.get(o2).compareTo(temporalGroupNumber.get(o1)); // sort descending
                    }
                });
                logger.debug("orderedEliminatedNodes = " + orderedEliminatedNodes);

                Iterator<Node> eliminatedNodeIterator = orderedEliminatedNodes.iterator();
                int prevGroupNumber = temporalGroupNumber.get(orderedEliminatedNodes.get(0));
                Set<Node> nodesToEliminateTogether = new HashSet<Node>();
                while (eliminatedNodeIterator.hasNext()) {
                    Node node = eliminatedNodeIterator.next();
                    Integer nodeGroupNumber = temporalGroupNumber.get(node);
                    if (nodeGroupNumber == prevGroupNumber) {
                        nodesToEliminateTogether.add(node);
                    } else {
                        logger.debug("nodesToEliminateTogether = " + nodesToEliminateTogether);
                        projectionNodes.removeAll(nodesToEliminateTogether);
                        Potential newPChance;
                        Potential newPUtility;
                        if (nodesToEliminateTogether.iterator().next().getCategory() == Node.Category.Chance) {
                            newPChance = pChance.project(projectionNodes);
                            newPUtility = pChance.multiply(pUtility).project(projectionNodes).divide(newPChance);
                        } else if (nodesToEliminateTogether.iterator().next().getCategory() == Node.Category.Decision) {
                            newPChance = pChance.maxProject(projectionNodes, pChance.multiply(pUtility)).getPotential();
                            newPUtility = pChance.multiply(pUtility).maxProject(projectionNodes).getPotential().divide(newPChance);
                            logger.info("maxState = " + pChance.multiply(pUtility).maxProject(projectionNodes).getMaxState());
                            logger.info("MEU = " + pChance.multiply(pUtility).maxProject(projectionNodes).getPotential().divide(pChance));

                        } else {
                            throw new UnsupportedOperationException("Cannot eliminate a utility node.");
                        }
                        pChance = newPChance;
                        pUtility = newPUtility;

                        nodesToEliminateTogether = new HashSet<Node>();
                        nodesToEliminateTogether.add(node);
                        prevGroupNumber = nodeGroupNumber;
                    }
                }
                logger.debug("nodesToEliminateTogether = " + nodesToEliminateTogether);
                projectionNodes.removeAll(nodesToEliminateTogether);
                Potential newPChance;
                Potential newPUtility;
                if (nodesToEliminateTogether.iterator().next().getCategory() == Node.Category.Chance) {
                    newPChance = pChance.project(projectionNodes);
                    newPUtility = pChance.multiply(pUtility).project(projectionNodes).divide(newPChance);
                } else if (nodesToEliminateTogether.iterator().next().getCategory() == Node.Category.Decision) {
                    newPChance = pChance.maxProject(projectionNodes, pChance.multiply(pUtility)).getPotential();
                    newPUtility = pChance.multiply(pUtility).maxProject(projectionNodes).getPotential().divide(newPChance);
                    logger.info("maxState = " + pChance.multiply(pUtility).maxProject(projectionNodes).getMaxState());
                    logger.info("MEU = " + pChance.multiply(pUtility).maxProject(projectionNodes).getPotential().divide(pChance));
                } else {
                    throw new UnsupportedOperationException("Cannot eliminate a utility node.");
                }
                pChance = newPChance;
                pUtility = newPUtility;

                if (!sjt.getChildren(jtNode).isEmpty()) {
                    JunctionTreeNode<Node> child = sjt.getChildren(jtNode).iterator().next();
                    Potential chanceMessage = chanceMessages.get(child);
                    Potential utilityMessage = utilityMessages.get(child);
                    chanceMessages.put(child, chanceMessage.multiply(pChance));
                    utilityMessages.put(child, utilityMessage.add(pUtility));
                }
            }
        }
    }

    private Map<Edge<JunctionTreeNode<Node>>, Potential> getInitializedMessages(AbstractGraph<JunctionTreeNode<Node>, ? extends AbstractVertex> jt) {
        Map<Edge<JunctionTreeNode<Node>>, Potential> messages = new HashMap<Edge<JunctionTreeNode<Node>>, Potential>();

        for (JunctionTreeNode<Node> jtNode : jt.getNodes()) {
            if (jtNode instanceof SeparatorNode)
                for (JunctionTreeNode<Node> adjacent : jt.getAdjacents(jtNode))
                    messages.put(new Edge<JunctionTreeNode<Node>>(jtNode, adjacent), Potential.unityPotential());
        }
        return messages;
    }

    private Map<JunctionTreeNode<Node>, Potential> getUtilityInitializedMessages(DirectedGraph<JunctionTreeNode<Node>> jt) {
        Map<JunctionTreeNode<Node>, Potential> messages = new HashMap<JunctionTreeNode<Node>, Potential>();

        for (JunctionTreeNode<Node> jtNode : jt.getNodes()) {
            if (jtNode instanceof SeparatorNode)
                messages.put(jtNode, Potential.zeroPotential());
        }
        return messages;
    }

    private Map<JunctionTreeNode<Node>, Potential> getChanceInitializedMessages(DirectedGraph<JunctionTreeNode<Node>> jt) {
        Map<JunctionTreeNode<Node>, Potential> messages = new HashMap<JunctionTreeNode<Node>, Potential>();

        for (JunctionTreeNode<Node> jtNode : jt.getNodes()) {
            if (jtNode instanceof SeparatorNode)
                messages.put(jtNode, Potential.unityPotential());
        }
        return messages;
    }

    private Map<JunctionTreeNode<Node>, Set<Potential>> getChanceCliquePotentials(DirectedGraph<Node> graph, AbstractGraph<JunctionTreeNode<Node>, ? extends AbstractVertex> jtree) {
        Map<JunctionTreeNode<Node>, Set<Potential>> cliquePotentials = new HashMap<JunctionTreeNode<Node>, Set<Potential>>();
        Set<Node> nodesCopy = new HashSet<Node>(graph.getNodes());
        for (JunctionTreeNode<Node> jtNode : jtree.getNodes()) {
            if (jtNode instanceof CliqueNode) {
                Set<Node> nodesToRemove = new HashSet<Node>();
                for (Node node : nodesCopy) {
                    if (node.getCategory() == Node.Category.Chance && jtNode.getMembers().containsAll(graph.getFamily(node))) {
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

    private Map<JunctionTreeNode<Node>, Set<Potential>> getUtilityCliquePotentials(DirectedGraph<Node> graph, AbstractGraph<JunctionTreeNode<Node>, ? extends AbstractVertex> jtree) {
        Map<JunctionTreeNode<Node>, Set<Potential>> cliquePotentials = new HashMap<JunctionTreeNode<Node>, Set<Potential>>();
        Set<Node> nodesCopy = new HashSet<Node>(graph.getNodes());
        for (JunctionTreeNode<Node> jtNode : jtree.getNodes()) {
            if (jtNode instanceof CliqueNode) {
                Set<Node> nodesToRemove = new HashSet<Node>();
                for (Node node : nodesCopy) {
                    if (node.getCategory() == Node.Category.Utility && jtNode.getMembers().containsAll(graph.getParents(node))) {
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
                    s.add(Potential.zeroPotential());
                    cliquePotentials.put(jtNode, s);
                }
            }
        }
        return cliquePotentials;
    }

}

