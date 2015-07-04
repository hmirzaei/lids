package org.uci.lids.graph;

import org.uci.lids.utils.MinHeap;

import java.util.*;

/**
 * Created by hamid on 3/17/15.
 */
public class UndirectedGraph<E> extends AbstractGraph<E, UndirectedVertex<E>> implements Cloneable {
    private List<E> eliminationOrder;

    {
        vertices = new LinkedHashMap<E, UndirectedVertex<E>>();
    }

    public Set<E> getAdjacents(E e) {
        return Collections.unmodifiableSet(vertices.get(e).getAdjacents());
    }

    @Override
    public void addNode(E e) {
        this.vertices.put(e, new UndirectedVertex<E>(e));
    }

    @Override
    public Set<E> getNodes() {
        return Collections.unmodifiableSet(vertices.keySet());
    }

    @Override
    public List<Edge<E>> getEdgeList() {
        List<Edge<E>> retVal = new ArrayList<Edge<E>>();

        for (E parent : vertices.keySet()) {
            for (E child : vertices.get(parent).getAdjacents()) {
                retVal.add(new Edge<E>(vertices.get(parent).content, vertices.get(child).content));
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

    @Override
    public void replaceNode(E e1, E e2) {
        // to do: implement
    }

    public void triangulate(List<Set<E>> temporalOrder) {
        try {
            eliminationOrder = new ArrayList<E>();
            UndirectedGraph<E> tmpGraph = this.clone();
            for (int i = temporalOrder.size() - 1; i >= 0; i--) {
                triangulateSubGraph(temporalOrder.get(i), tmpGraph);
            }
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
    }

    public void triangulate() {
        try {
            UndirectedGraph<E> tmpGraph = this.clone();
            triangulateSubGraph(this.getNodes(), tmpGraph);
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
    }

    private void triangulateSubGraph(Collection<E> nodes, UndirectedGraph<E> tmpGraph) {
        MinHeap<Integer, UndirectedVertex<E>> degreeHeap = new MinHeap<Integer, UndirectedVertex<E>>();


        for (E n : nodes) {
            degreeHeap.put(tmpGraph.vertices.get(n).getDegree(), tmpGraph.vertices.get(n));
        }

        while (degreeHeap.size() > 0) {
            UndirectedVertex<E> minDegreeVertex = degreeHeap.remove().getValue();
            for (E e1 : minDegreeVertex.getAdjacents()) {
                for (E e2 : minDegreeVertex.getAdjacents()) {
                    if (!e1.equals(e2)) {
                        this.addLink(e1, e2);
                        tmpGraph.addLink(e1, e2);
                    }
                }
                if (degreeHeap.contains(tmpGraph.vertices.get(e1))) {
                    degreeHeap.remove(tmpGraph.vertices.get(e1));
                    degreeHeap.put(tmpGraph.vertices.get(e1).getDegree() - 1, tmpGraph.vertices.get(e1));
                }
            }

            tmpGraph.removeNode(minDegreeVertex.getContent());
            eliminationOrder.add(minDegreeVertex.getContent());
        }
    }

    public UndirectedGraph<JunctionTreeNode<E>> getJunctionTree() {
        return this.getJunctionTree(null).junctionTree.getUndirectedCopy();
    }

    public JunctionTreeAndRoot getJunctionTree(List<Set<E>> temporalOrder) {

        DirectedGraph<JunctionTreeNode<E>> junctionTree = new DirectedGraph<JunctionTreeNode<E>>();
        Set<E> visited = new HashSet<E>();
        Set<E> bLambda = null;
        Set<E> prev_b_lambda = new HashSet<E>();
        Set<E> cliqueUnion = new HashSet<E>();
        Map<E, Set<JunctionTreeNode<E>>> assignedCliques = new HashMap<E, Set<JunctionTreeNode<E>>>();
        UndirectedVertex<E> maxCardinalityVertex = null;
        MinHeap<Integer, UndirectedVertex<E>> cardinalities = new MinHeap<Integer, UndirectedVertex<E>>();

        int prevPi = Integer.MAX_VALUE;
        int pi = 0;

        for (UndirectedVertex<E> vertex : this.vertices.values()) {
            cardinalities.put(0, vertex);
        }


        boolean finished = false;
        while (!finished) {
            if (cardinalities.size() > 0) {
                maxCardinalityVertex = cardinalities.remove().getValue();
                for (E e : maxCardinalityVertex.getAdjacents()) {
                    if (cardinalities.contains(this.vertices.get(e))) {
                        int cardinality = cardinalities.remove(this.vertices.get(e));
                        cardinalities.put(cardinality - 1, this.vertices.get(e));
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
                JunctionTreeNode<E> clique = new CliqueNode<E>(prev_b_lambda);

                Set<E> separator = new HashSet<E>();
                for (E lambda : prev_b_lambda) {
                    if (cliqueUnion.contains(lambda))
                        separator.add(lambda);
                }
                junctionTree.addNode(clique);
                if (!separator.isEmpty()) {
                    JunctionTreeNode<E> separatorClique = new SeparatorNode<E>(separator);
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

        JunctionTreeAndRoot jtreeAndRoot = new JunctionTreeAndRoot();
        jtreeAndRoot.junctionTree = junctionTree;

        if (temporalOrder != null) {
            DirectedGraph<JunctionTreeNode<E>> jtree2 = new DirectedGraph<JunctionTreeNode<E>>();
            List<Edge<JunctionTreeNode<E>>> jtreeEdges = new ArrayList<Edge<JunctionTreeNode<E>>>();
            List<CliqueNode<E>> cliqueReplaceList = new ArrayList<CliqueNode<E>>();
            for (E e : eliminationOrder) {
                if (!assignedCliques.get(e).isEmpty()) {
                    CliqueNode<E> clique = (CliqueNode<E>) assignedCliques.get(e).iterator().next();
                    jtree2.addNode(clique);
                    Set<E> seperatorMembers = new HashSet<E>();
                    Set<JunctionTreeNode<E>> candidates = new HashSet<JunctionTreeNode<E>>();
                    boolean isCandidatesInitialized = false;
                    for (E m : clique.getMembers()) {
                        if (assignedCliques.get(m).contains(clique))
                            assignedCliques.get(m).remove(clique);
                        if (!assignedCliques.get(m).isEmpty()) {
                            seperatorMembers.add(m);
                            if (!isCandidatesInitialized) {
                                isCandidatesInitialized = true;
                                candidates.addAll(assignedCliques.get(m));
                            } else {
                                candidates.retainAll(assignedCliques.get(m));
                            }
                        }
                    }
                    if (!candidates.isEmpty()) {
                        SeparatorNode<E> separatorNode = new SeparatorNode<E>(seperatorMembers);
                        jtree2.addNode(separatorNode);
                        jtree2.addLink(clique, separatorNode);
                        jtreeEdges.add(new Edge<JunctionTreeNode<E>>(separatorNode, candidates.iterator().next()));
                    } else {
                        if (!seperatorMembers.isEmpty()) {
                            jtree2.removeNode(clique);
                            CliqueNode<E> newClique = new CliqueNode<E>(seperatorMembers);
                            for (E e1 : seperatorMembers)
                                assignedCliques.get(e1).add(newClique);
                            cliqueReplaceList.add(clique);
                            cliqueReplaceList.add(newClique);
                        } else
                            jtreeAndRoot.rootClique = clique;
                    }
                }
            }
            for (Edge<JunctionTreeNode<E>> edge : jtreeEdges) {
                jtree2.addLink(edge.getNode1(), edge.getNode2());
            }
            ListIterator<CliqueNode<E>> replaceListIter = cliqueReplaceList.listIterator(cliqueReplaceList.size());
            while (replaceListIter.hasPrevious())
                jtree2.replaceNode(replaceListIter.previous(), replaceListIter.previous());
            jtreeAndRoot.junctionTree = jtree2;

            assertStrength(jtree2, temporalOrder, jtreeAndRoot.rootClique);
        }
        return jtreeAndRoot;
    }

    private void addTreeLinks(DirectedGraph<E> g, Set<E> v, E e, Boolean d) {
        v.add(e);
        for (E e2 : vertices.get(e).getAdjacents())
            if (!v.contains(e2)) {
                if (d)
                    g.addLink(e, e2);
                else
                    g.addLink(e2, e);

                addTreeLinks(g, v, e2, d);
            }
    }

    public DirectedGraph<E> getTreeSinkTo(E root) {
        Set<E> visited = new HashSet<E>();
        DirectedGraph<E> result = new DirectedGraph<E>(this.getNodes());
        addTreeLinks(result, visited, root, false);
        return result;
    }

    public DirectedGraph<E> getTreeSourceFrom(E root) {
        Set<E> visited = new HashSet<E>();
        DirectedGraph<E> result = new DirectedGraph<E>(this.getNodes());
        addTreeLinks(result, visited, root, true);
        return result;
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

    public String generateVisualizationHtml(String title) {
        return super.generateVisualizationHtml(false, title);
    }

    private void assertStrength(DirectedGraph<JunctionTreeNode<E>> rootedJunctionTree, List<Set<E>> temporalOrder, JunctionTreeNode<E> root) {
        for (JunctionTreeNode<E> parent : rootedJunctionTree.getParents(root)) {
            JunctionTreeNode<E> grandParent = rootedJunctionTree.getParents(parent).iterator().next();

            HashSet<E> set1 = new HashSet<E>(grandParent.getMembers());
            set1.removeAll(parent.getMembers());
            int separatorMinimumOrder = Integer.MAX_VALUE;
            int i = 0;
            while (separatorMinimumOrder == Integer.MAX_VALUE && i < temporalOrder.size()) {
                for (E node : set1)
                    if (temporalOrder.get(i).contains(node)) {
                        separatorMinimumOrder = i;
                        break;
                    }
                i++;
            }
            for (int j = 0; j < temporalOrder.size(); j++) {
                for (E node : parent.getMembers())
                    if (temporalOrder.get(j).contains(node)) {
                        assert j <= separatorMinimumOrder;
                    }

            }
            assertStrength(rootedJunctionTree, temporalOrder, grandParent);
        }
    }

    public class JunctionTreeAndRoot {
        public DirectedGraph<JunctionTreeNode<E>> junctionTree = null;
        public CliqueNode<E> rootClique = null;
    }


}
