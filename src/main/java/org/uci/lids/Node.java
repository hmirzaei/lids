package org.uci.lids;

/**
 * Created by hamid on 3/9/15.
 */
public class Node {
    private VariableType variableType;
    private Category category;
    private int size;
    private String label;
    private float[] potential;


    public Node(VariableType variableType, Category category, int size, String label, float[] potential) {
        this.variableType = variableType;
        this.category = category;
        this.size = size;
        this.label = label;
        this.potential = potential;

    }

    public VariableType VariableType() {
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

}
