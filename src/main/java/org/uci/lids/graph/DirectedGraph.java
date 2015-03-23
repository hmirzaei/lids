package org.uci.lids.graph;


import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Project: ${PROJECT_NAME}
 * Package: ${PACKAGE_NAME}
 * Created by Hamid Mirzaei on 3/17/15.
 */
public class DirectedGraph<E> extends AbstractGraph<E, DirectedVertex<E>> {
    {
        vertices = new LinkedHashMap<E, DirectedVertex<E>>();
    }

    @Override
    public void addNode(E e) {
        this.vertices.put(e, new DirectedVertex<E>(e));
    }

    @Override
    public List<Edge> getEdgeList() {
        List<Edge> retVal = new ArrayList<Edge>();

        for (E parent : vertices.keySet()) {
            for (E child : vertices.get(parent).getChildren()) {
                retVal.add(new Edge(vertices.get(parent), vertices.get(child)));
            }
        }
        return retVal;
    }

    @Override
    public void removeNode(E node) {
        for (E child : vertices.get(node).getChildren())
            vertices.get(node).removeLinkTo(vertices.get(child));

        for (E parent : vertices.get(node).getParents())
            vertices.get(parent).removeLinkTo(vertices.get(node));

        this.vertices.remove(node);
    }

    public UndirectedGraph getUndirectedCopy() {
        UndirectedGraph<E> ug = new UndirectedGraph<E>();

        for (E e : this.vertices.keySet()) {
            ug.addNode(e);
        }

        for (DirectedVertex<E> parent : this.vertices.values()) {
            for (E child : parent.getChildren()) {
                ug.addLink(parent.getContent(), child);
            }
        }
        return ug;
    }


    public UndirectedGraph getMoralizedUndirectedCopy() {
        UndirectedGraph<E> ug = this.getUndirectedCopy();

        List<E> sources = new ArrayList<E>();
        List<E> destinations = new ArrayList<E>();

        for (DirectedVertex<E> vertex : this.vertices.values()) {
            for (E parent1Content : vertex.getParents()) {
                for (E parent2Content : vertex.getParents()) {
                    if (!parent1Content.equals(parent2Content)) {
                        sources.add(parent1Content);
                        destinations.add(parent2Content);
                    }
                }
            }
        }

        for (int i = 0; i < sources.size(); i++) {
            ug.addLink(sources.get(i), destinations.get(i));
        }
        return ug;
    }


    public String generateVisualizationHtml() {
        return super.generateVisualizationHtml(true);
    }

}
