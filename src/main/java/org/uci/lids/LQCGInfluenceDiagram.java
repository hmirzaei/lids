package org.uci.lids;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.uci.lids.graph.*;
import org.uci.lids.utils.CGPotential;
import org.uci.lids.utils.CGUtility;
import org.uci.lids.utils.Misc;
import org.uci.lids.utils.Potential;

import java.util.*;

/**
 * Created by hamid on 3/9/15.
 */
public class LQCGInfluenceDiagram {

    final static Logger logger = Logger.getLogger(LQCGInfluenceDiagram.class);
    private DirectedGraph<Node> bayesianNetwork;
    private Map<Node, Object> nodePotentialMap;

    public LQCGInfluenceDiagram(DirectedGraph<Node> bayesianNetwork) {
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

    public static UndirectedGraph<Node> getMoralizedInfluenceDiagram(DirectedGraph<Node> bayesianNetwork) {

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

    private void updateNodePotentials(Map<Node, Integer> evidences) {
        nodePotentialMap = new HashMap<Node, Object>();
        for (Node n : bayesianNetwork.getNodes()) {
            if (n.getCategory() == Node.Category.Chance)
                nodePotentialMap.put(n, n.getCGPotential(bayesianNetwork));
            if (n.getCategory() == Node.Category.Utility)
                nodePotentialMap.put(n, n.getCGUtility(bayesianNetwork));
        }
        /*if (evidences != null) {
            for (Map.Entry<Node, Integer> entry : evidences.entrySet()) {
                Node n = entry.getKey();
                for (Node member : bayesianNetwork.getChildren(n))
                    this.getNodePotential(member).applyEvidence(n, entry.getValue());
                this.getNodePotential(n).applyEvidence(n, entry.getValue());
            }
        }*/
    }

    public Map<Node, CGPotential> getOptimalStrategy() {
        return this.getOptimalStrategy(null);
    }

    public Map<Node, CGPotential> getOptimalStrategy(Map<Node, Integer> evidences) {
        this.updateNodePotentials(evidences);
        Map<Node, CGPotential> strategy = new HashMap<Node, CGPotential>();

        List<DirectedGraph<Node>> connectedComponents = this.bayesianNetwork.getConnectedComponents();
        for (DirectedGraph<Node> graph : connectedComponents) {
            getConnectedComponent‌OptimalStrategy(graph, strategy);
        }
        return strategy;
    }

    private void getConnectedComponent‌OptimalStrategy(DirectedGraph<Node> bayesianNetwork, Map<Node, CGPotential> strategy) {
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
            Map<JunctionTreeNode<Node>, Set<CGPotential>> chanceCliquePotentials = getChanceCliquePotentials(subGraph, jt);
            Map<JunctionTreeNode<Node>, Set<CGUtility>> utilityCliquePotentials = getUtilityCliquePotentials(bayesianNetwork, jt);
            logger.debug("chanceCliquePotentials = " + chanceCliquePotentials);
            logger.debug("utilityCliquePotentials = " + utilityCliquePotentials);
            Map<JunctionTreeNode<Node>, CGPotential> chanceMessages = getChanceInitializedMessages(jt); // might be redundant
            Map<JunctionTreeNode<Node>, CGUtility> utilityMessages = getUtilityInitializedMessages(jt); // might be redundant

            List<JunctionTreeNode<Node>> topologicalSorted = jt.getTopologicalOrderedNodes();
            performDecisionMessagePassing(jt, chanceCliquePotentials, utilityCliquePotentials, strategy,
                    topologicalSorted, chanceMessages, utilityMessages, temporalGroupNumbers);
        }
    }

    public Object getNodePotential(Node node) {
        return nodePotentialMap.get(node);
    }

    public Map<Node, CGPotential> getMarginals() {
        return getMarginals(null);
    }

    public Map<Node, CGPotential> getMarginals(Map<Node, Integer> evidences) { //TODO not complete
        this.updateNodePotentials(evidences);
        Map<JunctionTreeNode<Node>, CGPotential> cliqueMarginals = new HashMap<JunctionTreeNode<Node>, CGPotential>();

        List<DirectedGraph<Node>> connectedComponents = this.bayesianNetwork.getConnectedComponents();
        for (DirectedGraph<Node> graph : connectedComponents) {
            getConnectedComponentMarginal(graph, cliqueMarginals);
        }
        Map<Node, CGPotential> result = new HashMap<Node, CGPotential>();
//        for (Map.Entry<JunctionTreeNode<Node>, Potential> entry : cliqueMarginals.entrySet()) {
//            for (Node node : entry.getKey().getMembers()) {
//                LinkedHashSet<Node> s = new LinkedHashSet<Node>();
//                s.add(node);
//                result.put(node, entry.getValue().project(s));
//            }
//        }
//        if (evidences != null) {
//            for (Map.Entry<Node, CGPotential> entry : result.entrySet()) {
//                entry.getValue().normalize(entry.getKey());
//            }
//        }
        return result;
    }

    private void getConnectedComponentMarginal(DirectedGraph<Node> graph, Map<JunctionTreeNode<Node>, CGPotential> cliqueMarginals) { //TODO not complete
        UndirectedGraph<Node> moralized = graph.getMoralizedUndirectedCopy();
        moralized.triangulate();
        UndirectedGraph<JunctionTreeNode<Node>> jt = moralized.getJunctionTree();


        Iterator<JunctionTreeNode<Node>> it = jt.getNodes().iterator();
        JunctionTreeNode<Node> root;
        // find a leave
        do {
            root = it.next();
        } while (jt.getAdjacents(root).size() > 1);

        Map<JunctionTreeNode<Node>, Set<CGPotential>> cliquePotentials = getChanceCliquePotentials(graph, jt);
        Map<Edge<JunctionTreeNode<Node>>, CGPotential> messages = getInitializedMessages(jt);

//        DirectedGraph<JunctionTreeNode<Node>> jtd = jt.getTreeSinkTo(root);
//        List<JunctionTreeNode<Node>> topologicalSorted = jtd.getTopologicalOrderedNodes();
//        performMessagePassing(jt, jtd, cliquePotentials, cliqueMarginals, topologicalSorted, messages, false);
//
//        jtd = jt.getTreeSourceFrom(root);
//
//        topologicalSorted = jtd.getTopologicalOrderedNodes();
//        performMessagePassing(jt, jtd, cliquePotentials, cliqueMarginals, topologicalSorted, messages, true);
    }

    private void performMessagePassing(UndirectedGraph<JunctionTreeNode<Node>> jt, DirectedGraph<JunctionTreeNode<Node>> jtd, //TODO not touched
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
                                               Map<JunctionTreeNode<Node>, Set<CGPotential>> chanceCliquePotentials,
                                               Map<JunctionTreeNode<Node>, Set<CGUtility>> utilityCliquePotentials,
                                               Map<Node, CGPotential> strategy, List<JunctionTreeNode<Node>> topologicalSorted,
                                               Map<JunctionTreeNode<Node>, CGPotential> chanceMessages,
                                               Map<JunctionTreeNode<Node>, CGUtility> utilityMessages,
                                               final Map<Node, Integer> temporalGroupNumber) {


        for (JunctionTreeNode<Node> jtNode : topologicalSorted) {
            if (jtNode instanceof CliqueNode) {
                logger.debug(" ======================================= ");
                logger.debug("jtNode = " + jtNode);
                Set<JunctionTreeNode<Node>> parents = sjt.getParents(jtNode);
                logger.debug("parents = " + parents);

                Set<CGPotential> nodeCliqueChanceMessages = new HashSet<CGPotential>(chanceCliquePotentials.get(jtNode));
                Set<CGUtility> nodeCliqueUtilityMessages = new HashSet<CGUtility>(utilityCliquePotentials.get(jtNode));

                logger.debug("nodeCliqueChanceMessages = " + nodeCliqueChanceMessages);
                logger.debug("nodeCliqueUtilityMessages = " + nodeCliqueUtilityMessages);

                Set<Node> continuousNodes = new HashSet<Node>();
                Map<Node, CGPotential> nodeCGPotentials = new HashMap<Node, CGPotential>();
                for (CGPotential c : nodeCliqueChanceMessages) {
                    continuousNodes.addAll(c.getHeadVariables());
                    continuousNodes.addAll(c.getTailVariables());
                    if (!c.getHeadVariables().isEmpty())
                        nodeCGPotentials.put(c.getHeadVariables().iterator().next(), c);
                }

                DirectedGraph<Node> subGraph = bayesianNetwork.getSubGraph(continuousNodes);
                List<Node> sortedNodes = subGraph.getTopologicalOrderedNodes();
                sortedNodes.retainAll(nodeCGPotentials.keySet());

                logger.debug("sortedNodes = " + sortedNodes);

                CGPotential cgChance;
                if (!sortedNodes.isEmpty()) {
                    cgChance = nodeCGPotentials.get(sortedNodes.get(0));
                    for (int i = 1; i < sortedNodes.size(); i++) {
                        cgChance = cgChance.directCombination(nodeCGPotentials.get(sortedNodes.get(i)));
                    }
                } else {
                    cgChance = nodeCliqueChanceMessages.iterator().next();
                }

                Iterator<CGUtility> utilityIterator = nodeCliqueUtilityMessages.iterator();
                CGUtility cgUtility = utilityIterator.next();

                while (utilityIterator.hasNext()) {
                    cgUtility = cgUtility.add(utilityIterator.next());
                }
                Set<CGPotential> nodeChanceMessages = new HashSet<CGPotential>();
                Set<CGUtility> nodeUtilityMessages = new HashSet<CGUtility>();
                for (JunctionTreeNode<Node> member : parents) {
                    nodeChanceMessages.add(chanceMessages.get(member));
                    nodeUtilityMessages.add(utilityMessages.get(member));
                }

                for (CGPotential cg : nodeChanceMessages)
                    cgChance = cgChance.recursiveCombination(cg);

                for (CGUtility cgu : nodeUtilityMessages)
                    cgUtility = cgUtility.add(cgu);

                logger.debug("cgChance = " + cgChance);
                logger.debug("cgUtility = " + cgUtility);

                Set<Node> projectionNodes;
                if (!sjt.getChildren(jtNode).isEmpty()) {
                    JunctionTreeNode<Node> child = sjt.getChildren(jtNode).iterator().next();
                    projectionNodes = child.getMembers();
                } else { // [sigh] we are in root now
                    projectionNodes = new HashSet<Node>();
                }

                LinkedHashSet<Node> expandedNodes = (LinkedHashSet<Node>) cgUtility.getContinuousVariables().clone();
                expandedNodes.addAll(cgChance.getTailVariables());
                cgUtility.expand(expandedNodes);
                expandedNodes.removeAll(cgChance.getHeadVariables());
                cgChance.expand(expandedNodes);

                Set<Node> eliminatedNodes = new HashSet<Node>(cgChance.getHeadVariables());
                eliminatedNodes.addAll(cgUtility.getContinuousVariables());
                eliminatedNodes.removeAll(projectionNodes);

                logger.debug("eliminatedNodes = " + eliminatedNodes);

                List<Node> orderedEliminatedNodes = Misc.asSortedList(eliminatedNodes, new Comparator<Node>() {
                    public int compare(Node o1, Node o2) {
                        return temporalGroupNumber.get(o2).compareTo(temporalGroupNumber.get(o1)); // sort descending
                    }
                });
                logger.debug("orderedEliminatedNodes = " + orderedEliminatedNodes);

                Set<Node> processedNodes = new HashSet<Node>();

                for (Node node : orderedEliminatedNodes) {
                    if (cgChance.getHeadVariables().contains(node)) {
                        LinkedHashSet<Node> headMarginalNodes = (LinkedHashSet<Node>) cgChance.getHeadVariables().clone();
                        headMarginalNodes.remove(node);
                        if (cgUtility.getContinuousVariables().contains(node)) {
                            CGPotential complement = cgChance.complement(headMarginalNodes);
                            expandedNodes = (LinkedHashSet<Node>) complement.getTailVariables().clone();
                            expandedNodes.addAll(cgUtility.getContinuousVariables());
                            cgUtility.expand(expandedNodes);
                            cgUtility = cgUtility.marginalizeContinuousChanceVariable(node, complement);
                        }
                        cgChance = cgChance.headMarginal(headMarginalNodes);
                        processedNodes.add(node);
                    }
                }

                orderedEliminatedNodes.removeAll(processedNodes);
                for (Node node : orderedEliminatedNodes) {
                    CGUtility.ContinuousDecisionMarginalAnswer a = cgUtility.marginalizeContinuousDecisionVariable(node);
                    cgUtility = a.getCgUtility();
//                    System.out.println("a.getIntercept() = " + a.getIntercept());
//                    System.out.println("a.getRegressionFactor() = " + a.getRegressionFactor());
                }

                cgChance.reduce();
                logger.debug("newCGChance = " + cgChance);
                logger.debug("newCGUtility = " + cgUtility);


                if (!sjt.getChildren(jtNode).isEmpty()) {
                    JunctionTreeNode<Node> child = sjt.getChildren(jtNode).iterator().next();
                    CGPotential chanceMessage = chanceMessages.get(child);
                    CGUtility utilityMessage = utilityMessages.get(child);
                    chanceMessages.put(child, chanceMessage.recursiveCombination(cgChance));
                    utilityMessages.put(child, utilityMessage.add(cgUtility));
                }

                if (projectionNodes.isEmpty())
                    logger.info("MEU = " + cgUtility.getS().getData()[0]);

            }
        }
    }

    private Map<Edge<JunctionTreeNode<Node>>, CGPotential> getInitializedMessages(AbstractGraph<JunctionTreeNode<Node>, ? extends AbstractVertex> jt) {
        Map<Edge<JunctionTreeNode<Node>>, CGPotential> messages = new HashMap<Edge<JunctionTreeNode<Node>>, CGPotential>();

        for (JunctionTreeNode<Node> jtNode : jt.getNodes()) {
            if (jtNode instanceof SeparatorNode)
                for (JunctionTreeNode<Node> adjacent : jt.getAdjacents(jtNode))
                    messages.put(new Edge<JunctionTreeNode<Node>>(jtNode, adjacent), CGPotential.unityPotential());
        }
        return messages;
    }

    private Map<JunctionTreeNode<Node>, CGUtility> getUtilityInitializedMessages(DirectedGraph<JunctionTreeNode<Node>> jt) {
        Map<JunctionTreeNode<Node>, CGUtility> messages = new HashMap<JunctionTreeNode<Node>, CGUtility>();

        for (JunctionTreeNode<Node> jtNode : jt.getNodes()) {
            if (jtNode instanceof SeparatorNode)
                messages.put(jtNode, CGUtility.zeroUtility());
        }
        return messages;
    }

    private Map<JunctionTreeNode<Node>, CGPotential> getChanceInitializedMessages(DirectedGraph<JunctionTreeNode<Node>> jt) {
        Map<JunctionTreeNode<Node>, CGPotential> messages = new HashMap<JunctionTreeNode<Node>, CGPotential>();

        for (JunctionTreeNode<Node> jtNode : jt.getNodes()) {
            if (jtNode instanceof SeparatorNode)
                messages.put(jtNode, CGPotential.unityPotential());
        }
        return messages;
    }

    private Map<JunctionTreeNode<Node>, Set<CGPotential>> getChanceCliquePotentials(DirectedGraph<Node> graph, AbstractGraph<JunctionTreeNode<Node>, ? extends AbstractVertex> jtree) {
        Map<JunctionTreeNode<Node>, Set<CGPotential>> cliquePotentials = new HashMap<JunctionTreeNode<Node>, Set<CGPotential>>();
        Set<Node> nodesCopy = new HashSet<Node>(graph.getNodes());
        for (JunctionTreeNode<Node> jtNode : jtree.getNodes()) {
            if (jtNode instanceof CliqueNode) {
                Set<Node> nodesToRemove = new HashSet<Node>();
                for (Node node : nodesCopy) {
                    if (node.getCategory() == Node.Category.Chance && jtNode.getMembers().containsAll(graph.getFamily(node))) {
                        CGPotential p = (CGPotential) getNodePotential(node);
                        if (cliquePotentials.containsKey(jtNode))
                            cliquePotentials.get(jtNode).add(p);
                        else {
                            Set<CGPotential> s = new HashSet<CGPotential>();
                            s.add(p);
                            cliquePotentials.put(jtNode, s);
                        }
                        nodesToRemove.add(node);
                    }
                }
                nodesCopy.removeAll(nodesToRemove);
                if (!cliquePotentials.containsKey(jtNode)) {
                    Set<CGPotential> s = new HashSet<CGPotential>();
                    s.add(CGPotential.unityPotential());
                    cliquePotentials.put(jtNode, s);
                }
            }
        }
        return cliquePotentials;
    }

    private Map<JunctionTreeNode<Node>, Set<CGUtility>> getUtilityCliquePotentials(DirectedGraph<Node> graph, AbstractGraph<JunctionTreeNode<Node>, ? extends AbstractVertex> jtree) {
        Map<JunctionTreeNode<Node>, Set<CGUtility>> cliquePotentials = new HashMap<JunctionTreeNode<Node>, Set<CGUtility>>();
        Set<Node> nodesCopy = new HashSet<Node>(graph.getNodes());
        for (JunctionTreeNode<Node> jtNode : jtree.getNodes()) {
            if (jtNode instanceof CliqueNode) {
                Set<Node> nodesToRemove = new HashSet<Node>();
                for (Node node : nodesCopy) {
                    if (node.getCategory() == Node.Category.Utility && jtNode.getMembers().containsAll(graph.getParents(node))) {
                        CGUtility p = (CGUtility) getNodePotential(node);
                        if (cliquePotentials.containsKey(jtNode))
                            cliquePotentials.get(jtNode).add(p);
                        else {
                            Set<CGUtility> s = new HashSet<CGUtility>();
                            s.add(p);
                            cliquePotentials.put(jtNode, s);
                        }
                        nodesToRemove.add(node);
                    }
                }
                nodesCopy.removeAll(nodesToRemove);
                if (!cliquePotentials.containsKey(jtNode)) {
                    Set<CGUtility> s = new HashSet<CGUtility>();
                    s.add(CGUtility.zeroUtility());
                    cliquePotentials.put(jtNode, s);
                }
            }
        }
        return cliquePotentials;
    }

}

