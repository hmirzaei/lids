package org.uci.lids.utils;

import org.ejml.simple.SimpleMatrix;
import org.uci.lids.Node;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;

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


    public LinkedHashSet<Node> getVariables() {
        return variables;
    }

    public SimpleMatrix[] getData() {
        return this.data;
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
}
