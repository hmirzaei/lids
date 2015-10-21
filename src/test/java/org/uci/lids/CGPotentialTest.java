package org.uci.lids;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.ejml.simple.SimpleMatrix;
import org.uci.lids.graph.DirectedGraph;
import org.uci.lids.graph.UndirectedGraph;
import org.uci.lids.utils.CGPotential;
import org.uci.lids.utils.MatrixPotential;
import org.uci.lids.utils.Misc;
import org.uci.lids.utils.Potential;

import java.util.*;

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

    public void testWeakMarginal() {
        d[1].setPotentialArray(0.2, 0.8);
        d[2].setPotentialArray(0.5, 0.5);
        c[1].setPotentialArray(10, 20, 30, 40, 1, 2, 3, 4);
        c[2].setPotentialArray(1, 2, 3, 4, 1, 2, 3, 4, 5, 6, 7, 8);
        DirectedGraph<Node> bn = new DirectedGraph<Node>();
        bn.addNodes(d[1], d[2], c[1], c[2]);
        bn.addLink(d[1], c[1]);
        bn.addLink(d[2], c[1]);
        bn.addLink(d[1], c[2]);
        bn.addLink(d[2], c[2]);
        bn.addLink(c[1], c[2]);

        // - Node CG potentials
        CGPotential phi_d1 = d[1].getCGPotential(bn);
        CGPotential phi_d2 = d[2].getCGPotential(bn);
        CGPotential phi_c1_d1d2 = c[1].getCGPotential(bn);
        CGPotential phi_c2_d1d2c1 = c[2].getCGPotential(bn);

        // - Initial Assignment
        CGPotential phi_d1d2c1c2 = phi_c2_d1d2c1.directCombination(phi_c1_d1d2)
                .directCombination(phi_d1).directCombination(phi_d2);

        CGPotential weak = phi_d1d2c1c2.weakMarginal(new LinkedHashSet<Node>());
        assertTrue(weak.getMeans().getData()[0].isIdentical(new SimpleMatrix(2, 1, false, 28, 92.8), 1e-10));
        assertTrue(Math.abs(weak.getVariances().getData()[0].get(0, 0) - 118.8) < 1e-10);
        assertTrue(Math.abs(weak.getVariances().getData()[0].get(1, 1) - 3767.56) < 1e-10);

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

    public void testDirectCombinationCornerCases() {
        CGPotential cg2;
        CGPotential cg3;
        CGPotential cgc1;
        CGPotential cgc2;

        cg2 = getCgPotential(new Node[]{d[1], d[2]}, new Node[]{c[1], c[2]}, new Node[]{c[3], c[4], c[5]});
        cg3 = getCgPotential(new Node[]{d[3], d[4]}, new Node[]{c[6], c[7], c[8]}, new Node[]{c[9], c[10]});
        cgc1 = cg2.directCombination(cg3);
        cgc2 = cg3.directCombination(cg2);
        cg2 = getCgPotential(new Node[]{d[1], d[2]}, new Node[]{c[1], c[2]}, new Node[]{});
        cg3 = getCgPotential(new Node[]{d[3], d[4]}, new Node[]{c[6], c[7], c[8]}, new Node[]{c[9], c[10]});
        cgc1 = cg2.directCombination(cg3);
        cgc2 = cg3.directCombination(cg2);
        cg2 = getCgPotential(new Node[]{d[1], d[2]}, new Node[]{c[1], c[2]}, new Node[]{c[3], c[4], c[5]});
        cg3 = getCgPotential(new Node[]{d[3], d[4]}, new Node[]{c[6], c[7], c[8]}, new Node[]{});
        cgc1 = cg2.directCombination(cg3);
        cgc2 = cg3.directCombination(cg2);
        cg2 = getCgPotential(new Node[]{d[1], d[2]}, new Node[]{c[1], c[2]}, new Node[]{});
        cg3 = getCgPotential(new Node[]{d[3], d[4]}, new Node[]{c[6], c[7], c[8]}, new Node[]{});
        cgc1 = cg2.directCombination(cg3);
        cgc2 = cg3.directCombination(cg2);
        try {
            cg2 = getCgPotential(new Node[]{d[1], d[2]}, new Node[]{}, new Node[]{c[3], c[4], c[5]});
            cg3 = getCgPotential(new Node[]{d[3], d[4]}, new Node[]{c[6], c[7], c[8]}, new Node[]{c[9], c[10]});
            cgc1 = cg2.directCombination(cg3);
            cgc2 = cg3.directCombination(cg2);
        } catch (AssertionError e) {
            assertTrue(e.getMessage().equals("No discrete potential can have continuous parents"));
        }
        cg2 = getCgPotential(new Node[]{d[1], d[2]}, new Node[]{}, new Node[]{});
        cg3 = getCgPotential(new Node[]{d[3], d[4]}, new Node[]{c[6], c[7], c[8]}, new Node[]{c[9], c[10]});
        cgc1 = cg2.directCombination(cg3);
        cgc2 = cg3.directCombination(cg2);
        try {
            cg2 = getCgPotential(new Node[]{d[1], d[2]}, new Node[]{}, new Node[]{c[3], c[4], c[5]});
            cg3 = getCgPotential(new Node[]{d[3], d[4]}, new Node[]{c[6], c[7], c[8]}, new Node[]{});
            cgc1 = cg2.directCombination(cg3);
            cgc2 = cg3.directCombination(cg2);
        } catch (AssertionError e) {
            assertTrue(e.getMessage().equals("No discrete potential can have continuous parents"));
        }
        cg2 = getCgPotential(new Node[]{d[1], d[2]}, new Node[]{}, new Node[]{});
        cg3 = getCgPotential(new Node[]{d[3], d[4]}, new Node[]{c[6], c[7], c[8]}, new Node[]{});
        cgc1 = cg2.directCombination(cg3);
        cgc2 = cg3.directCombination(cg2);
        try {
            cg2 = getCgPotential(new Node[]{d[1], d[2]}, new Node[]{c[1], c[2]}, new Node[]{c[3], c[4], c[5]});
            cg3 = getCgPotential(new Node[]{d[3], d[4]}, new Node[]{}, new Node[]{c[9], c[10]});
            cgc1 = cg2.directCombination(cg3);
            cgc2 = cg3.directCombination(cg2);
        } catch (AssertionError e) {
            assertTrue(e.getMessage().equals("No discrete potential can have continuous parents"));
        }
        try {
            cg2 = getCgPotential(new Node[]{d[1], d[2]}, new Node[]{c[1], c[2]}, new Node[]{});
            cg3 = getCgPotential(new Node[]{d[3], d[4]}, new Node[]{}, new Node[]{c[9], c[10]});
            cgc1 = cg2.directCombination(cg3);
            cgc2 = cg3.directCombination(cg2);
        } catch (AssertionError e) {
            assertTrue(e.getMessage().equals("No discrete potential can have continuous parents"));
        }
        cg2 = getCgPotential(new Node[]{d[1], d[2]}, new Node[]{c[1], c[2]}, new Node[]{c[3], c[4], c[5]});
        cg3 = getCgPotential(new Node[]{d[3], d[4]}, new Node[]{}, new Node[]{});
        cgc1 = cg2.directCombination(cg3);
        cgc2 = cg3.directCombination(cg2);
        cg2 = getCgPotential(new Node[]{d[1], d[2]}, new Node[]{c[1], c[2]}, new Node[]{});
        cg3 = getCgPotential(new Node[]{d[3], d[4]}, new Node[]{}, new Node[]{});
        cgc1 = cg2.directCombination(cg3);
        cgc2 = cg3.directCombination(cg2);
        try {
            cg2 = getCgPotential(new Node[]{d[1], d[2]}, new Node[]{}, new Node[]{c[3], c[4], c[5]});
            cg3 = getCgPotential(new Node[]{d[3], d[4]}, new Node[]{}, new Node[]{c[9], c[10]});
            cgc1 = cg2.directCombination(cg3);
            cgc2 = cg3.directCombination(cg2);
        } catch (AssertionError e) {
            assertTrue(e.getMessage().equals("No discrete potential can have continuous parents"));

        }
        try {
            cg2 = getCgPotential(new Node[]{d[1], d[2]}, new Node[]{}, new Node[]{});
            cg3 = getCgPotential(new Node[]{d[3], d[4]}, new Node[]{}, new Node[]{c[9], c[10]});
            cgc1 = cg2.directCombination(cg3);
            cgc2 = cg3.directCombination(cg2);
        } catch (AssertionError e) {
            assertTrue(e.getMessage().equals("No discrete potential can have continuous parents"));
        }

        try {
            cg2 = getCgPotential(new Node[]{d[1], d[2]}, new Node[]{}, new Node[]{c[3], c[4], c[5]});
            cg3 = getCgPotential(new Node[]{d[3], d[4]}, new Node[]{}, new Node[]{});
            cgc1 = cg2.directCombination(cg3);
            cgc2 = cg3.directCombination(cg2);
        } catch (AssertionError e) {
            assertTrue(e.getMessage().equals("No discrete potential can have continuous parents"));
        }

        cg2 = getCgPotential(new Node[]{d[1], d[2]}, new Node[]{}, new Node[]{});
        cg3 = getCgPotential(new Node[]{d[3], d[4]}, new Node[]{}, new Node[]{});
        cgc1 = cg2.directCombination(cg3);
        cgc2 = cg3.directCombination(cg2);
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
        }
    }


    // "Stable local computation ..." Lauritzen & Jensen Example 2
    public void testCGBNSolveByHand() {
        Node a = new Node(Node.VariableType.Categorical, Node.Category.Chance, "a");
        a.setStates(new String[]{"0", "1"});
        Node b = new Node(Node.VariableType.Continuous, Node.Category.Chance, "b");
        Node c = new Node(Node.VariableType.Continuous, Node.Category.Chance, "c");
        Node d = new Node(Node.VariableType.Continuous, Node.Category.Chance, "d");
        Node e = new Node(Node.VariableType.Continuous, Node.Category.Chance, "e");
        Node f = new Node(Node.VariableType.Continuous, Node.Category.Chance, "f");
        a.setPotentialArray(0.6, 0.4);
        b.setPotentialArray(1, 2, 3, 4, 5, 6);
        c.setPotentialArray(8, 7, 6, 7, 5, 6);
        d.setPotentialArray(13, 14, 15, 16, 17);
        e.setPotentialArray(6, 7, 2);
        f.setPotentialArray(3, 5);
        DirectedGraph<Node> bn = new DirectedGraph<Node>();
        bn.addNodes(a, b, c, d, e, f);
        bn.addLink(a, b);
        bn.addLink(d, b);
        bn.addLink(a, c);
        bn.addLink(e, c);
        bn.addLink(c, d);
        bn.addLink(e, d);
        bn.addLink(f, d);
        bn.addLink(f, e);
        Misc.saveGraphOnDisk("bn", bn);
        List<Set<Node>> tempOrder = Arrays.asList(
                (Set<Node>) new HashSet<Node>(Collections.singletonList(a)),
                new HashSet<Node>(Arrays.asList(b, c, d, e, f)));

        // Junction tree Setup, one valid result is: (c,e,f,d) -> [c,e,d] -> (a,c,d,e) <- [d,a] <- (b,d,a)

        UndirectedGraph<Node> moralized = bn.getMoralizedUndirectedCopy();
        moralized.triangulate(tempOrder);
        UndirectedGraph<Node>.JunctionTreeAndRoot junctionTreeAndRoot = moralized.getJunctionTree(tempOrder);
        Misc.saveGraphOnDisk("jtree", junctionTreeAndRoot.junctionTree);
        assertTrue((junctionTreeAndRoot.rootClique.getMembers().containsAll(Arrays.asList(a, b, d)) &&
                junctionTreeAndRoot.rootClique.getMembers().size() == 3)
                || (junctionTreeAndRoot.rootClique.getMembers().containsAll(Arrays.asList(a, c, d, e)) &&
                junctionTreeAndRoot.rootClique.getMembers().size() == 4));

        // - Node CG potentials
        CGPotential phi_a = a.getCGPotential(bn);
        CGPotential phi_b_ad = b.getCGPotential(bn);
        CGPotential phi_c_ae = c.getCGPotential(bn);
        CGPotential phi_d_cef = d.getCGPotential(bn);
        CGPotential phi_e_f = e.getCGPotential(bn);
        CGPotential phi_f = f.getCGPotential(bn);

        // - Initial Assignment
        CGPotential phi_cefd = phi_d_cef.directCombination(phi_e_f).directCombination(phi_f); //TODO _e  effect
        CGPotential phi_acde = phi_a.directCombination(phi_c_ae);
        CGPotential phi_bda = phi_b_ad;

        // - Collecting messages at the root
        // cefd -> acde
        CGPotential phi_cefd_hm_ed = phi_cefd.headMarginal(new LinkedHashSet<Node>(Arrays.asList(e, d)));
        phi_acde = phi_acde.recursiveCombination(phi_cefd_hm_ed);
        phi_cefd = phi_cefd.complement(new LinkedHashSet<Node>(Arrays.asList(e, d)));

        // bda -> acde
        // phi_acde will have the strong marginal of the root nodes
        CGPotential phi_bda_hm_da = phi_bda.headMarginal(new LinkedHashSet<Node>());
        phi_acde = phi_acde.recursiveCombination(phi_bda_hm_da);
        phi_bda = phi_bda.complement(new LinkedHashSet<Node>());

        // bonus step: strong marginal of the all nodes (equation (7) in the paper)
        CGPotential cc = phi_bda.recursiveCombination(phi_cefd).recursiveCombination(phi_acde);

        // - Distributing messages from the root
        // acde -> cefd
        CGPotential phi_ced = phi_acde.headMarginal(new LinkedHashSet<Node>(Arrays.asList(c, e, d)));
        phi_ced = phi_ced.weakMarginal(new LinkedHashSet<Node>());
        CGPotential phi_cefd_weak = phi_cefd.directCombination(phi_ced);

        // acde -> bda
        CGPotential phi_da = phi_acde.headMarginal(new LinkedHashSet<Node>(Collections.singletonList(d)));
        phi_da = phi_da.weakMarginal(new LinkedHashSet<Node>(Collections.singletonList(a)));
        CGPotential phi_bda_weak = phi_bda.directCombination(phi_da);

    }


}
