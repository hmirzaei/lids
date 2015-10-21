package org.uci.lids.utils;

import org.uci.lids.Node;

import java.util.*;

/**
 * Created by hamid on 3/25/15.
 */
public class Potential {

    private LinkedHashSet<Node> variables;
    private ZeroConsciousDouble[] data;

    public Potential(LinkedHashSet<Node> variables, double[] data) {
        this.variables = variables;
        this.data = ZeroConsciousDouble.fromDoubleArray(data);
    }

    public Potential(LinkedHashSet<Node> variables) {
        this.variables = variables;
        this.data = new ZeroConsciousDouble[getTotalSize()];
        for (int i = 0; i < this.data.length; i++) {
            this.data[i] = new ZeroConsciousDouble(0);
        }
    }

    public static Potential multiply(Set<Potential> potentialSet) {
        Iterator<Potential> it = potentialSet.iterator();
        Potential result = it.next();

        while (it.hasNext()) {
            result = result.multiply(it.next());
        }
        return result;
    }

    public static Potential sum(Set<Potential> potentialSet) {
        Iterator<Potential> it = potentialSet.iterator();
        Potential result = it.next();

        while (it.hasNext()) {
            result = result.add(it.next());
        }
        return result;
    }

    public static Potential unityPotential() {
        LinkedHashSet<Node> emptySet = new LinkedHashSet<Node>();
        return new Potential(emptySet, new double[]{1});
    }

    public static Potential unityPotential(Set<Node> nodes) {
        LinkedHashSet<Node> nodeSet = new LinkedHashSet<Node>(nodes);
        Potential result = new Potential(nodeSet);
        for (int i = 0; i < result.data.length; i++) {
            result.data[i] = new ZeroConsciousDouble(1);
        }
        return result;
    }

    public static Potential zeroPotential() {
        LinkedHashSet<Node> emptySet = new LinkedHashSet<Node>();
        return new Potential(emptySet, new double[]{0});
    }

    public static Iterator<int[]> getComponentIndexIterator(final Potential a, final Potential a1, final Potential a2) {
        class ComponentIndexIterator implements Iterator<int[]> {
            ArrayList<Node> a1Vars = new ArrayList<Node>(a1.variables);
            ArrayList<Node> a2Vars = new ArrayList<Node>(a2.variables);
            ArrayList<Node> aVars = new ArrayList<Node>(a.variables);

            int[] a1Bits = new int[a1Vars.size()];
            int[] a2Bits = new int[a2Vars.size()];
            int counter = 0;

            {
                for (int i = 0; i < a1Vars.size(); i++) {
                    a1Bits[i] = aVars.indexOf(a1Vars.get(i));
                }
                for (int i = 0; i < a2Vars.size(); i++) {
                    a2Bits[i] = aVars.indexOf(a2Vars.get(i));
                }
            }

            public boolean hasNext() {
                return counter < a.getTotalSize();
            }

            public int[] next() {
                List<Integer> ind = a.getIndex(counter++);
                List<Integer> a1Ind = new ArrayList<Integer>();
                List<Integer> a2Ind = new ArrayList<Integer>();
                for (int j : a1Bits)
                    a1Ind.add(ind.get(j));
                for (int j : a2Bits)
                    a2Ind.add(ind.get(j));
                return new int[]{a1.getPotPosition(a1Ind), a2.getPotPosition(a2Ind)};
            }

        }
        return new ComponentIndexIterator();
    }

    public LinkedHashSet<Node> getVariables() {
        return variables;
    }

    public double[] getData() {
        return ZeroConsciousDouble.toDoubleArray(this.data);
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
            result.data[i] = this.data[this.getPotPosition(thisInd)].multiply(p.data[p.getPotPosition(pInd)]);
        }

