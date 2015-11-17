package org.uci.lids;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.apache.log4j.Logger;
import org.ejml.simple.SimpleMatrix;
import org.uci.lids.graph.DirectedGraph;
import org.uci.lids.utils.CGPotential;
import org.uci.lids.utils.CGUtility;
import org.uci.lids.utils.Misc;

import java.util.*;

/**
 * Unit test for simple Example.
 */
public class LQCGInfluenceDiagramTest
        extends TestCase {

    final static Logger logger = Logger.getLogger(LQCGInfluenceDiagram.class);

    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public LQCGInfluenceDiagramTest(String testName) {
        super(testName);
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite() {
        return new TestSuite(LQCGInfluenceDiagramTest.class);
    }

    private static Node createNode(Node.Category category, int i) {
        Node node;
        node = new Node(Node.VariableType.Continuous, category, Integer.toString(i));
        return node;
    }

    public void testInfluenceDiagramSolving() {
        List<Node> nodes = new ArrayList<Node>();
        int N = 100;
        DirectedGraph<Node> bn = new DirectedGraph<Node>();

        logger.info("Start Creating Graph");
        createGraph(nodes, N, bn);

        logger.info("Start Writing Graph html");
        Misc.saveGraphOnDisk("graph", bn);

        logger.info("Start Filling potentials");
        FillPotentialArrays(nodes, bn);
        logger.info("Start Writing CVX Script");
        Misc.writeCvxScript("cvxscript.m", bn, nodes);

        logger.info("Start Solving LQCG ID");
        LQCGInfluenceDiagram lid = new LQCGInfluenceDiagram(bn);
        lid.getOptimalStrategy();
    }

    private void createGraph(List<Node> nodes, int NumberOfNodes, DirectedGraph<Node> bn) {
        Random r = new Random(30);
        for (int i = 0; i < NumberOfNodes; i++) {
            double randDouble = r.nextDouble();
            Node node;
            if (randDouble < 1d / 2)
                node = createNode(Node.Category.Chance, i);
            else if (randDouble < 5d / 6)
                node = createNode(Node.Category.Decision, i);
            else
                node = createNode(Node.Category.Utility, i);
            nodes.add(node);
            bn.addNode(node);
        }

        for (int i = 0; i < NumberOfNodes; i++) {
            for (int j = i + 1; j < NumberOfNodes; j++) {
                if (nodes.get(i).getCategory() != Node.Category.Utility)
                    bn.addLink(nodes.get(i), nodes.get(j));
            }
        }
        for (int k = 0; k < 1.5 * (NumberOfNodes * NumberOfNodes); k++) {
            int i = r.nextInt(NumberOfNodes);
            int j = r.nextInt(NumberOfNodes);
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
                    Node newNode = createNode(Node.Category.Chance, NumberOfNodes++);
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


        List<Set<Node>> wholeNetworkTemporalOrder = LQCGInfluenceDiagram.getTemporalOrder(bn);
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
    }

    private void FillPotentialArrays(List<Node> nodes, DirectedGraph<Node> bn) {
        for (Node n : nodes) {
            if (n.getCategory() == Node.Category.Chance) {
                CGPotential c = CGPotentialTest.getCgPotential(new Node[]{}, new Node[]{n},
                        bn.getParents(n).toArray(new Node[bn.getParents(n).size()]));
                List<Double> data = new ArrayList<Double>();
                for (SimpleMatrix m : c.getMeans().getData()) {
                    data.add(m.get(0, 0));
                }
                for (SimpleMatrix m : c.getRegressionCoefficients().getData()) {
                    for (double d : m.getMatrix().getData())
                        data.add(d);
                }
                for (SimpleMatrix m : c.getVariances().getData()) {
                    data.add(m.get(0, 0));
                }
                n.setPotentialArray(new double[data.size()]);
                for (int i = 0; i < data.size(); i++) {
                    n.getPotentialArray()[i] = data.get(i);
                }
            } else if (n.getCategory() == Node.Category.Utility) {
                CGUtility c = CGUtilityTest.getCgUtility(new Node[]{},
                        bn.getParents(n).toArray(new Node[bn.getParents(n).size()]));
                List<Double> data = new ArrayList<Double>();
                for (SimpleMatrix m : c.getQ().getData()) {
                    for (double d : m.getMatrix().getData())
                        data.add(d);
                }
                for (SimpleMatrix m : c.getR().getData()) {
                    for (double d : m.getMatrix().getData())
                        data.add(d);
                }
                for (double d : c.getS().getData()) {
                    data.add(d);
                }
                n.setPotentialArray(new double[data.size()]);
                for (int i = 0; i < data.size(); i++) {
                    n.getPotentialArray()[i] = data.get(i);
                }
            }
        }
    }

}
