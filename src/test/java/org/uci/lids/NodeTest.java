package org.uci.lids;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.ejml.simple.SimpleMatrix;
import org.uci.lids.graph.DirectedGraph;
import org.uci.lids.utils.CGPotential;

import java.util.Arrays;
import java.util.Collections;

/**
 * Unit test for simple Example.
 */
public class NodeTest
        extends TestCase {
    Node d[] = new Node[7];
    Node c[] = new Node[12];

    {
        for (int i = 0; i < d.length; i++) {
            d[i] = new Node(Node.VariableType.Categorical, Node.Category.Chance, "d" + i);
            d[i].setStates(new String[]{"0", "1"});

        }
        for (int i = 0; i < c.length; i++) {
            c[i] = new Node(Node.VariableType.Continuous, Node.Category.Chance, "c" + i);
        }
    }

    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public NodeTest(String testName) {
        super(testName);
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite() {
        return new TestSuite(NodeTest.class);
    }

    public void testGetCGPotential() {
        d[1].setPotentialArray(0.2, 0.8);
        d[2].setPotentialArray(0.5, 0.5);
        c[3].setPotentialArray(new double[16]);
        for (int i = 0; i < c[3].getPotentialArray().length; i++) {
            c[3].getPotentialArray()[i] = i + 1;
        }
        DirectedGraph<Node> bn = new DirectedGraph<Node>();
        bn.addNode(d[1]);
        bn.addNode(d[2]);
        bn.addNode(c[1]);
        bn.addNode(c[2]);
        bn.addNode(c[3]);
        bn.addLink(c[1], c[3]);
        bn.addLink(c[2], c[3]);
        bn.addLink(d[1], c[3]);
        bn.addLink(d[2], c[3]);
        CGPotential cg = c[3].getCGPotential(bn);
        assertTrue(cg.getDiscretePotential().getVariables().containsAll(Arrays.asList(d[1], d[2])));
        assertTrue(cg.getDiscretePotential().getVariables().size() == 2);
        assertTrue(cg.getHeadVariables().containsAll(Collections.singletonList(c[3])));
        assertTrue(cg.getHeadVariables().size() == 1);
        assertTrue(cg.getTailVariables().containsAll(Arrays.asList(c[1], c[2])));
        assertTrue(cg.getTailVariables().size() == 2);
        assertTrue(cg.getMeans().getData()[0].isIdentical(new SimpleMatrix(1, 1, true, 1), 1e-10));
        assertTrue(cg.getMeans().getData()[1].isIdentical(new SimpleMatrix(1, 1, true, 2), 1e-10));
        assertTrue(cg.getMeans().getData()[2].isIdentical(new SimpleMatrix(1, 1, true, 3), 1e-10));
        assertTrue(cg.getMeans().getData()[3].isIdentical(new SimpleMatrix(1, 1, true, 4), 1e-10));
        assertTrue(cg.getRegressionCoefficients().getData()[0].isIdentical(new SimpleMatrix(1, 2, true, 5, 6), 1e-10));
        assertTrue(cg.getRegressionCoefficients().getData()[1].isIdentical(new SimpleMatrix(1, 2, true, 7, 8), 1e-10));
        assertTrue(cg.getRegressionCoefficients().getData()[2].isIdentical(new SimpleMatrix(1, 2, true, 9, 10), 1e-10));
        assertTrue(cg.getRegressionCoefficients().getData()[3].isIdentical(new SimpleMatrix(1, 2, true, 11, 12), 1e-10));
        assertTrue(cg.getVariances().getData()[0].isIdentical(new SimpleMatrix(1, 1, true, 13), 1e-10));
        assertTrue(cg.getVariances().getData()[1].isIdentical(new SimpleMatrix(1, 1, true, 14), 1e-10));
        assertTrue(cg.getVariances().getData()[2].isIdentical(new SimpleMatrix(1, 1, true, 15), 1e-10));
        assertTrue(cg.getVariances().getData()[3].isIdentical(new SimpleMatrix(1, 1, true, 16), 1e-10));
    }

    public void testGetCGPotentialNoContinuousParents() {
        d[1].setPotentialArray(0.2, 0.8);
        d[2].setPotentialArray(0.5, 0.5);
        c[1].setPotentialArray(1, 2, 3, 4, 10, 20, 30, 40);
        DirectedGraph<Node> bn = new DirectedGraph<Node>();
        bn.addNode(d[1]);
        bn.addNode(d[2]);
        bn.addNode(c[1]);
        bn.addLink(d[1], c[1]);
        bn.addLink(d[2], c[1]);
        CGPotential cg = c[1].getCGPotential(bn);
        assertTrue(cg.getDiscretePotential().getVariables().containsAll(Arrays.asList(d[1], d[2])));
        assertTrue(cg.getDiscretePotential().getVariables().size() == 2);
        assertTrue(cg.getHeadVariables().containsAll(Collections.singletonList(c[1])));
        assertTrue(cg.getHeadVariables().size() == 1);
        assertTrue(cg.getTailVariables().size() == 0);
        assertTrue(cg.getMeans().getData()[0].isIdentical(new SimpleMatrix(1, 1, true, 1), 1e-10));
        assertTrue(cg.getMeans().getData()[1].isIdentical(new SimpleMatrix(1, 1, true, 2), 1e-10));
        assertTrue(cg.getMeans().getData()[2].isIdentical(new SimpleMatrix(1, 1, true, 3), 1e-10));
        assertTrue(cg.getMeans().getData()[3].isIdentical(new SimpleMatrix(1, 1, true, 4), 1e-10));
        assertTrue(cg.getVariances().getData()[0].isIdentical(new SimpleMatrix(1, 1, true, 10), 1e-10));
        assertTrue(cg.getVariances().getData()[1].isIdentical(new SimpleMatrix(1, 1, true, 20), 1e-10));
        assertTrue(cg.getVariances().getData()[2].isIdentical(new SimpleMatrix(1, 1, true, 30), 1e-10));
        assertTrue(cg.getVariances().getData()[3].isIdentical(new SimpleMatrix(1, 1, true, 40), 1e-10));
    }
}
