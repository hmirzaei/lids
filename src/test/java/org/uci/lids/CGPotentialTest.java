package org.uci.lids;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.ejml.simple.SimpleMatrix;
import org.uci.lids.graph.DirectedGraph;
import org.uci.lids.utils.CGPotential;
import org.uci.lids.utils.MatrixPotential;
import org.uci.lids.utils.Misc;
import org.uci.lids.utils.Potential;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Random;

/**
 * Unit test for simple Example.
 */
public class CGPotentialTest
        extends TestCase {
    Node d[] = new Node[7];
    Node c[] = new Node[12];
    CGPotential cg1;

    {
        for (int i = 0; i < d.length; i++) {
            d[i] = new Node(Node.VariableType.Categorical, Node.Category.Chance, "d" + i);
            d[i].setStates(new String[]{"0", "1"});

        }
        for (int i = 0; i < c.length; i++) {
            c[i] = new Node(Node.VariableType.Continuous, Node.Category.Chance, "c" + i);
        }
        Node[] head = new Node[]{c[1], c[2], c[3]};
        Node[] tail = new Node[]{c[4], c[5]};
        Node[] discreteVars = new Node[]{d[1], d[2], d[3]};
        cg1 = getCgPotential(discreteVars, head, tail);
    }

    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public CGPotentialTest(String testName) {
        super(testName);
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite() {
        return new TestSuite(CGPotentialTest.class);
    }

    private static CGPotential getCgPotential(Node[] discreteVars, Node[] head, Node[] tail) {
        LinkedHashSet<Node> head2 = new LinkedHashSet<Node>(new LinkedHashSet<Node>(Arrays.asList(head)));
        LinkedHashSet<Node> tail2 = new LinkedHashSet<Node>(new LinkedHashSet<Node>(Arrays.asList(tail)));
        LinkedHashSet<Node> discreteVars2 = new LinkedHashSet<Node>(new LinkedHashSet<Node>(Arrays.asList(discreteVars)));
        Random r = new Random();
        double[] potData = new double[1 << discreteVars.length];
        for (int i = 0; i < potData.length; i++) {
            potData[i] = r.nextDouble();
        }
        SimpleMatrix[] meansData = new SimpleMatrix[1 << discreteVars.length];
        for (int i = 0; i < meansData.length; i++) {
            double[] data = new double[head.length];
            for (int j = 0; j < data.length; j++) {
                data[j] = r.nextDouble();
            }
            meansData[i] = new SimpleMatrix(head.length, 1, false, data);
        }
        SimpleMatrix[] coeffsData = new SimpleMatrix[1 << discreteVars.length];
        for (int i = 0; i < coeffsData.length; i++) {
            double[] data = new double[head.length * tail.length];
            for (int j = 0; j < data.length; j++) {
                data[j] = r.nextDouble();
            }
            coeffsData[i] = new SimpleMatrix(head.length, tail.length, false, data);
        }
        SimpleMatrix[] variancesData = new SimpleMatrix[1 << discreteVars.length];
        for (int i = 0; i < variancesData.length; i++) {
            double[] data = new double[head.length * head.length];
            for (int j = 0; j < data.length; j++) {
                data[j] = r.nextDouble();
            }
            variancesData[i] = new SimpleMatrix(head.length, head.length, false, data);
            variancesData[i] = variancesData[i].mult(variancesData[i].transpose());
        }
        Potential p = new Potential(discreteVars2, potData);
        MatrixPotential means = new MatrixPotential(discreteVars2, meansData);
        MatrixPotential coeffs = new MatrixPotential(discreteVars2, coeffsData);
        MatrixPotential variances = new MatrixPotential(discreteVars2, variancesData);
        return new CGPotential(discreteVars2, head2, tail2, p, means, coeffs, variances);
    }

    public void testHeadMarginal() {

        LinkedHashSet<Node> h2 = new LinkedHashSet<Node>(Arrays.asList(new Node[]{c[3], c[1]}));
        CGPotential cg2 = this.cg1.headMarginal(h2);

        assertTrue(cg2.getHeadVariables().size() == 2);
        assertTrue(cg2.getHeadVariables().containsAll(Arrays.asList(c[3], c[1])));

        assertTrue(cg2.getTailVariables().containsAll(this.cg1.getTailVariables()));
        assertTrue(this.cg1.getTailVariables().containsAll(cg2.getTailVariables()));

        assert cg2.getVariances().getData()[0].get(0, 0) ==
                this.cg1.getVariances().getData()[0].get(2, 2);
    }

    public void testExpand() {
        LinkedHashSet<Node> t2 = new LinkedHashSet<Node>(Arrays.asList(new Node[]{c[5], c[6], c[4]}));
        try {
            CGPotential temp = (CGPotential) cg1.clone();
            temp.expand(t2);
            assertTrue(temp.getTailVariables().size() == 3);
            assertTrue(temp.getTailVariables().containsAll(t2));
            for (int i = 0; i < temp.getRegressionCoefficients().getData().length; i++) {
                assertTrue(temp.getRegressionCoefficients().getData()[i].extractVector(false, 0).isIdentical(
                        cg1.getRegressionCoefficients().getData()[i].extractVector(false, 1), 1e-10));
                assertTrue(temp.getRegressionCoefficients().getData()[i].extractVector(false, 2).isIdentical(
                        cg1.getRegressionCoefficients().getData()[i].extractVector(false, 0), 1e-10));
                assertTrue(temp.getRegressionCoefficients().getData()[i].extractVector(false, 1).isIdentical(
                        new SimpleMatrix(3, 1), 1e-10));
            }
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }

    }

    public void testReduce() {
        try {
            CGPotential temp = (CGPotential) cg1.clone();
            for (int i = 0; i < temp.getRegressionCoefficients().getData().length; i++) {
                temp.getRegressionCoefficients().getData()[i].insertIntoThis(0, 0, new SimpleMatrix(3, 1));
            }
            temp.reduce();
            assertTrue(temp.getTailVariables().size() == 1);
            assertTrue(temp.getTailVariables().contains(c[5]));
            for (int i = 0; i < temp.getRegressionCoefficients().getData().length; i++) {
                assertTrue(temp.getRegressionCoefficients().getData()[i].numCols() == 1);
                assertTrue(temp.getRegressionCoefficients().getData()[i].extractVector(false, 0).isIdentical(
                        cg1.getRegressionCoefficients().getData()[i].extractVector(false, 1), 1e-10));
            }

        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
    }

    public void testDirectCombination() {
        CGPotential cg2 = getCgPotential(new Node[]{d[4], d[5]}, new Node[]{c[6], c[7]}, new Node[]{c[3], c[4], c[8]});
        CGPotential cg3 = getCgPotential(new Node[]{d[1], d[3], d[6]}, new Node[]{c[9], c[10], c[11]}, new Node[]{c[1], c[2], c[6], c[4], c[8]});
        CGPotential cgc1 = cg1.directCombination(cg2).directCombination(cg3);
        CGPotential cgc2 = cg1.directCombination(cg2.directCombination(cg3));
        assertTrue(cgc1.equals(cgc2));

    }

    public void testComplement() {
        LinkedHashSet<Node> h1 = new LinkedHashSet<Node>(Arrays.asList(c[1], c[2]));
        CGPotential cg_c = cg1.complement(h1);
        CGPotential cg1Reconstructed = cg1.headMarginal(h1).directCombination(cg_c);

        assertTrue(cg1.equals(cg1Reconstructed));

    }

    public void testRecursiveCombination() {
        CGPotential cg1 = getCgPotential(new Node[]{d[1], d[2]}, new Node[]{c[1], c[2]}, new Node[]{c[3], c[4], c[6]});
        CGPotential cg2 = getCgPotential(new Node[]{d[3], d[4]}, new Node[]{c[6], c[7]}, new Node[]{c[2], c[4], c[5]});
        try {
            System.out.println(cg1.recursiveCombination(cg2));
        } catch (AssertionError e) {
            System.out.println(e.getMessage());
        }
    }

    public void testSimpleCGBN() {
        d[1].setPotentialArray(0.2, 0.8);
        d[2].setPotentialArray(0.5, 0.5);
        DirectedGraph<Node> bn = new DirectedGraph<Node>();
        bn.addNode(d[1]);
        bn.addNode(d[2]);
        bn.addNode(c[1]);
        bn.addLink(d[1], c[1]);
        bn.addLink(d[2], c[1]);
        Misc.saveGraphOnDisk("bn", bn);

    }


}
