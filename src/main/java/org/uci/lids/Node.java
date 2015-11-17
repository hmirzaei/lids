package org.uci.lids;

import org.ejml.simple.SimpleMatrix;
import org.uci.lids.graph.DirectedGraph;
import org.uci.lids.graph.Visualizable;
import org.uci.lids.utils.CGPotential;
import org.uci.lids.utils.CGUtility;
import org.uci.lids.utils.MatrixPotential;
import org.uci.lids.utils.Potential;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
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

    public CGUtility getCGUtility(DirectedGraph<Node> bayesianNetwork) {
        Potential p;
        MatrixPotential Q;
        MatrixPotential R;
        Potential S;
        Set<Node> parents = bayesianNetwork.getParents(this);
        LinkedHashSet<Node> discreteParents = new LinkedHashSet<Node>();
        LinkedHashSet<Node> continuousParents = new LinkedHashSet<Node>();
        for (Node n : parents) {
            if (n.getVariableType() == VariableType.Categorical)
                discreteParents.add(n);
            else
                continuousParents.add(n);
        }

        if (variableType == VariableType.Categorical) {
            p = getPotential(bayesianNetwork);
            Q = new MatrixPotential(p.getVariables());
            R = new MatrixPotential(p.getVariables());
            S = new Potential(p.getVariables());
            return new CGUtility(p.getVariables(), new LinkedHashSet<Node>(),
                    Q, R, S);
        } else if (variableType == VariableType.Continuous) {
            assert category == Category.Utility : "Node category has to be Utility";
            Q = new MatrixPotential(discreteParents);
            R = new MatrixPotential(discreteParents);
            int counter = 0;
            for (int i = 0; i < Q.getTotalSize(); i++) {
                Q.getData()[i] = new SimpleMatrix(continuousParents.size(), continuousParents.size(), true,
                        Arrays.copyOfRange(potentialArray, counter, counter + continuousParents.size() * continuousParents.size()));
                counter += continuousParents.size() * continuousParents.size();
            }
            for (int i = 0; i < R.getTotalSize(); i++) {
                R.getData()[i] = new SimpleMatrix(1, continuousParents.size(), true,
                        Arrays.copyOfRange(potentialArray, counter, counter + continuousParents.size()));
                counter += continuousParents.size();
            }
            S = new Potential(discreteParents, Arrays.copyOfRange(potentialArray, counter, counter + Q.getTotalSize()));
            return new CGUtility(discreteParents, continuousParents, Q, R, S);
        }
        return null;
    }

    public CGPotential getCGPotential(DirectedGraph<Node> bayesianNetwork) {
        Potential p;
        MatrixPotential means;
        MatrixPotential regCoeffs;
        MatrixPotential variances;
        Set<Node> parents = bayesianNetwork.getParents(this);
        LinkedHashSet<Node> discreteParents = new LinkedHashSet<Node>();
        LinkedHashSet<Node> continuousParents = new LinkedHashSet<Node>();
        for (Node n : parents) {
            if (n.getVariableType() == VariableType.Categorical)
                discreteParents.add(n);
            else
                continuousParents.add(n);
        }

        if (variableType == VariableType.Categorical) {
            p = getPotential(bayesianNetwork);
            means = new MatrixPotential(p.getVariables());
            regCoeffs = new MatrixPotential(p.getVariables());
            variances = new MatrixPotential(p.getVariables());
            return new CGPotential(p.getVariables(), new LinkedHashSet<Node>(), new LinkedHashSet<Node>(), p,
                    means, regCoeffs, variances);
        } else if (variableType == VariableType.Continuous) {
            assert category == Category.Chance : "Node category has to be Chance";
            p = Potential.unityPotential().add(new Potential(discreteParents));
            means = new MatrixPotential(discreteParents);
            regCoeffs = new MatrixPotential(discreteParents);
            variances = new MatrixPotential(discreteParents);
            int counter = 0;
            for (int i = 0; i < p.getTotalSize(); i++)
                means.getData()[i] = new SimpleMatrix(1, 1, true, potentialArray[counter++]);
            for (int i = 0; i < p.getTotalSize(); i++) {
                regCoeffs.getData()[i] = new SimpleMatrix(1, continuousParents.size(), true,
                        Arrays.copyOfRange(potentialArray, counter, counter + continuousParents.size()));
                counter += continuousParents.size();
            }
            for (int i = 0; i < p.getTotalSize(); i++)
                variances.getData()[i] = new SimpleMatrix(1, 1, true, potentialArray[counter++]);
            return new CGPotential(p.getVariables(), new LinkedHashSet<Node>(Arrays.asList(this)), continuousParents, p,
                    means, regCoeffs, variances);
        }
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
        Categorical, Continuous
    }


}
