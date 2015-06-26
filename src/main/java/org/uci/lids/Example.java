package org.uci.lids;

import org.uci.lids.graph.DirectedGraph;
import org.uci.lids.utils.Misc;


import java.util.*;


public class Example {



    public static void main(String[] args) {
        DirectedGraph<Node> bn = new DirectedGraph<Node>();

        Node A = new Node(Node.VariableType.Categorical, Node.Category.Chance,"A");
        Node B = new Node(Node.VariableType.Categorical, Node.Category.Chance,"B");
        Node C = new Node(Node.VariableType.Categorical, Node.Category.Chance,"C");
        Node D = new Node(Node.VariableType.Categorical, Node.Category.Chance,"D");
        Node E = new Node(Node.VariableType.Categorical, Node.Category.Chance,"E");
        Node F = new Node(Node.VariableType.Categorical, Node.Category.Chance,"F");
        Node G = new Node(Node.VariableType.Categorical, Node.Category.Chance,"G");
        Node H = new Node(Node.VariableType.Categorical, Node.Category.Chance,"H");
        Node I = new Node(Node.VariableType.Categorical, Node.Category.Chance,"I");
        Node J = new Node(Node.VariableType.Categorical, Node.Category.Chance,"J");
        Node K = new Node(Node.VariableType.Categorical, Node.Category.Chance,"K");
        Node L = new Node(Node.VariableType.Categorical, Node.Category.Chance,"L");
        Node D1 = new Node(Node.VariableType.Categorical, Node.Category.Decision,"D1");
        Node D2 = new Node(Node.VariableType.Categorical, Node.Category.Decision,"D2");
        Node D3 = new Node(Node.VariableType.Categorical, Node.Category.Decision,"D3");
        Node D4 = new Node(Node.VariableType.Categorical, Node.Category.Decision,"D4");
        Node V1 = new Node(Node.VariableType.Categorical, Node.Category.Utility,"V1");
        Node V2 = new Node(Node.VariableType.Categorical, Node.Category.Utility,"V2");
        Node V3 = new Node(Node.VariableType.Categorical, Node.Category.Utility,"V3");
        Node V4 = new Node(Node.VariableType.Categorical, Node.Category.Utility,"V4");
        bn.addNode(A);
        bn.addNode(B);
        bn.addNode(C);
        bn.addNode(D);
        bn.addNode(E);
        bn.addNode(F);
        bn.addNode(G);
        bn.addNode(H);
        bn.addNode(I);
        bn.addNode(J);
        bn.addNode(K);
        bn.addNode(L);
        bn.addNode(D1);
        bn.addNode(D2);
        bn.addNode(D3);
        bn.addNode(D4);
        bn.addNode(V1);
        bn.addNode(V2);
        bn.addNode(V3);
        bn.addNode(V4);
        bn.addLink(B,D1);
        bn.addLink(D1,V1);
        bn.addLink(A,C);
        bn.addLink(B,C);
        bn.addLink(B,D);
        bn.addLink(D1,D);
        bn.addLink(C,E);
        bn.addLink(D,E);
        bn.addLink(D,F);
        bn.addLink(E,G);
        bn.addLink(E,D2);
        bn.addLink(F,D2);
        bn.addLink(G,I);
        bn.addLink(D2,I);
        bn.addLink(F,H);
        bn.addLink(D4,L);
        bn.addLink(I,L);
        bn.addLink(H,J);
        bn.addLink(H,K);
        bn.addLink(D3,K);
        bn.addLink(D3,V4);
        bn.addLink(L,V2);
        bn.addLink(J,V3);
        bn.addLink(K,V3);
        bn.addLink(D2,D3);
        bn.addLink(D3,D4);
        bn.addLink(G,D4);

        LQGInfluenceDiagram lid = new LQGInfluenceDiagram(bn);
        lid.getOptimalPolicy();
        Misc.saveGraphOnDisk("graph.html", bn);
    }
}
