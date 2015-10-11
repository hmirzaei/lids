package org.uci.lids.utils;

import org.ejml.simple.SimpleMatrix;
import org.uci.lids.Node;

import java.util.*;

/**
 * Created by hamid on 3/25/15.
 */
public class MatrixPotential {

    private LinkedHashSet<Node> variables;
    private SimpleMatrix[] data;

    public MatrixPotential(LinkedHashSet<Node> variables, SimpleMatrix[] data) {
        this.variables = variables;
        this.data = data;
    }

    public MatrixPotential(LinkedHashSet<Node> variables) {
        this.variables = variables;
        this.data = new SimpleMatrix[getTotalSize()];
    }

    public static MatrixPotential multiply(Set<MatrixPotential> potentialSet) {
//        Iterator<MatrixPotential> it = potentialSet.iterator();
//        MatrixPotential result = it.next();
//
//        while (it.hasNext()) {
//            result = result.multiply(it.next());
//        }
//        return result;
        return null;
    }

    public static MatrixPotential sum(Set<MatrixPotential> potentialSet) {
//        Iterator<MatrixPotential> it = potentialSet.iterator();
//        MatrixPotential result = it.next();
//
//        while (it.hasNext()) {
//            result = result.add(it.next());
//        }
//        return result;
        return null;
    }

    public static MatrixPotential unityPotential() {
//        LinkedHashSet<Node> emptySet = new LinkedHashSet<Node>();
//        return new MatrixPotential(emptySet, new double[]{1});
        return null;
    }

    public static MatrixPotential unityPotential(Set<Node> nodes) {
//        LinkedHashSet<Node> nodeSet = new LinkedHashSet<Node>(nodes);
//        MatrixPotential result = new MatrixPotential(nodeSet);
//        for (int i = 0; i < result.data.length; i++) {
//            result.data[i] = new ZeroConsciousDouble(1);
//        }
//        return result;
        return null;
    }


    public static MatrixPotential zeroPotential() {
//        LinkedHashSet<Node> emptySet = new LinkedHashSet<Node>();
//        return new MatrixPotential(emptySet, new double[]{0});
        return null;
    }

    public LinkedHashSet<Node> getVariables() {
        return variables;
    }

    public SimpleMatrix[] getData() {
        return this.data;
    }

    public MatrixPotential multiply(MatrixPotential p) {
//        LinkedHashSet<Node> variables = new LinkedHashSet<Node>(this.variables);
//        variables.addAll(p.variables);
//        MatrixPotential result = new MatrixPotential(variables);
//
//
//        ArrayList<Node> thisVars = new ArrayList<Node>(this.variables);
//        ArrayList<Node> pVars = new ArrayList<Node>(p.variables);
//        ArrayList<Node> resultVars = new ArrayList<Node>(variables);
//
//        int[] thisBits = new int[thisVars.size()];
//        int[] pBits = new int[pVars.size()];
//
//        for (int i = 0; i < thisVars.size(); i++) {
//            thisBits[i] = resultVars.indexOf(thisVars.get(i));
//        }
//        for (int i = 0; i < pVars.size(); i++) {
//            pBits[i] = resultVars.indexOf(pVars.get(i));
//        }
//
//        for (int i = 0; i < result.data.length; i++) {
//            List<Integer> ind = result.getIndex(i);
//            List<Integer> thisInd = new ArrayList<Integer>();
//            List<Integer> pInd = new ArrayList<Integer>();
//            for (int j : thisBits)
//                thisInd.add(ind.get(j));
//            for (int j : pBits)
//                pInd.add(ind.get(j));
//            result.data[i] = this.data[this.getPotPosition(thisInd)].multiply(p.data[p.getPotPosition(pInd)]);
//        }
//
//        return result;
        return p;
    }

