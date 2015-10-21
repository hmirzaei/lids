package org.uci.lids.utils;

import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.CommonOps;
import org.ejml.simple.SimpleMatrix;
import org.ejml.simple.SimpleSVD;
import org.uci.lids.Node;

import java.util.*;

/**
 * Created by hamid on 10/3/15.
 */
public class CGPotential {
    private LinkedHashSet<Node> discreteVariables;
    private LinkedHashSet<Node> headVariables;
    private LinkedHashSet<Node> tailVariables;
    private Potential discretePotential;
    private MatrixPotential means;
    private MatrixPotential regressionCoefficients;
    private MatrixPotential variances;


    public CGPotential(LinkedHashSet<Node> discreteVariables,
                       LinkedHashSet<Node> headVariables, LinkedHashSet<Node> tailVariables,
                       Potential discretePotential, MatrixPotential meanValues, MatrixPotential regressionCoefficients,
                       MatrixPotential variances) {
        assert !headVariables.isEmpty() || tailVariables.isEmpty() : "No discrete potential can have continuous parents";
        this.discreteVariables = discreteVariables;
        this.headVariables = headVariables;
        this.tailVariables = tailVariables;
        this.discretePotential = discretePotential;
        this.means = meanValues;
        this.regressionCoefficients = regressionCoefficients;
        this.variances = variances;
    }

    public LinkedHashSet<Node> getDiscreteVariables() {
        return discreteVariables;
    }

    public LinkedHashSet<Node> getHeadVariables() {
        return headVariables;
    }

    public LinkedHashSet<Node> getTailVariables() {
        return tailVariables;
    }

    public Potential getDiscretePotential() {
        return discretePotential;
    }

    public MatrixPotential getMeans() {
        return means;
    }

    public MatrixPotential getRegressionCoefficients() {
        return regressionCoefficients;
    }

    public MatrixPotential getVariances() {
        return variances;
    }

