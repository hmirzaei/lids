package org.uci.lids;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.uci.lids.graph.DirectedGraph;
import org.uci.lids.utils.Misc;
import org.uci.lids.utils.Potential;

import java.util.*;


public class Example {

    final static Logger logger = Logger.getLogger(Example.class);

    public static void main(String[] args) {
        List<Node> nodes = new ArrayList<Node>();
        int N = 100;
        DirectedGraph<Node> bn = new DirectedGraph<Node>();

        Node A = new Node(Node.VariableType.Categorical, Node.Category.Chance, "A");
        Node B = new Node(Node.VariableType.Categorical, Node.Category.Chance, "B");
        Node C = new Node(Node.VariableType.Categorical, Node.Category.Chance, "C");
        Node T = new Node(Node.VariableType.Categorical, Node.Category.Chance, "T");
        Node D1 = new Node(Node.VariableType.Categorical, Node.Category.Decision, "D1");
        Node D2 = new Node(Node.VariableType.Categorical, Node.Category.Decision, "D2");
        Node V1 = new Node(Node.VariableType.Categorical, Node.Category.Utility, "V1");
        Node V2 = new Node(Node.VariableType.Categorical, Node.Category.Utility, "V2");

        A.setStates(new String[]{"Y", "N"});
        B.setStates(new String[]{"Y", "N"});
        C.setStates(new String[]{"Y", "N"});
        T.setStates(new String[]{"Y", "N"});
        D1.setStates(new String[]{"D1_1", "D1_2"});
        D2.setStates(new String[]{"D2_1", "D2_2"});

        A.setPotential(new double[]{0.2, 0.8, 0.8, 0.2});
        B.setPotential(new double[]{0.8, 0.2, 0.2, 0.8});
        C.setPotential(new double[]{0.9, 0.5, 0.5, 0.9, 0.1, 0.5, 0.5, 0.1});
        T.setPotential(new double[]{0.9, 0.5, 0.5, 0.1, 0.1, 0.5, 0.5, 0.9});
        V1.setPotential(new double[]{3, 0, 0, 2});
        V2.setPotential(new double[]{10, 0});

        bn.addNode(A);
        bn.addNode(B);
        bn.addNode(C);
        bn.addNode(T);
        bn.addNode(D1);
        bn.addNode(D2);
        bn.addNode(V1);
        bn.addNode(V2);

        bn.addLink(D1, A);
        bn.addLink(A, B);
        bn.addLink(B, C);
        bn.addLink(D2, C);
        bn.addLink(B, T);
        bn.addLink(A, T);
        bn.addLink(A, V1);
        bn.addLink(D2, V1);
        bn.addLink(C, V2);
        bn.addLink(T, D2);


        Potential pa = getChancePotential(bn, A);
        Potential pb = getChancePotential(bn, B);
        Potential pc = getChancePotential(bn, C);
        Potential pt = getChancePotential(bn, T);
        Potential pv1 = getUtilityPotential(bn, V1);
        Potential pv2 = getUtilityPotential(bn, V2);

        // p(A|D1)
        // p(B|A)
        // p(C|B,D2)
        // p(T|A,B)
        // V1(A,D2)
        // V2(C)

        // strong jtree: (D1,T,D2,A) <--- [T,D2,A] <--- (B,T,D2,A) <--- [B,D2] <--- (B,D2,C)

        Potential phi_c = pc.project(getNodeSet(B, D2));
        Potential psi_c = pc.multiply(pv2).project(getNodeSet(B, D2)).divide(phi_c);

        Potential phi_b = phi_c.multiply(pt).multiply(pb).project(getNodeSet(A, T, D2));
        Potential psi_b = phi_c.multiply(pt).multiply(pb).multiply(psi_c.add(pv1)).project(getNodeSet(A, T, D2)).divide(phi_b);

        Potential phi_last = phi_b.multiply(pa);
        Potential psi_last = phi_b.multiply(pa).multiply(psi_b);

        Potential ptd = phi_last.project(getNodeSet(T, D1, D2));
        System.out.println("D2 = " + psi_last.project(getNodeSet(T, D1, D2)).maxProject(getNodeSet(T, D1)).getMaxState());
        System.out.println("MEU2 = " + psi_last.project(getNodeSet(T, D1, D2)).maxProject(getNodeSet(T, D1)).getPotential().divide(ptd));

        System.out.println("D1 = " + psi_last.project(getNodeSet(T, D1, D2)).maxProject(getNodeSet(T, D1)).getPotential().project(getNodeSet(D1)).maxProject(getNodeSet()).getMaxState());
        System.out.println("MEU1 = " + psi_last.project(getNodeSet(T, D1, D2)).maxProject(getNodeSet(T, D1)).getPotential().project(getNodeSet(D1)).maxProject(getNodeSet()).getPotential());


        if (logger.getEffectiveLevel() == Level.DEBUG)
            Misc.saveGraphOnDisk("graph.html", bn);
        LQGInfluenceDiagram lid = new LQGInfluenceDiagram(bn);
        lid.getOptimalPolicy();
    }

    private static HashSet<Node> getNodeSet(Node... nodes) {
        return new HashSet<Node>(Arrays.asList(nodes));
    }

    private static Potential getChancePotential(DirectedGraph<Node> bn, Node n) {
        return new Potential((LinkedHashSet<Node>) bn.getFamily(n), n.getPotential());
    }

    private static Potential getUtilityPotential(DirectedGraph<Node> bn, Node n) {
        return new Potential(new LinkedHashSet<Node>(bn.getParents(n)), n.getPotential());
    }

    private static Node createNode(Node.Category category, int NO_STATES, int i) {
        Node node;
        node = new Node(Node.VariableType.Categorical, category, Integer.toString(i));

        String[] sa = new String[NO_STATES];
        for (int j = 0; j < NO_STATES; j++) {
            sa[j] = Integer.toString(j);
        }
        node.setStates(sa);
        return node;
    }
}