    public MatrixPotential add(MatrixPotential p) {
//        LinkedHashSet<Node> variables = new LinkedHashSet<Node>(this.variables);
//        variables.addAll(p.variables);
//        MatrixPotential result = new MatrixPotential(variables);
//
//
//        ArrayList<Node> thisVars = new ArrayList<Node>(this.variables);
//        ArrayList<Node> pVars = new ArrayList<Node>(p.variables);
//        ArrayList<Node> resultVars = new ArrayList<Node>(variables);
//
//        int[] thisBits = new int[thisVars.size()];
//        int[] pBits = new int[pVars.size()];
//
//        for (int i = 0; i < thisVars.size(); i++) {
//            thisBits[i] = resultVars.indexOf(thisVars.get(i));
//        }
//        for (int i = 0; i < pVars.size(); i++) {
//            pBits[i] = resultVars.indexOf(pVars.get(i));
//        }
//
//        for (int i = 0; i < result.data.length; i++) {
//            List<Integer> ind = result.getIndex(i);
//            List<Integer> thisInd = new ArrayList<Integer>();
//            List<Integer> pInd = new ArrayList<Integer>();
//            for (int j : thisBits)
//                thisInd.add(ind.get(j));
//            for (int j : pBits)
//                pInd.add(ind.get(j));
//            result.data[i] = this.data[this.getPotPosition(thisInd)].add(p.data[p.getPotPosition(pInd)]);
//        }
//
//        return result;
        return p;
    }

    public MatrixPotential divide(MatrixPotential p) {
//        LinkedHashSet<Node> variables = new LinkedHashSet<Node>(this.variables);
//        variables.addAll(p.variables);
//        MatrixPotential result = new MatrixPotential(variables);
//
//
//        ArrayList<Node> thisVars = new ArrayList<Node>(this.variables);
//        ArrayList<Node> pVars = new ArrayList<Node>(p.variables);
//        ArrayList<Node> resultVars = new ArrayList<Node>(variables);
//
//        int[] thisBits = new int[thisVars.size()];
//        int[] pBits = new int[pVars.size()];
//
//        for (int i = 0; i < thisVars.size(); i++) {
//            thisBits[i] = resultVars.indexOf(thisVars.get(i));
//        }
//        for (int i = 0; i < pVars.size(); i++) {
//            pBits[i] = resultVars.indexOf(pVars.get(i));
//        }
//
//        for (int i = 0; i < result.data.length; i++) {
//            List<Integer> ind = result.getIndex(i);
//            List<Integer> thisInd = new ArrayList<Integer>();
//            List<Integer> pInd = new ArrayList<Integer>();
//            for (int j : thisBits)
//                thisInd.add(ind.get(j));
//            for (int j : pBits)
//                pInd.add(ind.get(j));
//            result.data[i] = this.data[this.getPotPosition(thisInd)].divide(p.data[p.getPotPosition(pInd)]);
//        }
//
//        return result;
        return p;
    }

    public void applyEvidence(Node variable, int state) {
//        ZeroConsciousDouble[] data = new ZeroConsciousDouble[this.getTotalSize()];
//
//        Set<Node> variables = new LinkedHashSet<Node>(this.variables);
//        variables.remove(variable);
//        MatrixPotential tmp = new MatrixPotential((LinkedHashSet<Node>) variables);
//        ArrayList<Node> thisVars = new ArrayList<Node>(this.variables);
//        int varIndex = thisVars.indexOf(variable);
//        for (int i = 0; i < tmp.getTotalSize(); i++) {
//            List<Integer> index = tmp.getIndex(i);
//            index.add(varIndex, state);
//            data[this.getPotPosition(index)] = this.data[this.getPotPosition(index)];
//        }
//        this.data = data;
    }

