package org.uci.lids.utils;

import org.uci.lids.Node;
import org.uci.lids.graph.AbstractGraph;
import org.uci.lids.graph.DirectedGraph;
import org.uci.lids.graph.Edge;

import java.io.PrintWriter;
import java.util.*;

/**
 * Created by Hamid Mirzaei on 4/6/15.
 */
public class Misc {
    public static <T extends Comparable<? super T>> List<T> asSortedList(Collection<T> c) {
        return Misc.asSortedList(c, new Comparator<T>() {
            public int compare(T o1, T o2) {
                return o1.compareTo(o2);
            }
        });
    }

    public static <T extends Comparable<? super T>> List<T> asSortedList(Collection<T> c, Comparator<? super T> comparator) {
        List<T> list = new ArrayList<T>(c);
        java.util.Collections.sort(list, comparator);
        return list;
    }

    public static <K, V extends Comparable<? super V>> Map<K, V>
    sortByValue(Map<K, V> map) {
        List<Map.Entry<K, V>> list =
                new LinkedList<Map.Entry<K, V>>(map.entrySet());
        Collections.sort(list, new Comparator<Map.Entry<K, V>>() {
            public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
                return (o1.getValue()).compareTo(o2.getValue());
            }
        });

        Map<K, V> result = new LinkedHashMap<K, V>();
        for (Map.Entry<K, V> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }

    public static void saveGraphOnDisk(String filename, AbstractGraph g) {
        saveGraphOnDisk(filename, filename, g);
    }

    public static void saveGraphOnDisk(String filename, String title, AbstractGraph g) {
        try {
            PrintWriter writer = new PrintWriter(filename, "UTF-8");
            writer.println(g.generateVisualizationHtml(title));
            writer.close();
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }

    public static void writeLibDaiInputfile(String filename, DirectedGraph bn, List<Node> nodes, int noStates) {
        int N = nodes.size();
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(filename);
        } catch (Exception e) {
            e.printStackTrace();
        }
        writer.println(N);
        writer.println();
        for (int i = 0; i < N; i++) {
            Set<Node> parents = bn.getParents(nodes.get(i));

            writer.println(parents.size() + 1);
            for (Node parent : parents) {
                writer.print(nodes.indexOf(parent));
                writer.print(" ");
            }
            writer.println(i);

            for (int j = 0; j < parents.size() + 1; j++) {
                writer.print(noStates);
                writer.print(" ");
            }
            writer.println();
            writer.println(nodes.get(i).getPotential().length);
            for (int j = 0; j < nodes.get(i).getPotential().length; j++) {
                writer.print(j);
                writer.print(" ");
                writer.println(nodes.get(i).getPotential()[j]);
            }
            writer.println();
        }
        writer.close();
    }

    public static void writeBntScript(String filename, DirectedGraph<Node> bn, List<Node> nodes, Map<Node, Potential> marginals) {
        int N = nodes.size();
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(filename);
        } catch (Exception e) {
            e.printStackTrace();
        }
        int[][] dag = new int[N][N];
        List<Edge<Node>> edges = bn.getEdgeList();
        for (Edge<Node> e : edges) {
            dag[nodes.indexOf(e.getNode1())][nodes.indexOf(e.getNode2())] = 1;
        }

        int[] noStatesArray = new int[N];
        for (int i = 0; i < N; i++) {
            noStatesArray[i] = nodes.get(i).getStates().length;
        }

        writer.println("dag=[");
        for (int[] a : dag)
            writer.println(Arrays.toString(a));
        writer.println("];");
        writer.println();
        writer.format("bnet = mk_bnet(dag, %s, 'discrete', 1:%d);", Arrays.toString(noStatesArray), nodes.size()).println();
        writer.println();
        for (int i = 0; i < N; i++) {
            writer.format("bnet.CPD{%d} = tabular_CPD(bnet, %d, %s);", i + 1, i + 1, Arrays.toString(nodes.get(i).getPotential())).println();
        }
        writer.println();
        writer.println("tic;");
        writer.println("engine = jtree_inf_engine(bnet);");
        writer.format("evidence = cell(1,%d);", nodes.size()).println();
        writer.println("[engine, loglik] = enter_evidence(engine, evidence);");
        writer.println();
        for (int i = 0; i < N; i++) {
            writer.format("marg%d = marginal_nodes(engine, %d);", i + 1, i + 1).println();
        }
        writer.println();
        writer.println("toc;");

        for (int i = 0; i < N; i++) {
            writer.format("bnt_%s = marg%d.T;", nodes.get(i).getLabel(), i + 1).println();
        }
        writer.println();

        writer.println("error=0;");
        for (Map.Entry<Node, Potential> entry : marginals.entrySet()) {
            writer.format("lids_%s=[", entry.getKey().getLabel()).println();
            for (int i = 0; i < entry.getValue().getData().length; i++) {
                writer.format("%.10f", entry.getValue().getData()[i]).println();
            }
            writer.print("];");
            writer.format("error = error+sum((lids_%s - bnt_%s).^2);", entry.getKey().getLabel(), entry.getKey().getLabel()).println();
        }
        writer.format("error = sqrt(error)").println();

        writer.close();
    }


}