        return result;
    }

    public Potential add(Potential p) {
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
            result.data[i] = this.data[this.getPotPosition(thisInd)].add(p.data[p.getPotPosition(pInd)]);
        }

        return result;
    }

    public Potential divide(Potential p) {
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
            result.data[i] = this.data[this.getPotPosition(thisInd)].divide(p.data[p.getPotPosition(pInd)]);
        }

        return result;
    }

    public void applyEvidence(Node variable, int state) {
        ZeroConsciousDouble[] data = new ZeroConsciousDouble[this.getTotalSize()];

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
        LinkedHashSet<Node> variables = new LinkedHashSet<Node>(this.variables);
        variables.remove(variable);
        Potential tmp = new Potential(variables);
        ArrayList<Node> thisVars = new ArrayList<Node>(this.variables);
        int varIndex = thisVars.indexOf(variable);
        for (int i = 0; i < this.getTotalSize(); i++) {
            List<Integer> index = this.getIndex(i);
            index.remove(varIndex);
            tmp.data[tmp.getPotPosition(index)] = tmp.data[tmp.getPotPosition(index)].add(this.data[i]);
        }
        for (int i = 0; i < this.getTotalSize(); i++) {
            List<Integer> index = this.getIndex(i);
            index.remove(varIndex);
            this.data[i] = this.data[i].divide(tmp.data[tmp.getPotPosition(index)]);
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
            result.data[i] = new ZeroConsciousDouble(0);
        }

        for (int i = 0; i < this.getTotalSize(); i++) {
            List<Integer> ind = this.getIndex(i);
            List<Integer> projectionInd = new ArrayList<Integer>();
            for (int j : projectionBits)
                projectionInd.add(ind.get(j));

            result.data[result.getPotPosition(projectionInd)] = result.data[result.getPotPosition(projectionInd)].add(this.data[this.getPotPosition(ind)]);

        }
        return result;
    }

    public MaxProjectAnswer maxProject(Set<Node> projVariables) {
        return maxProject(projVariables, this);
    }

    public MaxProjectAnswer maxProject(Set<Node> projVariables, Potential objectivePotential) {
        projVariables = new LinkedHashSet<Node>(projVariables);  // make a copy
        projVariables.retainAll(this.variables);

        LinkedHashSet<Node> eliminatedVariables = new LinkedHashSet<Node>(this.variables);  // make a copy
        eliminatedVariables.removeAll(projVariables);

        Potential maxPotential = new Potential((LinkedHashSet<Node>) projVariables);
        Potential projPotential = new Potential((LinkedHashSet<Node>) projVariables);
        HashMap<Node, Potential> maxStates = new HashMap<Node, Potential>();
        for (Node n : eliminatedVariables) {
            maxStates.put(n, new Potential((LinkedHashSet<Node>) projVariables));
        }


        ArrayList<Node> thisVars = new ArrayList<Node>(this.variables);
        ArrayList<Node> projectionVariablesList = new ArrayList<Node>(projVariables);
        ArrayList<Node> eliminatedVariablesList = new ArrayList<Node>(eliminatedVariables);

        int[] projectionBits = new int[projectionVariablesList.size()];
        int[] eliminatedBits = new int[eliminatedVariablesList.size()];

        for (int i = 0; i < projectionVariablesList.size(); i++) {
            projectionBits[i] = thisVars.indexOf(projectionVariablesList.get(i));
        }
        for (int i = 0; i < eliminatedVariablesList.size(); i++) {
            eliminatedBits[i] = thisVars.indexOf(eliminatedVariablesList.get(i));
        }

        for (int i = 0; i < maxPotential.data.length; i++) {
            maxPotential.data[i] = new ZeroConsciousDouble(Double.NEGATIVE_INFINITY);
        }

        for (int i = 0; i < this.getTotalSize(); i++) {
            List<Integer> ind = this.getIndex(i);
            List<Integer> projectionInd = new ArrayList<Integer>();
            for (int j : projectionBits)
                projectionInd.add(ind.get(j));
            List<Integer> eliminatedInd = new ArrayList<Integer>();
            for (int j : eliminatedBits)
                eliminatedInd.add(ind.get(j));

            if (objectivePotential.data[this.getPotPosition(ind)].toDouble() > maxPotential.data[maxPotential.getPotPosition(projectionInd)].toDouble()) {
                maxPotential.data[maxPotential.getPotPosition(projectionInd)] = objectivePotential.data[this.getPotPosition(ind)];
                projPotential.data[maxPotential.getPotPosition(projectionInd)] = this.data[this.getPotPosition(ind)];
                int c = 0;
                for (Node n : eliminatedVariables) {
                    Potential pot = maxStates.get(n);
                    pot.data[pot.getPotPosition(projectionInd)] = new ZeroConsciousDouble(ind.get(eliminatedBits[c]));
                    c++;
                }
            }
        }

        return new MaxProjectAnswer(projPotential, maxStates);
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
            sb.append(") -> ").append(String.format("%.3f", data[i].toDouble()));
        }
        sb.append("}\n");
        return sb.toString();
        //return variables.toString();
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return new Potential((LinkedHashSet<Node>) variables.clone(), getData());
    }

    @Override
    public boolean equals(Object obj) {
        Potential p = (Potential) obj;

        if (!this.variables.containsAll(p.variables)) return false;
        if (!p.variables.containsAll(this.variables)) return false;

        try {
            p = (Potential) p.clone();
            p = p.project(this.variables);
            for (int i = 0; i < p.getTotalSize(); i++) {
                if (Math.abs(p.getData()[i] - this.getData()[i]) > 1e-10) return false;
            }
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return true;
    }

    public class MaxProjectAnswer {
        private Potential potential;
        private Map<Node, Potential> maxState;

        public MaxProjectAnswer(Potential potential, Map<Node, Potential> maxState) {
            this.potential = potential;
            this.maxState = maxState;
        }

        public Potential getPotential() {
            return potential;
        }

        public Map<Node, Potential> getMaxState() {
            return maxState;
        }
    }
}
