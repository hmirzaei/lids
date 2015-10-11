package org.uci.lids;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.ejml.simple.SimpleMatrix;
import org.uci.lids.utils.CGPotential;
import org.uci.lids.utils.MatrixPotential;
import org.uci.lids.utils.Potential;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Random;

/**
 * Unit test for simple Example.
 */
public class CGPotentialTest
        extends TestCase {
    Node d1 = new Node(Node.VariableType.Categorical, Node.Category.Chance, "d1");
    Node d2 = new Node(Node.VariableType.Categorical, Node.Category.Chance, "d2");
    Node d3 = new Node(Node.VariableType.Categorical, Node.Category.Chance, "d3");
    Node d4 = new Node(Node.VariableType.Categorical, Node.Category.Chance, "d4");
    Node d5 = new Node(Node.VariableType.Categorical, Node.Category.Chance, "d5");
    Node d6 = new Node(Node.VariableType.Categorical, Node.Category.Chance, "d6");
    Node c1 = new Node(Node.VariableType.Continuous, Node.Category.Chance, "c1");
    Node c2 = new Node(Node.VariableType.Continuous, Node.Category.Chance, "c2");
    Node c3 = new Node(Node.VariableType.Continuous, Node.Category.Chance, "c3");
    Node c4 = new Node(Node.VariableType.Continuous, Node.Category.Chance, "c4");
    Node c5 = new Node(Node.VariableType.Continuous, Node.Category.Chance, "c5");
    Node c6 = new Node(Node.VariableType.Continuous, Node.Category.Chance, "c6");
    Node c7 = new Node(Node.VariableType.Continuous, Node.Category.Chance, "c7");
    Node c8 = new Node(Node.VariableType.Continuous, Node.Category.Chance, "c8");
    Node c9 = new Node(Node.VariableType.Continuous, Node.Category.Chance, "c9");
    Node c10 = new Node(Node.VariableType.Continuous, Node.Category.Chance, "c10");
    Node c11 = new Node(Node.VariableType.Continuous, Node.Category.Chance, "c11");
    Node[] head = new Node[]{c1, c2, c3};
    Node[] tail = new Node[]{c4, c5};
    Node[] discreteVars = new Node[]{d1, d2, d3};
    CGPotential cg1;

    {

        d1.setStates(new String[]{"0", "1"});
        d2.setStates(new String[]{"0", "1"});
        d3.setStates(new String[]{"0", "1"});
        d4.setStates(new String[]{"0", "1"});
        d5.setStates(new String[]{"0", "1"});
        d6.setStates(new String[]{"0", "1"});
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

    /**
     * Rigourous Test :-)
     */
    public void testHeadMarginal() {

        LinkedHashSet<Node> h2 = new LinkedHashSet<Node>(Arrays.asList(new Node[]{c3, c1}));
        CGPotential cg2 = this.cg1.headMarginal(h2);

        assertTrue(cg2.getHeadVariables().size() == 2);
        assertTrue(cg2.getHeadVariables().containsAll(Arrays.asList(c3, c1)));

        assertTrue(cg2.getTailVariables().containsAll(this.cg1.getTailVariables()));
        assertTrue(this.cg1.getTailVariables().containsAll(cg2.getTailVariables()));

        assert cg2.getVariances().getData()[0].get(0, 0) ==
                this.cg1.getVariances().getData()[0].get(2, 2);
    }

    public void testExpand() {
        LinkedHashSet<Node> t2 = new LinkedHashSet<Node>(Arrays.asList(new Node[]{c5, c6, c4}));
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
            assertTrue(temp.getTailVariables().contains(c5));
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
        CGPotential cg2 = getCgPotential(new Node[]{d4, d5}, new Node[]{c6, c7}, new Node[]{c3, c4, c8});
        CGPotential cg3 = getCgPotential(new Node[]{d1, d3, d6}, new Node[]{c9, c10, c11}, new Node[]{c1, c2, c6, c4, c8});
        CGPotential cgc1 = cg1.directCombination(cg2).directCombination(cg3);
        CGPotential cgc2 = cg1.directCombination(cg2.directCombination(cg3));
        assertTrue(cgc1.equals(cgc2));

    }

    public void testComplement() {
        LinkedHashSet<Node> h1 = new LinkedHashSet<Node>(Arrays.asList(c1, c2));
        CGPotential cg_c = cg1.complement(h1);
        CGPotential cg1Reconstructed = cg1.headMarginal(h1).directCombination(cg_c);

        assertTrue(cg1.equals(cg1Reconstructed));

    }

    public void testRecursiveCombination() {
        CGPotential cg1 = getCgPotential(new Node[]{d1, d2}, new Node[]{c1, c2}, new Node[]{c3, c4, c6});
        CGPotential cg2 = getCgPotential(new Node[]{d3, d4}, new Node[]{c6, c7}, new Node[]{c2, c4, c5});
        try {
            System.out.println(cg1.recursiveCombination(cg2));
        } catch (AssertionError e) {
            System.out.println(e.getMessage());
        }
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


}
