package org.uci.lids.graph;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Scanner;

/**
 * Created by hamid on 3/17/15.
 */
public abstract class AbstractGraph<E, Vertex extends AbstractVertex> {
    protected LinkedHashMap<E, Vertex> vertices;

    public abstract void addNode(E e);

    public abstract void removeNode(E e);

    public abstract List<Edge> getEdgeList();


    public void addLink(E node1, E node2) {
        this.vertices.get(node1).addLinkTo(vertices.get(node2));
    }

    public void removeLink(E node1, E node2) {
        this.vertices.get(node1).removeLinkTo(vertices.get(node2));
    }

    public int numberOfNodes() {
        return vertices.size();
    }


    protected String generateVisualizationHtml(boolean directed) {

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
            edgeString.append(String.format("{from: %d, to: %s},\n", e.getVertex1().hashCode(), e.getVertex2().hashCode()));
        }

        if (directed)
            htmlString = htmlString.replace("__edge_style", "edges: {style: 'arrow',},");
        else
            htmlString = htmlString.replace("__edge_style", "");


        htmlString = htmlString.replace("__node_data", nodeString.toString());
        htmlString = htmlString.replace("__edge_data", edgeString.toString());

        return htmlString;
    }

    @Override
    public String toString() {
        return Arrays.toString(vertices.values().toArray());
    }
}
