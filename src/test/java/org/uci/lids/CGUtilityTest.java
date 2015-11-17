package org.uci.lids;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.ejml.simple.SimpleMatrix;
import org.uci.lids.utils.CGUtility;
import org.uci.lids.utils.MatrixPotential;
import org.uci.lids.utils.Potential;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Random;

/**
 * Unit test for simple Example.
 */
public class CGUtilityTest
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
    public CGUtilityTest(String testName) {
        super(testName);
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite() {
        return new TestSuite(CGUtilityTest.class);
    }

    public static CGUtility getCgUtility(Node[] discreteVars, Node[] continuousVars) {
        LinkedHashSet<Node> continuousVars2 = new LinkedHashSet<Node>(new LinkedHashSet<Node>(Arrays.asList(continuousVars)));
        LinkedHashSet<Node> discreteVars2 = new LinkedHashSet<Node>(new LinkedHashSet<Node>(Arrays.asList(discreteVars)));
        Random r = new Random(1);
        double[] potData = new double[1 << discreteVars.length];
        for (int i = 0; i < potData.length; i++) {
            potData[i] = r.nextDouble();
        }
        SimpleMatrix[] qData = new SimpleMatrix[1 << discreteVars.length];
        for (int i = 0; i < qData.length; i++) {
            double[] data = new double[continuousVars.length * continuousVars.length];
            for (int j = 0; j < data.length; j++) {
                data[j] = r.nextDouble();
            }
            qData[i] = new SimpleMatrix(continuousVars.length, continuousVars.length, false, data);
            qData[i] = qData[i].mult(qData[i].transpose()).scale(-1);
            qData[i] = SimpleMatrix.identity(continuousVars.length).scale(-1);
        }
        SimpleMatrix[] rData = new SimpleMatrix[1 << discreteVars.length];
        for (int i = 0; i < rData.length; i++) {
            double[] data = new double[continuousVars.length];
            for (int j = 0; j < data.length; j++) {
                data[j] = r.nextDouble();
            }
            rData[i] = new SimpleMatrix(1, continuousVars.length, false, data);
        }
        double[] sData = new double[1 << discreteVars.length];
        for (int i = 0; i < sData.length; i++) {
            sData[i] = r.nextDouble();
        }
        MatrixPotential Q = new MatrixPotential(discreteVars2, qData);
        MatrixPotential R = new MatrixPotential(discreteVars2, rData);
        Potential S = new Potential(discreteVars2, sData);
        return new CGUtility(discreteVars2, continuousVars2, Q, R, S);
    }

    public void testMarginalizeContinuousDecisionVariable() {
        Node[] continuousVaars = new Node[]{c[1], c[2], c[3]};
        Node[] discreteVars = new Node[]{d[1], d[2], d[3]};
        CGUtility cgu1 = getCgUtility(discreteVars, continuousVaars);

        CGUtility.ContinuousDecisionMarginalAnswer cgu1m = cgu1.marginalizeContinuousDecisionVariable(c[1]);
    }

}
