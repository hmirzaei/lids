package org.uci.lids;

import org.uci.lids.graph.DirectedGraph;
import org.uci.lids.graph.Visualizable;
import org.uci.lids.utils.Potential;

import java.util.LinkedHashSet;
import java.util.UUID;

/**
 * Created by hamid on 3/9/15.
 */
public class Node implements Visualizable, Comparable<Node> {
    private UUID uid;
    private VariableType variableType;
    private Category category;
    private int size;
    private String label;
    private double[] potentialArray;
    private String[] states;

    public Node(VariableType variableType, Category category, String label) {
        this.uid = UUID.randomUUID();
        this.variableType = variableType;
        this.category = category;
        this.label = label;
    }

    public int getSize() {
        return states.length;
    }

    public double[] getPotentialArray() {
        return potentialArray;
    }

    public void setPotentialArray(double... potentialArray) {
        this.potentialArray = potentialArray;
    }

    public Potential getPotential(DirectedGraph<Node> bayesianNetwork) {
        if (category == Node.Category.Chance)
            return new Potential((LinkedHashSet<Node>) bayesianNetwork.getFamily(this), potentialArray);
        else if (category == Category.Utility)
            return new Potential((LinkedHashSet<Node>) bayesianNetwork.getParents(this), potentialArray);
        else
            return null;
    }

    public String[] getStates() {
        return states;
    }

    public void setStates(String[] states) {
        this.states = states;
    }

    public String nodeType() {
        String typeString;
        if (variableType.equals(VariableType.Continuous))
            typeString = "Continuous";
        else
            typeString = "Discrete";

        return typeString + category.toString();
    }

    public UUID getUid() {
        return uid;
    }

    public VariableType getVariableType() {
        return variableType;
    }


    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Node) {
            return this.uid.equals(((Node) obj).getUid());
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return uid.hashCode();
    }

    @Override
    public String toString() {
        return label;
    }

    public int compareTo(Node n) {
        if (label.matches("-?\\d+(\\.\\d+)?")) {
            return ((Double) Double.parseDouble(this.label)).compareTo(Double.parseDouble(n.label));
        } else {
            return this.label.compareTo(n.label);
        }
    }

    public Category getCategory() {
        return category;
    }


    public enum Category {
        Chance, Decision, Utility
    }

    public enum VariableType {
        Binary, Categorical, Continuous
    }


}
