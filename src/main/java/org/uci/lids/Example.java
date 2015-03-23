package org.uci.lids;

import org.uci.lids.graph.DirectedGraph;
import org.uci.lids.graph.JunctionTreeNode;
import org.uci.lids.graph.UndirectedGraph;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class Example {


    public static void main(String[] args) throws Exception {

        List<Node> nodes = new ArrayList<Node>();
        final int N = 20;
        final boolean writeHtmlFiles = true;
        PrintWriter writer;


        System.out.println("Graph Generation");
        DirectedGraph<Node> bayesianNetwork = new DirectedGraph<Node>();

        for (int i = 0; i < N; i++) {
//            Node node = new Node(VariableType.Categorical, Category.Chance, 3, Character.toString((char)('A'+i)), null);
            Node node = new Node(Node.VariableType.Categorical, Node.Category.Chance, 3, Integer.toString(i), null);
            nodes.add(node);
            bayesianNetwork.addNode(node);
        }

        Random random = new Random(1);

        for (int k = 1; k < 1 * N; k++) {
            int i = random.nextInt(N);
            int j = random.nextInt(N);
            if (i != j)
                bayesianNetwork.addLink(nodes.get(i), nodes.get(j));

        }


        if (writeHtmlFiles) {
            writer = new PrintWriter("graph.htm", "UTF-8");
            writer.println(bayesianNetwork.generateVisualizationHtml());
            writer.close();
        }
        System.out.println("Moralization");
        long startTime = System.currentTimeMillis();
        UndirectedGraph<Node> moralizedGraph = bayesianNetwork.getMoralizedUndirectedCopy();
        long estimatedTime = System.currentTimeMillis() - startTime;
        System.out.println("estimatedTime = " + estimatedTime / 1000.0 + " sec");

        if (writeHtmlFiles) {
            writer = new PrintWriter("moralized.htm", "UTF-8");
            writer.println(moralizedGraph.generateVisualizationHtml());
            writer.close();
        }

        System.out.println("Triangulation");
        startTime = System.currentTimeMillis();
        moralizedGraph.triangulate();
        estimatedTime = System.currentTimeMillis() - startTime;
        System.out.println("estimatedTime = " + estimatedTime / 1000.0 + " sec");

        if (writeHtmlFiles) {
            writer = new PrintWriter("triangulated.htm", "UTF-8");
            writer.println(moralizedGraph.generateVisualizationHtml());
            writer.close();
        }

        System.out.println("Junction tee construction");
        startTime = System.currentTimeMillis();
        UndirectedGraph<JunctionTreeNode<Node>> jt = moralizedGraph.getJunctionTree();
        estimatedTime = System.currentTimeMillis() - startTime;
        System.out.println("estimatedTime = " + estimatedTime / 1000.0 + " sec");

        if (writeHtmlFiles) {
            writer = new PrintWriter("junctiontree.htm", "UTF-8");
            writer.println(jt.generateVisualizationHtml());
            writer.close();
        }

        System.out.println("Finished");
    }
}
