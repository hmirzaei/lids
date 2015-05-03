package org.uci.lids.graph;


import java.util.*;

/**
 * Project: ${PROJECT_NAME}
 * Package: ${PACKAGE_NAME}
 * Created by Hamid Mirzaei on 3/17/15.
 */
public class DirectedGraph<E> extends AbstractGraph<E, DirectedVertex<E>> {
    {
        vertices = new LinkedHashMap<E, DirectedVertex<E>>();
    }

    public DirectedGraph() {
    }

    public DirectedGraph(Set<E> nodes) {
        for (E e : nodes)
            addNode(e);
    }

    private DirectedGraph(LinkedHashMap<E, DirectedVertex<E>> vertices) {
        this.vertices = vertices;
    }

    @Override
    public void addNode(E e) {
        this.vertices.put(e, new DirectedVertex<E>(e));
    }

    @Override
    public Set<E> getNodes() {
        return Collections.unmodifiableSet(vertices.keySet());
    }

    @Override
    public List<Edge<E>> getEdgeList() {
        List<Edge<E>> retVal = new ArrayList<Edge<E>>();

        for (E parent : vertices.keySet()) {
            for (E child : vertices.get(parent).getChildren()) {
                retVal.add(new Edge<E>(vertices.get(parent).content, vertices.get(child).content));
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

    public UndirectedGraph<E> getUndirectedCopy() {
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

    private void doDFSForTopologicalOrdering(LinkedList<E> Output, Set<E> visited, Set<E> ordered, E e) {
        for (E child : vertices.get(e).getChildren()) {
            if (!visited.contains(child)) {
                visited.add(child);
                doDFSForTopologicalOrdering(Output, visited, ordered, child);
            }
        }
        if (!ordered.contains(e)) {
            ordered.add(e);
            Output.addFirst(e);
        }
    }

    public LinkedList<E> getTopologicalOrderedNodes() {
        LinkedList<E> result = new LinkedList<E>();
        Set<E> visited = new HashSet<E>();
        Set<E> ordered = new HashSet<E>();

        for (E e : vertices.keySet()) {
            if (!visited.contains(e)) {
                doDFSForTopologicalOrdering(result, visited, ordered, e);
            }
        }

        return result;
    }

    public Set<E> getParents(E e) {
        return Collections.unmodifiableSet(vertices.get(e).getParents());
    }

    public Set<E> getChildren(E e) {
        return Collections.unmodifiableSet(vertices.get(e).getChildren());
    }

    public Set<E> getFamily(E e) {
        Set<E> family = new LinkedHashSet<E>(getParents(e));
        family.add(e);
        return family;
    }

    public UndirectedGraph<E> getMoralizedUndirectedCopy() {
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

    public String generateVisualizationHtml(String title) {
        return super.generateVisualizationHtml(true, title);
    }

    private void doDFSForConnectedComponents(UndirectedGraph<E> skeleton, LinkedHashMap<E, DirectedVertex<E>> vertices, Set<E> visited, E e) {
        visited.add(e);
        for (E child : skeleton.vertices.get(e).getAdjacents()) {
            if (!visited.contains(child)) {
                vertices.put(child, this.vertices.get(child));
                doDFSForConnectedComponents(skeleton, vertices, visited, child);
            }
        }
    }

    public List<DirectedGraph<E>> getConnectedComponents() {
        UndirectedGraph<E> skeleton = this.getUndirectedCopy();
        List<DirectedGraph<E>> result = new LinkedList<DirectedGraph<E>>();
        Set<E> visited = new HashSet<E>();
        for (E e : skeleton.vertices.keySet()) {
            if (!visited.contains(e)) {
                LinkedHashMap<E, DirectedVertex<E>> vertices = new LinkedHashMap<E, DirectedVertex<E>>();
                vertices.put(e, this.vertices.get(e));
                doDFSForConnectedComponents(skeleton, vertices, visited, e);
                result.add(new DirectedGraph<E>(vertices));
            }
        }
        //System.out.println("result = " + result);
        return result;
    }

}