    public CGPotential headMarginal(LinkedHashSet<Node> h1) {
        int[] indices = new int[h1.size()];

        Iterator<Node> h1Iterator = h1.iterator();
        int i = 0;
        while (h1Iterator.hasNext()) {
            Node h1n = h1Iterator.next();
            Iterator<Node> hIterator = headVariables.iterator();
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

        MatrixPotential means1 = new MatrixPotential(this.discreteVariables);
        MatrixPotential regressionCoefficients1 = new MatrixPotential(this.discreteVariables);
        MatrixPotential variances1 = new MatrixPotential(this.discreteVariables);

        for (int j = 0; j < means1.getData().length; j++) {
            means1.getData()[j] = MatrixUtils.subMatrixByRows(means.getData()[j], indices);
            regressionCoefficients1.getData()[j] = MatrixUtils.subMatrixByRows(regressionCoefficients.getData()[j], indices);
            variances1.getData()[j] = MatrixUtils.subMatrix(variances.getData()[j], indices, indices);
        }
        if (h1.isEmpty())
            return new CGPotential(discreteVariables, h1, new LinkedHashSet<Node>(), discretePotential, means1,
                    regressionCoefficients1, variances1);
        else
            return new CGPotential(discreteVariables, h1, tailVariables, discretePotential, means1,
                    regressionCoefficients1, variances1);
    }

    public CGPotential weakMarginal(LinkedHashSet<Node> h1) {
        try {
            CGPotential cgCopy = (CGPotential) this.clone();
            Potential p_tilde = cgCopy.getDiscretePotential().project(h1);
            Potential p = cgCopy.getDiscretePotential();
            LinkedHashSet<Node> w1 = cgCopy.getDiscreteVariables();
            w1.removeAll(h1);


            MatrixPotential means2 = new MatrixPotential(h1);
            MatrixPotential variances2 = new MatrixPotential(h1);
            for (int i = 0; i < p_tilde.getTotalSize(); i++) {
                means2.getData()[i] = new SimpleMatrix(cgCopy.headVariables.size(), 1
                        , false, new double[cgCopy.headVariables.size()]);
                variances2.getData()[i] = new SimpleMatrix(cgCopy.headVariables.size(), cgCopy.headVariables.size()
                        , false, new double[cgCopy.headVariables.size() * cgCopy.headVariables.size()]);
            }

            Iterator<int[]> indIterator =
                    Potential.getComponentIndexIterator(cgCopy.getDiscretePotential(), p_tilde, new Potential(w1));
            for (int i = 0; i < cgCopy.getDiscretePotential().getTotalSize(); i++) {
                int[] ind = indIterator.next();
                SimpleMatrix A_tilde = means2.getData()[ind[0]];
                SimpleMatrix A = cgCopy.getMeans().getData()[i];
                means2.getData()[ind[0]] = A_tilde.plus(A.scale(p.getData()[i]));
            }
            for (int i = 0; i < p_tilde.getTotalSize(); i++) {
                means2.getData()[i] = means2.getData()[i].divide(p_tilde.getData()[i]);
            }

            indIterator =
                    Potential.getComponentIndexIterator(cgCopy.getDiscretePotential(), p_tilde, new Potential(w1));
            for (int i = 0; i < cgCopy.getDiscretePotential().getTotalSize(); i++) {
                int[] ind = indIterator.next();
                SimpleMatrix C_tilde = variances2.getData()[ind[0]];
                SimpleMatrix A = cgCopy.getMeans().getData()[i];
                SimpleMatrix C = cgCopy.getVariances().getData()[i];
                SimpleMatrix A_tilde = means2.getData()[ind[0]];
                variances2.getData()[ind[0]] = C_tilde.plus(C.plus(A.minus(A_tilde).mult(A.minus(A_tilde).transpose()))
                        .scale(p.getData()[i]));
            }
            for (int i = 0; i < p_tilde.getTotalSize(); i++) {
                variances2.getData()[i] = variances2.getData()[i].divide(p_tilde.getData()[i]);
            }
            return new CGPotential(p_tilde.getVariables(), cgCopy.getHeadVariables(), cgCopy.getTailVariables(), p_tilde, means2, cgCopy.getRegressionCoefficients(), variances2);

        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return null;
    }

    public CGPotential directCombination(CGPotential cg1) {
        try {
            CGPotential cgCopy = (CGPotential) this.clone();
            CGPotential cg1Copy = (CGPotential) cg1.clone();
            Potential combinedPot = cgCopy.getDiscretePotential().multiply(cg1Copy.getDiscretePotential());

            if (cgCopy.headVariables.isEmpty() && cg1Copy.headVariables.isEmpty()) {
                return new CGPotential(combinedPot.getVariables(), cgCopy.headVariables, cgCopy.tailVariables,
                        combinedPot, cgCopy.means, cgCopy.regressionCoefficients, cgCopy.variances);
            } else if (cgCopy.headVariables.isEmpty()) {
                CGPotential temp = cg1Copy;
                cg1Copy = cgCopy;
                cgCopy = temp;
            }

            if (cg1Copy.headVariables.isEmpty()) {
                MatrixPotential means2 = new MatrixPotential(combinedPot.getVariables());
                MatrixPotential regressionCoefficients2 = new MatrixPotential(combinedPot.getVariables());
                MatrixPotential variances2 = new MatrixPotential(combinedPot.getVariables());

                Iterator<int[]> indIterator =
                        Potential.getComponentIndexIterator(combinedPot, cgCopy.getDiscretePotential(), cg1Copy.getDiscretePotential());
                for (int i = 0; i < combinedPot.getData().length; i++) {
                    int[] ind = indIterator.next();
                    SimpleMatrix A = cgCopy.getMeans().getData()[ind[0]];
                    SimpleMatrix B = cgCopy.getRegressionCoefficients().getData()[ind[0]];
                    SimpleMatrix C = cgCopy.getVariances().getData()[ind[0]];

                    means2.getData()[i] = A;
                    regressionCoefficients2.getData()[i] = B;
                    variances2.getData()[i] = C;
                }

                return new CGPotential(combinedPot.getVariables(), cgCopy.headVariables, cgCopy.tailVariables,
                        combinedPot, means2, regressionCoefficients2, variances2);
            }
            LinkedHashSet<Node> cg1HeadVariables = (LinkedHashSet<Node>) cg1Copy.headVariables.clone();
            cg1HeadVariables.removeAll(cgCopy.headVariables);
            cg1HeadVariables.removeAll(cgCopy.tailVariables);
            if (cg1HeadVariables.size() != cg1Copy.headVariables.size()) {
                CGPotential temp = cg1Copy;
                cg1Copy = cgCopy;
                cgCopy = temp;
                cg1HeadVariables = (LinkedHashSet<Node>) cg1Copy.headVariables.clone();
                cg1HeadVariables.removeAll(cgCopy.headVariables);
                cg1HeadVariables.removeAll(cgCopy.tailVariables);
                assert cg1HeadVariables.size() == cg1Copy.headVariables.size()
                        : "At least one of the head variable sets and the other potential domain should be disjoint sets.";
            }
            LinkedHashSet<Node> h2 = (LinkedHashSet<Node>) cgCopy.headVariables.clone();
            h2.addAll(cg1Copy.headVariables);

            MatrixPotential means2 = new MatrixPotential(combinedPot.getVariables());
            MatrixPotential regressionCoefficients2 = new MatrixPotential(combinedPot.getVariables());
            MatrixPotential variances2 = new MatrixPotential(combinedPot.getVariables());

            Iterator<int[]> indIterator =
                    Potential.getComponentIndexIterator(combinedPot, cgCopy.getDiscretePotential(), cg1Copy.getDiscretePotential());

            if (cgCopy.getTailVariables().isEmpty() && cg1Copy.getTailVariables().isEmpty()) {
                for (int i = 0; i < combinedPot.getData().length; i++) {
                    int[] ind = indIterator.next();
                    SimpleMatrix A = cgCopy.getMeans().getData()[ind[0]];
                    SimpleMatrix C = cgCopy.getVariances().getData()[ind[0]];
                    SimpleMatrix E = cg1Copy.getMeans().getData()[ind[1]];
                    SimpleMatrix G = cg1Copy.getVariances().getData()[ind[1]];

                    means2.getData()[i] = A.combine(A.numRows(), 0, E);
                    variances2.getData()[i] = C.combine(C.numRows(), C.numCols(), G);
                }

                return new CGPotential(combinedPot.getVariables(), h2, cgCopy.tailVariables, combinedPot, means2, cgCopy.regressionCoefficients, variances2);

            } else {

                LinkedHashSet<Node> t1 = (LinkedHashSet<Node>) cgCopy.tailVariables.clone();
                t1.addAll(cg1Copy.tailVariables);
                t1.removeAll(cgCopy.headVariables);
                cgCopy.expand(t1);

                LinkedHashSet<Node> t2 = (LinkedHashSet<Node>) cgCopy.headVariables.clone();
                t2.addAll(cgCopy.tailVariables);
                cg1Copy.expand(t2);


                for (int i = 0; i < combinedPot.getData().length; i++) {
                    int[] ind = indIterator.next();
                    SimpleMatrix A = cgCopy.getMeans().getData()[ind[0]];
                    SimpleMatrix B = cgCopy.getRegressionCoefficients().getData()[ind[0]];
                    SimpleMatrix C = cgCopy.getVariances().getData()[ind[0]];
                    SimpleMatrix E = cg1Copy.getMeans().getData()[ind[1]];
                    SimpleMatrix F = cg1Copy.getRegressionCoefficients().getData()[ind[1]];
                    SimpleMatrix G = cg1Copy.getVariances().getData()[ind[1]];
                    SimpleMatrix F1 = F.extractMatrix(0, F.numRows(), 0, cgCopy.headVariables.size());
                    if (cgCopy.headVariables.size() < F.numCols()) {
                        SimpleMatrix F2 = F.extractMatrix(0, F.numRows(), cgCopy.headVariables.size(), F.numCols());
                        regressionCoefficients2.getData()[i] = B.combine(B.numRows(), 0, F2.plus(F1.mult(B)));
                    } else {
                        regressionCoefficients2.getData()[i] = B;
                    }
                    means2.getData()[i] = A.combine(A.numRows(), 0, E.plus(F1.mult(A)));
                    variances2.getData()[i] = C.combine(C.numRows(), 0, F1.mult(C))
                            .combine(0, C.numCols(), C.mult(F1.transpose()).combine(C.numRows(), 0, G.plus(F1.mult(C).mult(F1.transpose()))));
                }

                return new CGPotential(combinedPot.getVariables(), h2, cgCopy.tailVariables, combinedPot, means2, regressionCoefficients2, variances2);
            }
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }

        return null;
    }

    public CGPotential complement(LinkedHashSet<Node> h1) {
        if (h1.isEmpty()) {
            try {
                return (CGPotential) this.clone();
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }
            return null;
        } else {
            LinkedHashSet<Node> h2 = (LinkedHashSet<Node>) this.headVariables.clone();
            h2.removeAll(h1);
            List<Node> hList = new ArrayList<Node>(headVariables);
            List<Node> h1List = new ArrayList<Node>(h1);
            List<Node> h2List = new ArrayList<Node>(h2);

            MatrixPotential means2 = new MatrixPotential(this.discreteVariables);
            MatrixPotential regressionCoefficients2 = new MatrixPotential(this.discreteVariables);
            MatrixPotential variances2 = new MatrixPotential(this.discreteVariables);

            int[] h1Bits = new int[h1.size()];
            int[] h2Bits = new int[h2.size()];

            for (int i = 0; i < h1.size(); i++) {
                h1Bits[i] = hList.indexOf(h1List.get(i));
            }
            for (int i = 0; i < h2.size(); i++) {
                h2Bits[i] = hList.indexOf(h2List.get(i));
            }

            for (int i = 0; i < this.discretePotential.getTotalSize(); i++) {
                SimpleMatrix A = this.means.getData()[i];
                SimpleMatrix B = this.regressionCoefficients.getData()[i];
                SimpleMatrix C = this.variances.getData()[i];
                SimpleMatrix A1 = MatrixUtils.subMatrixByRows(A, h1Bits);
                SimpleMatrix A2 = MatrixUtils.subMatrixByRows(A, h2Bits);
                SimpleMatrix B1 = MatrixUtils.subMatrixByRows(B, h1Bits);
                SimpleMatrix B2 = MatrixUtils.subMatrixByRows(B, h2Bits);
                SimpleMatrix C11 = MatrixUtils.subMatrix(C, h1Bits, h1Bits);
                SimpleMatrix C12 = MatrixUtils.subMatrix(C, h1Bits, h2Bits);
                SimpleMatrix C21 = MatrixUtils.subMatrix(C, h2Bits, h1Bits);
                SimpleMatrix C22 = MatrixUtils.subMatrix(C, h2Bits, h2Bits);
                SimpleMatrix C11_inv;
                if (Math.abs(C11.transpose().mult(C11).determinant()) < 1e-10) {
                    SimpleSVD svd = C11.svd();
                    for (int j = 0; j < Math.min(svd.getW().numCols(), svd.getW().numRows()); j++) {
                        double d = svd.getW().get(j, j);
                        if (Math.abs(d) < 1e-10)
                            svd.getW().set(j, j, 0);
                        else
                            svd.getW().set(j, j, 1 / svd.getW().get(j, j));
                    }
                    C11_inv = svd.getV().mult(svd.getW().transpose()).mult(svd.getU().transpose());

                } else {
                    DenseMatrix64F C11_inv_dm = new DenseMatrix64F(C11.numCols(), C11.numRows());
                    CommonOps.pinv(C11.getMatrix(), C11_inv_dm);
                    C11_inv = SimpleMatrix.wrap(C11_inv_dm);
                }
                means2.getData()[i] = A2.minus(C21.mult(C11_inv).mult(A1));
                SimpleMatrix F = C21.mult(C11_inv);
                F = F.combine(0, F.numCols(), B2.minus(C21.mult(C11_inv).mult(B1)));
                regressionCoefficients2.getData()[i] = F;
                variances2.getData()[i] = C22.minus(C21.mult(C11_inv).mult(C12));

            }

            Potential p = Potential.unityPotential().add(new Potential(discreteVariables));
            LinkedHashSet<Node> t = (LinkedHashSet<Node>) h1.clone();
            t.addAll(tailVariables);
            return new CGPotential(discreteVariables, h2, t, p, means2, regressionCoefficients2, variances2);
        }
    }

    public CGPotential recursiveCombination(CGPotential cg1) {
        CGPotential cgCopy = null;
        try {
            cgCopy = (CGPotential) this.clone();
            CGPotential cg1Copy = (CGPotential) cg1.clone();
            LinkedHashSet<Node> cgHeadVariables = (LinkedHashSet<Node>) cgCopy.headVariables.clone();
            LinkedHashSet<Node> cg1HeadVariables = (LinkedHashSet<Node>) cg1Copy.headVariables.clone();
            cg1HeadVariables.removeAll(cgCopy.headVariables);
            cg1HeadVariables.removeAll(cgCopy.tailVariables);
            cgHeadVariables.removeAll(cg1Copy.headVariables);
            cgHeadVariables.removeAll(cg1Copy.tailVariables);


            if (cgHeadVariables.size() == cgCopy.headVariables.size()) {
                CGPotential result = cg1Copy.directCombination(cgCopy);
                result.reduce();
                return result;

            } else if (cg1HeadVariables.size() == cg1Copy.headVariables.size()) {
                CGPotential result = cgCopy.directCombination(cg1Copy);
                result.reduce();
                return result;
            } else {
                assert !cg1HeadVariables.isEmpty() || !cgHeadVariables.isEmpty() : "Recursive combination is not defined";
                if (!cgHeadVariables.isEmpty()) {
                    LinkedHashSet<Node> h = (LinkedHashSet<Node>) cgCopy.headVariables.clone();
                    h.removeAll(cgHeadVariables);
                    CGPotential phi_p = cgCopy.complement(h);
                    phi_p.reduce();
                    CGPotential phi_dp = cgCopy.headMarginal(h);
                    phi_dp.reduce();
                    CGPotential phi_dpXpsi = phi_dp.recursiveCombination(cg1Copy);
                    phi_dpXpsi.reduce();
                    return phi_p.directCombination(phi_dpXpsi);
                } else if (!cg1HeadVariables.isEmpty()) {
                    LinkedHashSet<Node> h = (LinkedHashSet<Node>) cg1Copy.headVariables.clone();
                    h.removeAll(cg1HeadVariables);
                    CGPotential psi_p = cg1Copy.complement(h);
                    psi_p.reduce();
                    CGPotential psi_dp = cg1Copy.headMarginal(h);
                    psi_dp.reduce();
                    CGPotential psi_dpXphi = psi_dp.recursiveCombination(cgCopy);
                    psi_dpXphi.reduce();
                    return psi_p.directCombination(psi_dpXphi);
                }
            }


        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }

        return null;
    }

    public void expand(LinkedHashSet<Node> t1) {
        int[] indices = new int[t1.size()];

        Iterator<Node> t1Iterator = t1.iterator();
        int i = 0;
        while (t1Iterator.hasNext()) {
            Node h1n = t1Iterator.next();
            Iterator<Node> hIterator = tailVariables.iterator();
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
            regressionCoefficients.getData()[j] = MatrixUtils.subMatrixByColumns(regressionCoefficients.getData()[j], indices);
        }
        tailVariables = t1;
    }

    public void reduce() {
        int[] indices = new int[tailVariables.size()];
        LinkedHashSet<Node> tails1 = new LinkedHashSet<Node>();
        Iterator<Node> tailIterator = tailVariables.iterator();
        int j = 0;
        int k = 0;
        while (tailIterator.hasNext()) {
            Node tn = tailIterator.next();
            boolean wholeRowIsZero = true;
            for (SimpleMatrix m : regressionCoefficients.getData()) {
                SimpleMatrix v = m.extractVector(false, j);
                for (int i = 0; i < v.numRows(); i++)
                    if (v.get(i) != 0) {
                        wholeRowIsZero = false;
                        break;
                    }
                if (!wholeRowIsZero) break;
            }
            if (!wholeRowIsZero) {
                indices[k++] = j;
                tails1.add(tn);
            }
            j++;
        }
        tailVariables.retainAll(tails1);
        for (int i = 0; i < regressionCoefficients.getData().length; i++) {
            regressionCoefficients.getData()[i] = MatrixUtils.subMatrixByColumns(regressionCoefficients.getData()[i],
                    Arrays.copyOfRange(indices, 0, k));
        }

    }

    @Override
    public String toString() {
        return "\n" +
                "p: " + discreteVariables + "\n" +
                "H: " + headVariables + "\n" +
                "T: " + tailVariables + "\n";
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return new CGPotential((LinkedHashSet<Node>) discreteVariables.clone(),
                (LinkedHashSet<Node>) headVariables.clone(),
                (LinkedHashSet<Node>) tailVariables.clone(),
                (Potential) discretePotential.clone(),
                (MatrixPotential) means.clone(),
                (MatrixPotential) regressionCoefficients.clone(),
                (MatrixPotential) variances.clone());
    }

    @Override
    public boolean equals(Object obj) {
        CGPotential cg1 = (CGPotential) obj;
        if (!this.discreteVariables.containsAll(cg1.discreteVariables)) return false;
        if (!cg1.discreteVariables.containsAll(this.discreteVariables)) return false;

        if (!this.headVariables.containsAll(cg1.headVariables)) return false;
        if (!cg1.headVariables.containsAll(this.headVariables)) return false;

        if (!this.tailVariables.containsAll(cg1.tailVariables)) return false;
        if (!cg1.tailVariables.containsAll(this.tailVariables)) return false;

        if (!this.discretePotential.equals(cg1.discretePotential)) return false;
        if (!this.means.equals(cg1.means)) return false;
        if (!this.regressionCoefficients.equals(cg1.regressionCoefficients)) return false;
        if (!this.variances.equals(cg1.variances)) return false;

        return true;
    }
}
