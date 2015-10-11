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
        return new CGPotential(discreteVariables, h1, tailVariables, discretePotential, means1,
                regressionCoefficients1, variances1);
    }

    public CGPotential directCombination(CGPotential cg1) {
        //System.out.println("------------ dir. Comb");
        try {
            CGPotential cgCopy = (CGPotential) this.clone();
            CGPotential cg1Copy = (CGPotential) cg1.clone();
            LinkedHashSet<Node> cg1HeadVariables = (LinkedHashSet<Node>) cg1Copy.headVariables.clone();
            cg1HeadVariables.removeAll(cgCopy.headVariables);
            cg1HeadVariables.removeAll(cgCopy.tailVariables);
            assert cg1HeadVariables.size() == cg1Copy.headVariables.size() : "Passed object head and current object domain should be disjoint sets.";

            LinkedHashSet<Node> t1 = (LinkedHashSet<Node>) cgCopy.tailVariables.clone();
            t1.addAll(cg1Copy.tailVariables);
            t1.removeAll(cgCopy.headVariables);
            cgCopy.expand(t1);

            LinkedHashSet<Node> t2 = (LinkedHashSet<Node>) cgCopy.headVariables.clone();
            t2.addAll(cgCopy.tailVariables);
            cg1Copy.expand(t2);

            LinkedHashSet<Node> h2 = (LinkedHashSet<Node>) this.headVariables.clone();
            h2.addAll(cg1.headVariables);


            Potential p = cgCopy.getDiscretePotential().multiply(cg1Copy.getDiscretePotential());

            MatrixPotential means2 = new MatrixPotential(p.getVariables());
            MatrixPotential regressionCoefficients2 = new MatrixPotential(p.getVariables());
            MatrixPotential variances2 = new MatrixPotential(p.getVariables());

            ArrayList<Node> cgDiscreteVars = new ArrayList<Node>(cgCopy.getDiscreteVariables());
            ArrayList<Node> cg1DiscreteVars = new ArrayList<Node>(cg1Copy.getDiscreteVariables());
            ArrayList<Node> pDiscreteVats = new ArrayList<Node>(p.getVariables());

            int[] cgBits = new int[cgDiscreteVars.size()];
            int[] cg1Bits = new int[cg1DiscreteVars.size()];

            for (int i = 0; i < cgDiscreteVars.size(); i++) {
                cgBits[i] = pDiscreteVats.indexOf(cgDiscreteVars.get(i));
            }
            for (int i = 0; i < cg1DiscreteVars.size(); i++) {
                cg1Bits[i] = pDiscreteVats.indexOf(cg1DiscreteVars.get(i));
            }

            for (int i = 0; i < p.getData().length; i++) {
                List<Integer> ind = p.getIndex(i);
                List<Integer> cgInd = new ArrayList<Integer>();
                List<Integer> cg1Ind = new ArrayList<Integer>();
                for (int j : cgBits)
                    cgInd.add(ind.get(j));
                for (int j : cg1Bits)
                    cg1Ind.add(ind.get(j));
                SimpleMatrix A = cgCopy.getMeans().getData()[cgCopy.getMeans().getPotPosition(cgInd)];
                SimpleMatrix B = cgCopy.getRegressionCoefficients().getData()[cgCopy.getRegressionCoefficients().getPotPosition(cgInd)];
                SimpleMatrix C = cgCopy.getVariances().getData()[cgCopy.getVariances().getPotPosition(cgInd)];
                SimpleMatrix E = cg1Copy.getMeans().getData()[cg1Copy.getMeans().getPotPosition(cg1Ind)];
                SimpleMatrix F = cg1Copy.getRegressionCoefficients().getData()[cg1Copy.getRegressionCoefficients().getPotPosition(cg1Ind)];
                SimpleMatrix G = cg1Copy.getVariances().getData()[cg1Copy.getVariances().getPotPosition(cg1Ind)];
                SimpleMatrix F1 = F.extractMatrix(0, F.numRows(), 0, cgCopy.headVariables.size());
                SimpleMatrix F2 = F.extractMatrix(0, F.numRows(), cgCopy.headVariables.size(), F.numCols());

                means2.getData()[i] = A.combine(A.numRows(), 0, E.plus(F1.mult(A)));
                regressionCoefficients2.getData()[i] = B.combine(B.numRows(), 0, F2.plus(F1.mult(B)));
                variances2.getData()[i] = C.combine(C.numRows(), 0, F1.mult(C))
                        .combine(0, C.numCols(), C.mult(F1.transpose()).combine(C.numRows(), 0, G.plus(F1.mult(C).mult(F1.transpose()))));
            }

            return new CGPotential(p.getVariables(), h2, cgCopy.tailVariables, p, means2, regressionCoefficients2, variances2);
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }

        return null;
    }

    public CGPotential complement(LinkedHashSet<Node> h1) {
        //System.out.println("------------ compl.");
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

    public CGPotential recursiveCombination(CGPotential cg1) {
        //System.out.println("------------ rec. Comb");
        CGPotential cgCopy = null;
        try {
            cgCopy = (CGPotential) this.clone();
            CGPotential cg1Copy = (CGPotential) cg1.clone();
//            System.out.println("phi = " + cgCopy);
//            System.out.println("psi = " + cg1Copy);
            LinkedHashSet<Node> cgHeadVariables = (LinkedHashSet<Node>) cgCopy.headVariables.clone();
            LinkedHashSet<Node> cg1HeadVariables = (LinkedHashSet<Node>) cg1Copy.headVariables.clone();
            cg1HeadVariables.removeAll(cgCopy.headVariables);
            cg1HeadVariables.removeAll(cgCopy.tailVariables);
            cgHeadVariables.removeAll(cg1Copy.headVariables);
            cgHeadVariables.removeAll(cg1Copy.tailVariables);
//            System.out.println("D12 = " + cgHeadVariables);
//            System.out.println("D21 = " + cg1HeadVariables);

            assert !cg1HeadVariables.isEmpty() || !cgHeadVariables.isEmpty() : "Recursive combination is not defined";


            if (cgHeadVariables.size() == cgCopy.headVariables.size()) {
                CGPotential result = cg1Copy.directCombination(cgCopy);
                result.reduce();
//                System.out.println("psi ⨶ phi  = " + result);
                return result;

            } else if (cg1HeadVariables.size() == cg1Copy.headVariables.size()) {
                CGPotential result = cgCopy.directCombination(cg1Copy);
                result.reduce();
//                System.out.println("phi ⨶ psi  = " + result);
                return result;
            } else {
                if (!cgHeadVariables.isEmpty()) {
//                    System.out.println("** phi chosen");
                    LinkedHashSet<Node> h = (LinkedHashSet<Node>) cgCopy.headVariables.clone();
                    h.removeAll(cgHeadVariables);
//                    System.out.println("h = " + h);
                    CGPotential phi_p = cgCopy.complement(h);
                    phi_p.reduce();
                    CGPotential phi_dp = cgCopy.headMarginal(h);
                    phi_dp.reduce();
//                    System.out.println("phi_p = " + phi_p);
//                    System.out.println("phi_dp = " + phi_dp);
                    CGPotential phi_dpXpsi = phi_dp.recursiveCombination(cg1Copy);
                    phi_dpXpsi.reduce();
//                    System.out.println("phi_dp ⊗ psi = " + phi_dpXpsi);
                    return phi_p.directCombination(phi_dpXpsi);
                } else if (!cg1HeadVariables.isEmpty()) {
//                    System.out.println("**  psi chosen");
                    LinkedHashSet<Node> h = (LinkedHashSet<Node>) cg1Copy.headVariables.clone();
                    h.removeAll(cg1HeadVariables);
//                    System.out.println("h = " + h);
                    CGPotential psi_p = cg1Copy.complement(h);
                    psi_p.reduce();
                    CGPotential psi_dp = cg1Copy.headMarginal(h);
                    psi_dp.reduce();
//                    System.out.println("psi_p = " + psi_p);
//                    System.out.println("psi_dp = " + psi_dp);
                    CGPotential psi_dpXphi = psi_dp.recursiveCombination(cgCopy);
//                    System.out.println("psi_dp ⊗ phi = " + psi_dpXphi);
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
