package org.uci.lids.graph;

import org.apache.log4j.Logger;
import org.uci.lids.utils.MinHeap;
import org.uci.lids.utils.Misc;

import java.util.*;

/**
 * Created by hamid on 3/17/15.
 */
public class UndirectedGraph<E> extends AbstractGraph<E, UndirectedVertex<E>> implements Cloneable {
    final static Logger logger = Logger.getLogger(UndirectedGraph.class);

    private List<E> eliminationOrder;

    {
        vertices = new LinkedHashMap<E, UndirectedVertex<E>>();
    }

    public UndirectedGraph() {
    }

    public UndirectedGraph(Set<E> nodes) {
        for (E e : nodes)
            addNode(e);
    }

    private UndirectedGraph(LinkedHashMap<E, UndirectedVertex<E>> vertices) {
        this.vertices = vertices;
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
            logger.error(e);
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
            if (tmpGraph.vertices.containsKey(n))
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

        if (temporalOrder == null) {
            JunctionTreeAndRoot result = new JunctionTreeAndRoot();
            result.junctionTree = junctionTree;
            return result;
        } else {
            JunctionTreeAndRoot jtreeAndRoot = new JunctionTreeAndRoot();
            jtreeAndRoot.junctionTree = junctionTree;

            logger.debug("eliminationOrder = " + eliminationOrder);

            Map<E, Integer> nodeValues = new HashMap<E, Integer>();
            int value = 0;
            for (int i = eliminationOrder.size() - 1; i >= 0; i--)
                nodeValues.put(eliminationOrder.get(i), value++);

            Map<JunctionTreeNode<E>, Set<E>> visitedNodes = new HashMap<JunctionTreeNode<E>, Set<E>>();
            Map<JunctionTreeNode<E>, Integer> cliqueNumbers = new HashMap<JunctionTreeNode<E>, Integer>();
            Map<JunctionTreeNode<E>, Set<JunctionTreeNode<E>>> commonCliques = new HashMap<JunctionTreeNode<E>, Set<JunctionTreeNode<E>>>();

            for (JunctionTreeNode<E> e : junctionTree.getNodes())
                if (e instanceof CliqueNode) {
                    visitedNodes.put(e, new HashSet<E>());
                    cliqueNumbers.put(e, -1);
                    commonCliques.put(e, new HashSet<JunctionTreeNode<E>>());
                }


            ListIterator<E> li = eliminationOrder.listIterator(eliminationOrder.size());
            while (li.hasPrevious()) {
                E n = li.previous();
                for (JunctionTreeNode<E> c : assignedCliques.get(n)) {
                    if (visitedNodes.get(c).isEmpty())
                        cliqueNumbers.put(c, nodeValues.get(n));
                    else if (!commonCliques.get(c).isEmpty())
                        for (JunctionTreeNode<E> cc : commonCliques.get(c)) {
                            Set<E> ccMinusCMembers = new HashSet<E>(visitedNodes.get(cc));
                            ccMinusCMembers.removeAll(c.getMembers());
                            if (!ccMinusCMembers.isEmpty()) {
                                cliqueNumbers.put(c, nodeValues.get(n));
                                break;
                            }
                        }
                    if (visitedNodes.get(c).isEmpty())
                        commonCliques.get(c).addAll(assignedCliques.get(n));
                    else
                        commonCliques.get(c).retainAll(assignedCliques.get(n));
                    commonCliques.get(c).remove(c);

                    visitedNodes.get(c).add(n);
                }
            }

            cliqueNumbers = Misc.sortByValue(cliqueNumbers);
            logger.debug("cliqueNumbers = " + cliqueNumbers);

            cliqueUnion = new HashSet<E>();
            assignedCliques = new HashMap<E, Set<JunctionTreeNode<E>>>();
            DirectedGraph<JunctionTreeNode<E>> junctionTree2 = new DirectedGraph<JunctionTreeNode<E>>();

            for (JunctionTreeNode<E> clique : cliqueNumbers.keySet()) {
                Set<E> separator = new HashSet<E>();
                for (E e : clique.getMembers()) {
                    if (cliqueUnion.contains(e))
                        separator.add(e);
                }
                junctionTree2.addNode(clique);
                if (!separator.isEmpty()) {
                    JunctionTreeNode<E> separatorClique = new SeparatorNode<E>(separator);
                    junctionTree2.addNode(separatorClique);
                    junctionTree2.addLink(clique, separatorClique);
                    E e = separator.iterator().next();
                    for (JunctionTreeNode<E> cliqueCandid : assignedCliques.get(e)) {
                        boolean foundClique = true;
                        for (E e2 : separator)
                            if (!assignedCliques.get(e2).contains(cliqueCandid))
                                foundClique = false;
                        if (foundClique) {
                            junctionTree2.addLink(separatorClique, cliqueCandid);
                            break;
                        }
                    }
                }
                for (E e : clique.getMembers())
                    if (assignedCliques.containsKey(e))
                        assignedCliques.get(e).add(clique);
                    else {
                        Set<JunctionTreeNode<E>> s = new HashSet<JunctionTreeNode<E>>();
                        s.add(clique);
                        assignedCliques.put(e, s);
                    }

                cliqueUnion.addAll(clique.getMembers());
            }
            jtreeAndRoot.rootClique = (CliqueNode<E>) cliqueNumbers.keySet().iterator().next();
            assertStrength(junctionTree2, temporalOrder, jtreeAndRoot.rootClique);
            jtreeAndRoot.junctionTree = junctionTree2;

            return jtreeAndRoot;
        }
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

    @Override
    public UndirectedGraph<E> getSubGraph(Set<E> nodes) {
        UndirectedGraph<E> result = new UndirectedGraph<E>(nodes);
        for (E e : nodes) for (E a : this.getAdjacents(e)) if (nodes.contains(a)) result.addLink(e, a);
        return result;
    }

    private void assertStrength(DirectedGraph<JunctionTreeNode<E>> rootedJunctionTree, List<Set<E>> temporalOrder, JunctionTreeNode<E> root) {
        for (JunctionTreeNode<E> parent : rootedJunctionTree.getParents(root)) {
            JunctionTreeNode<E> grandParent = rootedJunctionTree.getParents(parent).iterator().next();

            assert checkStrengthCondition(temporalOrder, parent, grandParent) : "S: " + parent.toString() + ", C: " + grandParent.toString();
            assertStrength(rootedJunctionTree, temporalOrder, grandParent);
        }
    }

    private boolean checkStrengthCondition(List<Set<E>> temporalOrder, JunctionTreeNode<E> separator, JunctionTreeNode<E> clique) {
        HashSet<E> set1 = new HashSet<E>(clique.getMembers());
        set1.removeAll(separator.getMembers());
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

        for (int j = 0; j < temporalOrder.size(); j++)
            for (E node : separator.getMembers())
                if (temporalOrder.get(j).contains(node))
                    if (j > separatorMinimumOrder)
                        return false;

        return true;
    }

    private void doDFSForConnectedComponents(UndirectedGraph<E> skeleton, LinkedHashMap<E, UndirectedVertex<E>> vertices, Set<E> visited, E e) {
        visited.add(e);
        for (E child : skeleton.vertices.get(e).getAdjacents()) {
            if (!visited.contains(child)) {
                vertices.put(child, this.vertices.get(child));
                doDFSForConnectedComponents(skeleton, vertices, visited, child);
            }
        }
    }

    public List<UndirectedGraph<E>> getConnectedComponents() {
        UndirectedGraph<E> skeleton = this;
        List<UndirectedGraph<E>> result = new LinkedList<UndirectedGraph<E>>();
        Set<E> visited = new HashSet<E>();
        for (E e : skeleton.vertices.keySet()) {
            if (!visited.contains(e)) {
                LinkedHashMap<E, UndirectedVertex<E>> vertices = new LinkedHashMap<E, UndirectedVertex<E>>();
                vertices.put(e, this.vertices.get(e));
                doDFSForConnectedComponents(skeleton, vertices, visited, e);
                result.add(new UndirectedGraph<E>(vertices));
            }
        }
        return result;
    }

    public class JunctionTreeAndRoot {
        public DirectedGraph<JunctionTreeNode<E>> junctionTree = null;
        public CliqueNode<E> rootClique = null;
    }

}