    public void normalize(Node variable) {
//        LinkedHashSet<Node> variables = new LinkedHashSet<Node>(this.variables);
//        variables.remove(variable);
//        MatrixPotential tmp = new MatrixPotential(variables);
//        ArrayList<Node> thisVars = new ArrayList<Node>(this.variables);
//        int varIndex = thisVars.indexOf(variable);
//        for (int i = 0; i < this.getTotalSize(); i++) {
//            List<Integer> index = this.getIndex(i);
//            index.remove(varIndex);
//            tmp.data[tmp.getPotPosition(index)] = tmp.data[tmp.getPotPosition(index)].add(this.data[i]);
//        }
//        for (int i = 0; i < this.getTotalSize(); i++) {
//            List<Integer> index = this.getIndex(i);
//            index.remove(varIndex);
//            this.data[i] = this.data[i].divide(tmp.data[tmp.getPotPosition(index)]);
//        }
    }

    public MatrixPotential project(Set<Node> variables) {
//        variables = new LinkedHashSet<Node>(variables);  // make a copy
//        variables.retainAll(this.variables);
//        MatrixPotential result = new MatrixPotential((LinkedHashSet<Node>) variables);
//
//        ArrayList<Node> thisVars = new ArrayList<Node>(this.variables);
//        ArrayList<Node> projectionVars = new ArrayList<Node>(variables);
//
//        int[] projectionBits = new int[projectionVars.size()];
//
//        for (int i = 0; i < projectionVars.size(); i++) {
//            projectionBits[i] = thisVars.indexOf(projectionVars.get(i));
//        }
//
//        for (int i = 0; i < result.data.length; i++) {
//            result.data[i] = new ZeroConsciousDouble(0);
//        }
//
//        for (int i = 0; i < this.getTotalSize(); i++) {
//            List<Integer> ind = this.getIndex(i);
//            List<Integer> projectionInd = new ArrayList<Integer>();
//            for (int j : projectionBits)
//                projectionInd.add(ind.get(j));
//
//            result.data[result.getPotPosition(projectionInd)] = result.data[result.getPotPosition(projectionInd)].add(this.data[this.getPotPosition(ind)]);
//
//        }
//        return result;
        return null;
    }

    public MaxProjectAnswer maxProject(Set<Node> projVariables) {
        return maxProject(projVariables, this);
    }

    public MaxProjectAnswer maxProject(Set<Node> projVariables, MatrixPotential objectivePotential) {
//        projVariables = new LinkedHashSet<Node>(projVariables);  // make a copy
//        projVariables.retainAll(this.variables);
//
//        LinkedHashSet<Node> eliminatedVariables = new LinkedHashSet<Node>(this.variables);  // make a copy
//        eliminatedVariables.removeAll(projVariables);
//
//        MatrixPotential maxPotential = new MatrixPotential((LinkedHashSet<Node>) projVariables);
//        MatrixPotential projPotential = new MatrixPotential((LinkedHashSet<Node>) projVariables);
//        HashMap<Node, MatrixPotential> maxStates = new HashMap<Node, MatrixPotential>();
//        for (Node n : eliminatedVariables) {
//            maxStates.put(n, new MatrixPotential((LinkedHashSet<Node>) projVariables));
//        }
//
//
//        ArrayList<Node> thisVars = new ArrayList<Node>(this.variables);
//        ArrayList<Node> projectionVariablesList = new ArrayList<Node>(projVariables);
//        ArrayList<Node> eliminatedVariablesList = new ArrayList<Node>(eliminatedVariables);
//
//        int[] projectionBits = new int[projectionVariablesList.size()];
//        int[] eliminatedBits = new int[eliminatedVariablesList.size()];
//
//        for (int i = 0; i < projectionVariablesList.size(); i++) {
//            projectionBits[i] = thisVars.indexOf(projectionVariablesList.get(i));
//        }
//        for (int i = 0; i < eliminatedVariablesList.size(); i++) {
//            eliminatedBits[i] = thisVars.indexOf(eliminatedVariablesList.get(i));
//        }
//
//        for (int i = 0; i < maxPotential.data.length; i++) {
//            maxPotential.data[i] = new ZeroConsciousDouble(Double.NEGATIVE_INFINITY);
//        }
//
//        for (int i = 0; i < this.getTotalSize(); i++) {
//            List<Integer> ind = this.getIndex(i);
//            List<Integer> projectionInd = new ArrayList<Integer>();
//            for (int j : projectionBits)
//                projectionInd.add(ind.get(j));
//            List<Integer> eliminatedInd = new ArrayList<Integer>();
//            for (int j : eliminatedBits)
//                eliminatedInd.add(ind.get(j));
//
//            if (objectivePotential.data[this.getPotPosition(ind)].toDouble() > maxPotential.data[maxPotential.getPotPosition(projectionInd)].toDouble()) {
//                maxPotential.data[maxPotential.getPotPosition(projectionInd)] = objectivePotential.data[this.getPotPosition(ind)];
//                projPotential.data[maxPotential.getPotPosition(projectionInd)] = this.data[this.getPotPosition(ind)];
//                int c = 0;
//                for (Node n : eliminatedVariables) {
//                    MatrixPotential pot = maxStates.get(n);
//                    pot.data[pot.getPotPosition(projectionInd)] = new ZeroConsciousDouble(ind.get(eliminatedBits[c]));
//                    c++;
//                }
//            }
//        }
//
//        return new MaxProjectAnswer(projPotential, maxStates);
        return null;
    }

