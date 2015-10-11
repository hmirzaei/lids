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
            if (filename.length() > 40) {
                filename = filename.substring(0, 37) + "...";
            }
            PrintWriter writer = new PrintWriter(filename + ".html", "UTF-8");
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
            writer.println(nodes.get(i).getPotentialArray().length);
            for (int j = 0; j < nodes.get(i).getPotentialArray().length; j++) {
                writer.print(j);
                writer.print(" ");
                writer.println(nodes.get(i).getPotentialArray()[j]);
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
            writer.format("bnet.CPD{%d} = tabular_CPD(bnet, %d, %s);", i + 1, i + 1, Arrays.toString(nodes.get(i).getPotentialArray())).println();
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

    public static void writeHuginNet(String filename, DirectedGraph<Node> bn, List<Node> nodes) {
        int N = nodes.size();
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(filename);
        } catch (Exception e) {
            e.printStackTrace();
        }
        writer.println("net {");
        writer.println("  node_size = (40 40);");
        for (Node node1 : nodes) {
            writer.format("  HR_realname_n%s = \"%s\";", node1.getLabel(), node1.getLabel()).println();
        }
        writer.println("}");
        Random random = new Random();

        for (Node node : nodes) {
            String type;
            if (node.getCategory() == Node.Category.Chance)
                type = "node";
            else if (node.getCategory() == Node.Category.Decision)
                type = "decision";
            else
                type = "utility";
            writer.format("%s n%s {", type, node.getLabel()).println();
            writer.println("  HR_Group = \"0\";");
            writer.format("  label = \"%s\";", node.getLabel()).println();
            writer.format("  position = (%d %d);", random.nextInt(400), random.nextInt(400)).println();
            if (node.getCategory() != Node.Category.Utility) {
                writer.println("  states = (");
                for (String state : node.getStates())
                    writer.format("  \"%s\"", state);
                writer.println("  );");
            }
            if (bn.getChildren(node).size() > 0) {
                writer.print("  HR_LinkGroup = \"");
                for (Node child : bn.getChildren(node))
                    writer.format("[n%s:0]", child.getLabel());
                writer.println("\";");
                writer.print("  HR_LinkMode = \"");
                for (Node child : bn.getChildren(node))
                    writer.format("[n%s:0]", child.getLabel());
                writer.println("\";");
            }

            writer.println("}");
        }

        for (Node node : nodes) {
            writer.print("potential (");
            writer.format("n%s", node.getLabel());
            if (!bn.getParents(node).isEmpty()) {
                writer.print(" | ");
                for (Node parent : bn.getParents(node))
                    writer.format("n%s ", parent.getLabel());
            }
            writer.println(") {");
            Potential p = null;
            if (node.getCategory() == Node.Category.Chance) {
                p = new Potential((LinkedHashSet<Node>) bn.getFamily(node), node.getPotentialArray());
                Set<Node> reorderedVariables = new LinkedHashSet<Node>();
                reorderedVariables.add(node);
                ArrayList<Node> parents = new ArrayList<Node>(bn.getParents(node));
                Collections.reverse(parents);
                for (Node parent : parents)
                    reorderedVariables.add(parent);
                p = p.project(reorderedVariables);
            } else if (node.getCategory() == Node.Category.Utility) {
                p = new Potential((LinkedHashSet<Node>) bn.getParents(node), node.getPotentialArray());
                Set<Node> reorderedVariables = new LinkedHashSet<Node>();
                ArrayList<Node> parents = new ArrayList<Node>(bn.getParents(node));
                Collections.reverse(parents);
                for (Node parent : parents)
                    reorderedVariables.add(parent);
                p = p.project(reorderedVariables);
            }

            if (p != null) {
                writer.println("  data = ");
                List<Double> dataList = new ArrayList<Double>();
                for (int i = 0; i < p.getData().length; i++) {
                    dataList.add(p.getData()[i]);
                }
                writer.print(getPotentialDataString(p, node.getCategory(), p.getVariables().size() - 1, dataList.iterator()));
                writer.println(";");
            }
            writer.println("}");

        }

        writer.close();
    }

    private static String getPotentialDataString(Potential potential,
                                                 Node.Category category, int variableIndex,
                                                 Iterator<Double> dataIterator) {
        StringBuilder sb = new StringBuilder();

        if (variableIndex == 0) {
            Node variable = potential.getVariables().iterator().next();
            sb.append("(");
            for (int i = 0; i < variable.getStates().length; i++) {
                sb.append(dataIterator.next().toString()).append(" ");
            }
            sb.append(")");
        } else {
            sb.append("(");
            Iterator<Node> variableIterator = potential.getVariables().iterator();
            Node nextVariable = null;
            for (int i = 0; i <= variableIndex; i++) {
                nextVariable = variableIterator.next();
            }

            assert nextVariable != null;
            for (int i = 0; i < nextVariable.getStates().length; i++) {
                sb.append(getPotentialDataString(potential, category, variableIndex - 1, dataIterator));
            }
            sb.append(")");

        }
        return sb.toString();
    }


}
