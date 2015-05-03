package org.uci.lids.utils;

import org.uci.lids.Node;

import java.util.*;

/**
 * Created by hamid on 3/25/15.
 */
public class Potential {
    private LinkedHashSet<Node> variables;
    private double[] data;

    public Potential(LinkedHashSet<Node> variables, double[] data) {
        this.variables = variables;
        this.data = data;
    }

    public Potential(LinkedHashSet<Node> variables) {
        this.variables = variables;
        this.data = new double[getTotalSize()];
    }

    public static Potential multiply(Set<Potential> potentialSet) {
        Iterator<Potential> it = potentialSet.iterator();
        Potential result = it.next();

        while (it.hasNext()) {
            result = result.multiply(it.next());
        }
        return result;
    }

    public static Potential unityPotential() {
        LinkedHashSet<Node> emptySet = new LinkedHashSet<Node>();
        return new Potential(emptySet, new double[]{1});
    }

    public double[] getData() {
        return data;
    }

    public Potential multiply(Potential p) {
        LinkedHashSet<Node> variables = new LinkedHashSet<Node>(this.variables);
        variables.addAll(p.variables);
        Potential result = new Potential(variables);


        ArrayList<Node> thisVars = new ArrayList<Node>(this.variables);
        ArrayList<Node> pVars = new ArrayList<Node>(p.variables);
        ArrayList<Node> resultVars = new ArrayList<Node>(variables);

        int[] thisBits = new int[thisVars.size()];
        int[] pBits = new int[pVars.size()];

        for (int i = 0; i < thisVars.size(); i++) {
            thisBits[i] = resultVars.indexOf(thisVars.get(i));
        }
        for (int i = 0; i < pVars.size(); i++) {
            pBits[i] = resultVars.indexOf(pVars.get(i));
        }

        for (int i = 0; i < result.data.length; i++) {
            List<Integer> ind = result.getIndex(i);
            List<Integer> thisInd = new ArrayList<Integer>();
            List<Integer> pInd = new ArrayList<Integer>();
            for (int j : thisBits)
                thisInd.add(ind.get(j));
            for (int j : pBits)
                pInd.add(ind.get(j));
            result.data[i] = this.data[this.getPotPosition(thisInd)] * p.data[p.getPotPosition(pInd)];
        }

        return result;
    }

    public void applyEvidence(Node variable, int state) {
        double[] data = new double[this.getTotalSize()];

        Set<Node> variables = new LinkedHashSet<Node>(this.variables);
        variables.remove(variable);
        Potential tmp = new Potential((LinkedHashSet<Node>) variables);
        ArrayList<Node> thisVars = new ArrayList<Node>(this.variables);
        int varIndex = thisVars.indexOf(variable);
        for (int i = 0; i < tmp.getTotalSize(); i++) {
            List<Integer> index = tmp.getIndex(i);
            index.add(varIndex, state);
            data[this.getPotPosition(index)] = this.data[this.getPotPosition(index)];
        }
        this.data = data;
    }

    public void normalize(Node variable) {
        double[] data = new double[this.getTotalSize()];

        LinkedHashSet<Node> variables = new LinkedHashSet<Node>(this.variables);
        variables.remove(variable);
        Potential tmp = new Potential(variables);
        ArrayList<Node> thisVars = new ArrayList<Node>(this.variables);
        int varIndex = thisVars.indexOf(variable);
        for (int i = 0; i < this.getTotalSize(); i++) {
            List<Integer> index = this.getIndex(i);
            index.remove(varIndex);
            tmp.getData()[tmp.getPotPosition(index)] += this.data[i];
        }
        for (int i = 0; i < this.getTotalSize(); i++) {
            List<Integer> index = this.getIndex(i);
            index.remove(varIndex);
            this.data[i] /= tmp.getData()[tmp.getPotPosition(index)];
        }
    }

    public Potential project(Set<Node> variables) {
        variables = new LinkedHashSet<Node>(variables);  // make a copy
        variables.retainAll(this.variables);
        Potential result = new Potential((LinkedHashSet<Node>) variables);

        ArrayList<Node> thisVars = new ArrayList<Node>(this.variables);
        ArrayList<Node> projectionVars = new ArrayList<Node>(variables);

        int[] projectionBits = new int[projectionVars.size()];

        for (int i = 0; i < projectionVars.size(); i++) {
            projectionBits[i] = thisVars.indexOf(projectionVars.get(i));
        }

        for (int i = 0; i < result.data.length; i++) {
            result.data[i] = 0;
        }

        for (int i = 0; i < this.getTotalSize(); i++) {
            List<Integer> ind = this.getIndex(i);
            List<Integer> projectionInd = new ArrayList<Integer>();
            for (int j : projectionBits)
                projectionInd.add(ind.get(j));

            result.data[result.getPotPosition(projectionInd)] += this.data[this.getPotPosition(ind)];

        }
        return result;
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
            sb.append(") -> ").append(String.format("%.3f", data[i]));
        }
        sb.append("}\n");
        return sb.toString();
        //return variables.toString();
    }

}
