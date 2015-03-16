package org.uci.lids;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Hello world!
 */
public class Example {


    public static void main(String[] args) throws Exception {
        List<Node> nodes = new ArrayList<Node>();
        final int N = 5;
        int[][] dag = new int[N][N];
        LQGInfluenceDiagram lqgid;


        for (int i = 0; i < N; i++) {
//            nodes.add(new Node(VariableType.Categorical, Category.Chance, 3, Character.toString((char)('A'+i)), null));
            nodes.add(new Node(VariableType.Categorical, Category.Chance, 3, Integer.toString(i), null));
        }

//        dag[0][1] = 1;
//        dag[0][2] = 1;
//        dag[0][3] = 1;
//        dag[1][2] = 1;
//        dag[1][3] = 1;
//        dag[1][4] = 1;
//        dag[1][6] = 1;
//        dag[2][3] = 1;
//        dag[2][4] = 1;
//        dag[2][6] = 1;
//        dag[2][7] = 1;
//        dag[2][9] = 1;
//        dag[3][4] = 1;
//        dag[3][5] = 1;
//        dag[3][6] = 1;
//        dag[3][8] = 1;
//        dag[4][5] = 1;
//        dag[4][8] = 1;
//        dag[5][8] = 1;
//        dag[6][7] = 1;
//        dag[6][9] = 1;
//        dag[7][9] = 1;

        Random random = new Random(5);

//        for (int i = 1; i < N; i++) {
////            for (int j = 0; j < N; j++) {
////                if (i != j)
////                    dag[i][j] = (random.nextDouble() < 0.8) ? 0 : 1;
////            }
//            dag[i][0] = 1;
//        }

        dag[0][1]=1;
        dag[1][2]=1;
        dag[2][3]=1;
        dag[3][0]=1;
        dag[4][1]=1;
        dag[2][4]=1;


        lqgid = new LQGInfluenceDiagram(nodes, dag);

        PrintWriter writer = new PrintWriter("graph.htm", "UTF-8");
        writer.println(lqgid.generateVisualizationHtml(lqgid.getDag(), true));
        writer.close();
        writer = new PrintWriter("moralgraph.htm", "UTF-8");
        writer.println(lqgid.generateVisualizationHtml(lqgid.getMoralAdjacencyMatrix(), false));
        writer.close();

        for (int i = 0; i < dag.length; i++) {
            System.out.println(Arrays.toString(dag[i]));
        }
    }
}