    public List<Integer> getIndex(int potPositon) {
        List<Integer> index = new ArrayList<Integer>();

        for (Node var : variables) {
            index.add(potPositon % var.getSize());
            potPositon = potPositon / var.getSize();
        }
        return index;
    }

    public int getPotPosition(List<Integer> index) {
        Iterator<Integer> it = index.iterator();

        int position = 0;
        int multiplier = 1;
        for (Node var : variables) {
            position += it.next() * multiplier;
            multiplier *= var.getSize();
        }
        return position;
    }

    public int getTotalSize() {
        int totalSize = 1;
        for (Node var : variables) {
            totalSize *= var.getSize();
        }
        return totalSize;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();


        String[] varStr = new String[variables.size()];
        int i = 0;
        for (Node var : variables) {
            varStr[i++] = var.toString();
        }

        sb.append("\n{");
        String prefix2 = "";
        for (i = 0; i < getTotalSize(); i++) {
            sb.append(prefix2).append("(");
            prefix2 = "\n";
            List<Integer> index = getIndex(i);
            int j = 0;
            Iterator<Node> it = variables.iterator();
            String prefix = "";
            for (int ind : index) {
                sb.append(prefix).append(varStr[j++]).append(":'").append(it.next().getStates()[ind]).append("'");
                prefix = ",  ";
            }
            sb.append(") -> ").append(data[i].toString());
        }
        sb.append("}\n");
        return sb.toString();
        //return variables.toString();
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return new MatrixPotential((LinkedHashSet<Node>) variables.clone(), data.clone());
    }

    @Override
    public boolean equals(Object obj) {
        MatrixPotential p = (MatrixPotential) obj;

        if (!this.variables.containsAll(p.variables)) return false;
        if (!p.variables.containsAll(this.variables)) return false;

        try {
            p = (MatrixPotential) p.clone();
//            p = p.project(this.variables);
            for (int i = 0; i < p.getTotalSize(); i++) {
                if (!p.getData()[i].isIdentical(this.getData()[i], 1e-10)) return false;
            }
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return true;
    }

    public class MaxProjectAnswer {
//        private MatrixPotential potential;
//        private Map<Node, MatrixPotential> maxState;
//
//        public MaxProjectAnswer(MatrixPotential potential, Map<Node, MatrixPotential> maxState) {
//            this.potential = potential;
//            this.maxState = maxState;
//        }
//
//        public MatrixPotential getPotential() {
//            return potential;
//        }
//
//        public Map<Node, MatrixPotential> getMaxState() {
//            return maxState;
//        }
    }

}
