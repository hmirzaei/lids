package org.uci.lids.graph;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

/**
 * Created by hamid on 3/17/15.
 */
public abstract class AbstractGraph<E, Vertex extends AbstractVertex> {
    protected LinkedHashMap<E, Vertex> vertices;

    public abstract void addNode(E e);

    public abstract Set<E> getNodes();

    public abstract void removeNode(E e);

    public abstract List<Edge<E>> getEdgeList();

    public abstract String generateVisualizationHtml(String title);


    public void addLink(E node1, E node2) {
        this.vertices.get(node1).addLinkTo(vertices.get(node2));
    }

    public void removeLink(E node1, E node2) {
        this.vertices.get(node1).removeLinkTo(vertices.get(node2));
    }


    protected String generateVisualizationHtml(boolean directed, String title) {

        String htmlString = "";
        try {
            htmlString = new Scanner(new File("src/main/resources/graph_template.html")).useDelimiter("\\Z").next();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        StringBuilder nodeString = new StringBuilder();
        for (E node : vertices.keySet()) {

            String shapeString;
            if (node instanceof Visualizable)
                shapeString = ((Visualizable) node).nodeType();
            else
                shapeString = "Generic";

            nodeString.append(String.format("{id: \"%d\", label: \"%s\", group: \"%s\"},\n", node.hashCode(), node.toString(), shapeString));
        }

        StringBuilder edgeString = new StringBuilder();
        for (Edge<E> e : getEdgeList()) {
            if (directed)
                edgeString.append(String.format("{from: %d, to: %s, arrows:'to'},\n", e.getNode1().hashCode(), e.getNode2().hashCode()));
            else
                edgeString.append(String.format("{from: %d, to: %s},\n", e.getNode1().hashCode(), e.getNode2().hashCode()));
        }



        htmlString = htmlString.replace("__node_data__", nodeString.toString());
        htmlString = htmlString.replace("__edge_data__", edgeString.toString());
        htmlString = htmlString.replace("__page_title__", title);

        return htmlString;
    }

    @Override
    public String toString() {
        return Arrays.toString(vertices.values().toArray());
    }
}
