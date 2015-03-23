package org.uci.lids;

import org.uci.lids.graph.Visualizable;

import java.util.UUID;

/**
 * Created by hamid on 3/9/15.
 */
public class Node implements Visualizable {
    private UUID uid;
    private VariableType variableType;
    private Category category;
    private int size;
    private String label;
    private float[] potential;

    public Node(VariableType variableType, Category category, int size, String label, float[] potential) {
        this.uid = UUID.randomUUID();
        this.variableType = variableType;
        this.category = category;
        this.size = size;
        this.label = label;
        this.potential = potential;

    }

    public String nodeType() {
        String typeString;
        if (variableType.equals(VariableType.Continuous))
            typeString = "Continuous";
        else
            typeString = "Discrete";

        String nodeTypeStr = typeString + category.toString();
        return nodeTypeStr;
    }

    public UUID getUid() {
        return uid;
    }

    public VariableType getVariableType() {
        return variableType;
    }

    public Category Category() {
        return category;
    }

    public int Size() {
        return size;
    }

    public String Label() {
        return label;
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

    public enum Category {
        Chance, Decision, Utility
    }

    public enum VariableType {
        Binary, Categorical, Continuous
    }
}
