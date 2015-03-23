package org.uci.lids.graph;

import org.uci.lids.utils.MinHeap;

import java.util.*;

/**
 * Created by hamid on 3/17/15.
 */
public class UndirectedGraph<E> extends AbstractGraph<E, UndirectedVertex<E>> implements Cloneable {
    {
        vertices = new LinkedHashMap<E, UndirectedVertex<E>>();
    }

    @Override
    public void addNode(E e) {
        this.vertices.put(e, new UndirectedVertex<E>(e));
    }

    @Override
    public List<Edge> getEdgeList() {
        List<Edge> retVal = new ArrayList<Edge>();

        for (E parent : vertices.keySet()) {
            for (E child : vertices.get(parent).getAdjacents()) {
                retVal.add(new Edge(vertices.get(parent), vertices.get(child)));
            }
        }
        return retVal;
    }

    @Override
    public void removeNode(E node) {
        Object[] adjacents = vertices.get(node).getAdjacents().toArray();
        for (Object adjacent : adjacents)
            removeLink(node, (E) adjacent);
        this.vertices.remove(node);
    }

    public void triangulate() {

        try {
            UndirectedGraph<E> tmpGraph = this.clone();


            Integer[] degrees = new Integer[tmpGraph.numberOfNodes()];
            UndirectedVertex<E>[] vertices = new UndirectedVertex[tmpGraph.numberOfNodes()];

            int ind = 0;

            for (UndirectedVertex<E> vertex : tmpGraph.vertices.values()) {
                degrees[ind] = vertex.getDegree();
                vertices[ind] = vertex;
                ind++;
            }

            MinHeap<Integer, UndirectedVertex<E>> degreeHeap =
                    new MinHeap<Integer, UndirectedVertex<E>>(degrees, vertices);

            while (degreeHeap.heapSize() > 0) {
                UndirectedVertex<E> minDegreeVertex = degreeHeap.removeMin().getValue();

                for (E e1 : minDegreeVertex.getAdjacents()) {
                    for (E e2 : minDegreeVertex.getAdjacents()) {
                        if (!e1.equals(e2)) {
                            this.addLink(e1, e2);
                            tmpGraph.addLink(e1, e2);
                        }
                    }
                    degreeHeap.remove(this.vertices.get(e1));
                    degreeHeap.insert(this.vertices.get(e1).getDegree(), this.vertices.get(e1));
                }
                tmpGraph.removeNode(minDegreeVertex.getContent());
            }

        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }


    }

    public UndirectedGraph<JunctionTreeNode<E>> getJunctionTree() {

        UndirectedGraph<JunctionTreeNode<E>> junctionTree = new UndirectedGraph<JunctionTreeNode<E>>();
        UndirectedVertex<E>[] vertices = new UndirectedVertex[this.numberOfNodes()];
        Integer[] cardInit = new Integer[this.numberOfNodes()];
        Set<E> visited = new HashSet<E>();
        Set<E> bLambda = null;
        Set<E> prev_b_lambda = new HashSet<E>();
        Set<E> cliqueUnion = new HashSet<E>();
        Map<E, Set<JunctionTreeNode<E>>> assignedCliques = new HashMap<E, Set<JunctionTreeNode<E>>>();
        UndirectedVertex<E> maxCardinalityVertex = null;

        int prevPi = Integer.MAX_VALUE;
        int pi = 0;

        int ind = 0;
        for (UndirectedVertex<E> vertex : this.vertices.values()) {
            cardInit[ind] = 0;
            vertices[ind] = vertex;
            ind++;
        }

        MinHeap<Integer, UndirectedVertex<E>> cardinalities =
                new MinHeap<Integer, UndirectedVertex<E>>(cardInit, vertices);

        boolean finished = false;
        while (!finished) {
            if (cardinalities.heapSize() > 0) {
                maxCardinalityVertex = cardinalities.removeMin().getValue();
                for (E e : maxCardinalityVertex.getAdjacents()) {
                    if (cardinalities.contains(this.vertices.get(e))) {
                        int cardinality = cardinalities.remove(this.vertices.get(e));
                        cardinalities.insert(cardinality - 1, this.vertices.get(e));
                    }
                }
                bLambda = new HashSet<E>();

                for (E adjacent : maxCardinalityVertex.getAdjacents()) {
                    if (visited.contains(adjacent))
                        bLambda.add(adjacent);
                }
                pi = bLambda.size();
            } else {
                pi = Integer.MIN_VALUE;
                finished = true;
            }


            if (pi < prevPi + 1) {
                JunctionTreeNode<E> clique = new JunctionTreeNode<E>(prev_b_lambda, JunctionTreeNode.Type.Clique);

                Set<E> separator = new HashSet<E>();
                for (E lambda : prev_b_lambda) {
                    if (cliqueUnion.contains(lambda))
                        separator.add(lambda);
                }
                junctionTree.addNode(clique);
                if (!separator.isEmpty()) {
                    JunctionTreeNode<E> separatorClique = new JunctionTreeNode<E>(separator, JunctionTreeNode.Type.Separator);
                    junctionTree.addNode(separatorClique);
                    junctionTree.addLink(clique, separatorClique);

                    E e = separator.iterator().next();

                    for (JunctionTreeNode<E> cliqueCandid : assignedCliques.get(e)) {
                        boolean foundClique = true;
                        for (E e2 : separator)
                            if (!assignedCliques.get(e2).contains(cliqueCandid))
                                foundClique = false;
                        if (foundClique) {
                            junctionTree.addLink(separatorClique, cliqueCandid);
                            break;
                        }
                    }
                }

                for (E e : prev_b_lambda)
                    if (assignedCliques.containsKey(e))
                        assignedCliques.get(e).add(clique);
                    else {
                        Set<JunctionTreeNode<E>> s = new HashSet<JunctionTreeNode<E>>();
                        s.add(clique);
                        assignedCliques.put(e, s);
                    }

                cliqueUnion.addAll(prev_b_lambda);
            }

            bLambda.add(maxCardinalityVertex.getContent());
            prevPi = pi;
            prev_b_lambda = bLambda;
            visited.add(maxCardinalityVertex.getContent());
        }


        return junctionTree;
    }

    @Override
    protected UndirectedGraph<E> clone() throws CloneNotSupportedException {
        UndirectedGraph<E> ug = new UndirectedGraph<E>();

        for (E e : this.vertices.keySet()) {
            ug.addNode(e);
        }

        for (UndirectedVertex<E> parent : this.vertices.values()) {
            for (E child : parent.getAdjacents()) {
                ug.addLink(parent.getContent(), child);
            }
        }
        return ug;
    }

    public String generateVisualizationHtml() {
        return super.generateVisualizationHtml(false);
    }

}
