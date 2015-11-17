package org.uci.lids.utils;

import org.ejml.simple.SimpleMatrix;
import org.uci.lids.Node;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * Created by hamid on 10/3/15.
 */
public class CGUtility {
    private LinkedHashSet<Node> discreteVariables;
    private LinkedHashSet<Node> continuousVariables;
    private MatrixPotential Q;
    private MatrixPotential R;
    private Potential S;

    public CGUtility(LinkedHashSet<Node> discreteVariables, LinkedHashSet<Node> continuousVariables,
                     MatrixPotential q, MatrixPotential r, Potential s) {
        this.discreteVariables = discreteVariables;
        this.continuousVariables = continuousVariables;
        this.Q = q;
        this.R = r;
        this.S = s;
    }

    public static CGUtility zeroUtility() {
        return new CGUtility(new LinkedHashSet<Node>(), new LinkedHashSet<Node>(),
                new MatrixPotential(new LinkedHashSet<Node>()),
                new MatrixPotential(new LinkedHashSet<Node>()), Potential.zeroPotential());
    }

    public LinkedHashSet<Node> getDiscreteVariables() {
        return discreteVariables;
    }

    public LinkedHashSet<Node> getContinuousVariables() {
        return continuousVariables;
    }

    public MatrixPotential getQ() {
        return Q;
    }

    public MatrixPotential getR() {
        return R;
    }

    public Potential getS() {
        return S;
    }

    public ContinuousDecisionMarginalAnswer marginalizeContinuousDecisionVariable(Node x) {
        try {
            CGUtility cgu1 = (CGUtility) this.clone();
            List<Node> contVars = new ArrayList<Node>(cgu1.continuousVariables);
            int xIndex = contVars.indexOf(x);
            contVars.remove(x);
            int[] zIndecis = new int[contVars.size()];
            for (int i = 0; i < xIndex; i++)
                zIndecis[i] = i;
            for (int i = xIndex; i < zIndecis.length; i++)
                zIndecis[i] = i + 1;


            MatrixPotential q2 = new MatrixPotential(cgu1.discreteVariables);
            MatrixPotential r2 = new MatrixPotential(cgu1.discreteVariables);
            Potential s2 = new Potential(cgu1.discreteVariables);
            MatrixPotential regFactor = new MatrixPotential(cgu1.discreteVariables);
            double[] intercept = new double[q2.getTotalSize()];


            for (int i = 0; i < q2.getTotalSize(); i++) {
                SimpleMatrix q = cgu1.getQ().getData()[i];
                SimpleMatrix r = cgu1.getR().getData()[i];
                double s = cgu1.getS().getData()[i];
                double qxx = q.get(xIndex, xIndex);
                SimpleMatrix qzx = MatrixUtils.subMatrix(q, zIndecis, new int[]{xIndex});
                SimpleMatrix qxz = MatrixUtils.subMatrix(q, new int[]{xIndex}, zIndecis);
                SimpleMatrix qzz = MatrixUtils.subMatrix(q, zIndecis, zIndecis);
                double rx = r.get(0, xIndex);
                SimpleMatrix rz = MatrixUtils.subMatrix(r, new int[]{0}, zIndecis);

                if (qxx != 0) {
                    q2.getData()[i] = qzx.mult(qxz).scale(-1 / qxx).plus(qzz);
                    r2.getData()[i] = qxz.scale(-rx / qxx).plus(rz);
                    s2.set(i, -rx * rx / (4 * qxx) + s);

                    regFactor.getData()[i] = qxz.scale(-1 / qxx);
                    intercept[i] = -rx / (2 * qxx);
                } else {
                    q2.getData()[i] = qzx.mult(qxz).scale(0).plus(qzz);
                    r2.getData()[i] = qxz.scale(0).plus(rz);
                    s2.set(i, s);

                    regFactor.getData()[i] = qxz.scale(0);
                    intercept[i] = 0;
                }

            }
            return new ContinuousDecisionMarginalAnswer(
                    new CGUtility(cgu1.discreteVariables, new LinkedHashSet<Node>(contVars), q2, r2, s2),
                    regFactor, new Potential(cgu1.getDiscreteVariables(), intercept));

        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }

        return null;
    }

