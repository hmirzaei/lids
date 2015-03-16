package org.uci.lids;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Scanner;

/**
 * Created by hamid on 3/9/15.
 */
public class LQGInfluenceDiagram {
    private List<Node> nodes;
    private int[][] dag;
    private int[][] moralAdjacencyMatrix;
    private final int numberOfNodes;

    public int[][] getDag() {
        return dag;
    }
    public int[][] getMoralAdjacencyMatrix() {
        return moralAdjacencyMatrix;
    }


    public LQGInfluenceDiagram(List<Node> nodes, int[][] dag) {
        this.nodes = nodes;
        this.numberOfNodes = nodes.size();
        this.dag = dag;
        moralizeAndCovertToUndirected();
    }

    private void moralizeAndCovertToUndirected() {
        this.moralAdjacencyMatrix = new int[numberOfNodes][numberOfNodes];
        int[] index = new int[numberOfNodes];
        for (int j = 0; j < numberOfNodes; j++) {

            int numParents = 0;
            for (int i = 0; i< numberOfNodes; i++) {
                if (dag[i][j] == 1) {
                    index[numParents++] = i;
                    moralAdjacencyMatrix[i][j] = 1;
                    moralAdjacencyMatrix[j][i] = 1;
                }
            }

            for (int i = 0; i < numParents; i++) {
                for (int k = i+1; k < numParents; k++) {
                    moralAdjacencyMatrix[index[i]][index[k]] = 1;
                    moralAdjacencyMatrix[index[k]][index[i]] = 1;
                }
            }
            if (j%1000==0) System.out.println("j = " + j);
        }
    }

    private void triangulate() {

    }

    private void findSimplicialNode() {
        int[] cliqueNodes = new int[numberOfNodes];
        int[] parents = new int[numberOfNodes];

        int cliqueSize = 0;
        int parentsSize = 0;
        cliqueNodes[0] = 0;
        do {
            
        } while (true);



    }

    public  void constructJunctionTree() {

    }


    public String generateVisualizationHtml(int[][] adjacencyMatrix, boolean directed) {

        String htmlString = "";
        try {
            htmlString = new Scanner(new File("src/main/resources/graph_template.html")).useDelimiter("\\Z").next();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        StringBuilder nodeString = new StringBuilder();

        for (int i = 0; i < nodes.size(); i++) {
            String typeString = "";
            if (nodes.get(i).VariableType().equals(VariableType.Continuous))
                typeString = "Continuous";
            else
                typeString = "Discrete";

            String shapeString = typeString + nodes.get(i).Category().toString();
            nodeString.append(String.format("{id: \"%d\", label: \"%s\", group: \"%s\"},\n", i, nodes.get(i).Label(), shapeString));
        }

        StringBuilder edgeString = new StringBuilder();
        for (int i = 0; i < adjacencyMatrix.length; i++) {
            for (int j = 0; j < adjacencyMatrix.length; j++) {
                if (adjacencyMatrix[i][j] == 1)
                    edgeString.append(String.format("{from: %d, to: %d},\n", i, j));
            }
        }

        if(directed)
            htmlString = htmlString.replace("__edge_style", "edges: {style: 'arrow',},");
        else
            htmlString = htmlString.replace("__edge_style", "");


        htmlString = htmlString.replace("__node_data", nodeString.toString());
        htmlString = htmlString.replace("__edge_data", edgeString.toString());

        return htmlString;
    }
}