    public CGUtility marginalizeContinuousChanceVariable(Node x, CGPotential cg) {
        try {
            CGUtility cgu1 = (CGUtility) this.clone();
            List<Node> contVars = new ArrayList<Node>(cgu1.continuousVariables);
            int xIndex = contVars.indexOf(x);
            contVars.remove(x);
            int[] zIndecis = new int[contVars.size()];
            for (int i = 0; i < xIndex; i++)
                zIndecis[i] = i;
            for (int i = xIndex; i < zIndecis.length; i++)
                zIndecis[i] = i + 1;


            MatrixPotential q2 = new MatrixPotential(cgu1.discreteVariables);
            MatrixPotential r2 = new MatrixPotential(cgu1.discreteVariables);
            Potential s2 = new Potential(cgu1.discreteVariables);

            for (int i = 0; i < q2.getTotalSize(); i++) {
                SimpleMatrix q = cgu1.getQ().getData()[i];
                SimpleMatrix r = cgu1.getR().getData()[i];
                double s = cgu1.getS().getData()[i];
                double qxx = q.get(xIndex, xIndex);
                SimpleMatrix qzx = MatrixUtils.subMatrix(q, zIndecis, new int[]{xIndex});
                SimpleMatrix qxz = MatrixUtils.subMatrix(q, new int[]{xIndex}, zIndecis);
                SimpleMatrix qzz = MatrixUtils.subMatrix(q, zIndecis, zIndecis);
                double rx = r.get(0, xIndex);
                SimpleMatrix rz = MatrixUtils.subMatrix(r, new int[]{0}, zIndecis);
                double A = cg.getMeans().getData()[i].get(0, 0); // 1-by-1 matrix
                SimpleMatrix B = cg.getRegressionCoefficients().getData()[i];
                double C = cg.getVariances().getData()[i].get(0, 0); // 1-by-1 matrix;

                q2.getData()[i] = qzz.plus(qzx.mult(B)).plus(B.transpose().mult(qxz)).plus(B.transpose().mult(B).scale(qxx));
                r2.getData()[i] = rz.plus(qxz.scale(2 * A)).plus(B.scale(2 * qxx * A + rx));
                s2.set(i, qxx * (A * A + C) + s + rx * A);

            }
            return new CGUtility(cgu1.discreteVariables, new LinkedHashSet<Node>(contVars), q2, r2, s2);

        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public String toString() {
        return "\n" +
                "p: " + discreteVariables + "\n" +
                "Y: " + continuousVariables + "\n";
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return new CGUtility((LinkedHashSet<Node>) discreteVariables.clone(),
                (LinkedHashSet<Node>) continuousVariables.clone(),
                (MatrixPotential) Q.clone(),
                (MatrixPotential) R.clone(),
                (Potential) S.clone());
    }

    @Override
    public boolean equals(Object obj) {
        CGUtility cg1 = (CGUtility) obj;
        if (!this.discreteVariables.containsAll(cg1.discreteVariables)) return false;
        if (!cg1.discreteVariables.containsAll(this.discreteVariables)) return false;

        if (!this.continuousVariables.containsAll(cg1.continuousVariables)) return false;
        if (!cg1.continuousVariables.containsAll(this.continuousVariables)) return false;

        if (!this.Q.equals(cg1.Q)) return false;
        if (!this.R.equals(cg1.R)) return false;
        if (!this.S.equals(cg1.S)) return false;

        return true;
    }

    public CGUtility add(CGUtility cgu1) {
        LinkedHashSet<Node> wholeVars = (LinkedHashSet<Node>) this.continuousVariables.clone();
        wholeVars.addAll(cgu1.continuousVariables);
        try {
            CGUtility result = (CGUtility) this.clone();
            result.expand(wholeVars);
            CGUtility cgu1Copy = (CGUtility) cgu1.clone();
            cgu1Copy.expand(wholeVars);

            for (int i = 0; i < result.getQ().getTotalSize(); i++) {
                result.getQ().getData()[i] = result.getQ().getData()[i].plus(cgu1Copy.getQ().getData()[i]);
                result.getR().getData()[i] = result.getR().getData()[i].plus(cgu1Copy.getR().getData()[i]);
                result.getS().set(i, result.getS().getData()[i] + cgu1Copy.getS().getData()[i]);
            }
            return result;
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void expand(LinkedHashSet<Node> newContinuousVariables) {
        if (continuousVariables.isEmpty()) {
            MatrixPotential regressionCoefficients1 = new MatrixPotential(this.discreteVariables);
            for (int j = 0; j < regressionCoefficients1.getData().length; j++) {
                Q.getData()[j] = new SimpleMatrix(newContinuousVariables.size(), newContinuousVariables.size(), false,
                        new double[newContinuousVariables.size() * newContinuousVariables.size()]);
                R.getData()[j] = new SimpleMatrix(1, newContinuousVariables.size(), false,
                        new double[newContinuousVariables.size()]);
                S.getData()[j] = 0;
            }
        } else {
            int[] indices = new int[newContinuousVariables.size()];

            Iterator<Node> t1Iterator = newContinuousVariables.iterator();
            int i = 0;
            while (t1Iterator.hasNext()) {
                Node h1n = t1Iterator.next();
                Iterator<Node> hIterator = continuousVariables.iterator();
                indices[i] = -1;
                int j = 0;
                while (hIterator.hasNext()) {
                    Node hn = hIterator.next();
                    if (hn.equals(h1n)) {
                        indices[i] = j;
                        break;
                    }
                    j++;
                }
                i++;
            }
            MatrixPotential regressionCoefficients1 = new MatrixPotential(this.discreteVariables);
            for (int j = 0; j < regressionCoefficients1.getData().length; j++) {
                Q.getData()[j] = MatrixUtils.subMatrix(Q.getData()[j], indices, indices);
                R.getData()[j] = MatrixUtils.subMatrixByColumns(R.getData()[j], indices);
            }
        }
        continuousVariables = (LinkedHashSet<Node>) newContinuousVariables.clone();
    }

    public class ContinuousDecisionMarginalAnswer {
        private CGUtility cgUtility;
        private MatrixPotential regressionFactor;
        private Potential intercept;

        public ContinuousDecisionMarginalAnswer(CGUtility cgUtility, MatrixPotential regressionFactor,
                                                Potential intercept) {
            this.cgUtility = cgUtility;
            this.regressionFactor = regressionFactor;
            this.intercept = intercept;
        }

        public CGUtility getCgUtility() {
            return cgUtility;
        }

        public MatrixPotential getRegressionFactor() {
            return regressionFactor;
        }

        public Potential getIntercept() {
            return intercept;
        }
    }

}
