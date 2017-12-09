package graph;

import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Before;
import org.junit.Test;
import subgraphs.Subgraph;
import utils.OntologyType;
import utils.SemanticGroup;
import utils.SemanticType;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

public class UMLSGraphTest {
    private UMLSGraph graph = new UMLSGraph();

    @Before
    public void setUp() {
        TestSubgraph subgraph = new TestSubgraph();
        subgraph.addEdges(new ArrayList<>(Arrays.asList(
                Pair.of(4, 2), Pair.of(4, 3), Pair.of(3, 1))));
        graph.addSubgraph(subgraph);
    }

    @Test
    public void testReadResourceFiles() throws IOException {
        String decorationsFileSample = "1\t2,3\tCuis123|cuis123\tICD10PCS,1.0|ICD9CM," +
                "10.0\tT059\tPROC\tICD9CM\n" +
                "2\t1,3\tCuis123|cuis123\tICD10PCS,1.0|ICD9CM,10.0\tT059\tPROC\tICD9CM\n" +
                "3\t1,2\tCuis123|cuis123\tICD10PCS,1.0|ICD9CM,10.0\tT059\tPROC\tICD9CM\n" +
                "4\t\tcui4\t\tT047\tDISO\tICD9CM\n" +
                "5\t\tcui5\t\tT121\tCHEM\tICD9CM\n" +
                "6\t\tcui6\t\t\t\tICD9CM\n" +
                "7\t\tcui7\t\t\t\tICD9CM\n";
        String structureFileSample = "1\t4\n1\t5\n2\t4\n2\t5\n3\t4\n3\t5\n5\t6\n5\t7\n";
        graph = new UMLSGraph(new ByteArrayInputStream(structureFileSample.getBytes()),
                new ByteArrayInputStream(decorationsFileSample.getBytes()));

        assertEquals(graph.getAllNodes().size(), 5);
        assertTrue(graph.graphContainsCui(1));
        assertTrue(graph.graphContainsCui(2));
        assertTrue(graph.graphContainsCui(3));
        assertTrue(graph.graphContainsCui(4));
        assertTrue(graph.graphContainsCui(5));
        assertTrue(graph.graphContainsCui(6));
        assertTrue(graph.graphContainsCui(7));
        assertEquals(graph.getAllEdges().size(), 4);
        assertTrue(graph.graphContainsEdge(new Edge(graph.getNodeForCui(1), graph.getNodeForCui(4))));
        assertTrue(graph.graphContainsEdge(new Edge(graph.getNodeForCui(2), graph.getNodeForCui(4))));
        assertTrue(graph.graphContainsEdge(new Edge(graph.getNodeForCui(3), graph.getNodeForCui(4))));
        assertTrue(graph.graphContainsEdge(new Edge(graph.getNodeForCui(1), graph.getNodeForCui(5))));
        assertTrue(graph.graphContainsEdge(new Edge(graph.getNodeForCui(2), graph.getNodeForCui(5))));
        assertTrue(graph.graphContainsEdge(new Edge(graph.getNodeForCui(3), graph.getNodeForCui(5))));
        assertTrue(graph.graphContainsEdge(new Edge(graph.getNodeForCui(5), graph.getNodeForCui(6))));
        assertTrue(graph.graphContainsEdge(new Edge(graph.getNodeForCui(5), graph.getNodeForCui(7))));
        assertFalse(graph.graphContainsEdge(new Edge(graph.getNodeForCui(4), graph.getNodeForCui(5))));
        assertFalse(graph.graphContainsEdge(new Edge(graph.getNodeForCui(6), graph.getNodeForCui(7))));
        assertEquals(graph.getNodeForCui(1), graph.getNodeForCui(2));

        OutputStream outputStreamStructure = new ByteArrayOutputStream();
        OutputStream outputStreamDecorations = new ByteArrayOutputStream();
        graph.writeResourceFiles(outputStreamStructure, outputStreamDecorations);
        assertEquals(outputStreamStructure.toString(), structureFileSample);
        assertEquals(outputStreamDecorations.toString(), decorationsFileSample);
    }

    @Test
    public void testEdgeIsInCycle() {
        for (Edge e : graph.getAllEdges()) {
            assertFalse(graph.isInCycle(e));
        }

        /* this will add a single new edge from node 1 to node 4 */
        graph.addEdge(1, 4);

        assertEquals(graph.getAllNodes().size(), 4);
        assertEquals(graph.getAllEdges().size(), 4);
        Set<Node> totalNodes = new HashSet<>();
        totalNodes.addAll(graph.getAllNodes());
        for (int i = 1; i <= 4; i++) {
            totalNodes.add(graph.getNodeForCui(i));
        }
        assertEquals(totalNodes.size(), 4);

        for (Edge e : graph.getAllEdges()) {
            if (e.getParentNode().getCuis().contains(1) && e.getChildNode().getCuis().contains(4)) {
                assertTrue(graph.isInCycle(e));
            } else if (e.getParentNode().getCuis().contains(3) && e.getChildNode().getCuis().contains(1)) {
                assertTrue(graph.isInCycle(e));
            } else if (e.getParentNode().getCuis().contains(4) && e.getChildNode().getCuis().contains(3)) {
                assertTrue(graph.isInCycle(e));
            } else {
                assertFalse(graph.isInCycle(e));
            }
        }

        //System.out.println(graph);
    }

    @Test
    public void testTryToAddSelfLoop() {
        graph.addEdge(1, 1);
        assertEquals(graph.getAllEdges().size(), 3);
        for (Edge edge : graph.getAllEdges()) {
            assertFalse(edge.getChildNode().equals(edge.getParentNode()));
        }
    }

    @Test
    public void testRemoveCycles() {
        for (Edge e : graph.getAllEdges()) {
            assertFalse(graph.isInCycle(e));
        }

        /* this will add a single new edge from node 1 to node 4 */
        graph.addEdge(2, 4);
        graph.addEdge(1, 3);

        graph.removeCycles();

        assertEquals(graph.getAllEdges().size(), 1);
        assertEquals(graph.getAllNodes().size(), 2);
        assertEquals(graph.getNodeForCui(2), graph.getNodeForCui(4));
        assertEquals(graph.getNodeForCui(1), graph.getNodeForCui(3));
        for (Edge edge : graph.getAllEdges()) {
            assertTrue(edge.getParentNode().getCuis().equals(new TIntHashSet(Arrays.asList(4, 2))));
            assertTrue(edge.getChildNode().getCuis().equals(new TIntHashSet(Arrays.asList(3, 1))));
        }
    }

    @Test
    public void testRemoveCyclesOneGiantClump() {
        /* this makes the whole graph one big clump */
        graph.addEdge(2, 4);
        graph.addEdge(1, 3);
        graph.addEdge(3, 4);

        graph.removeCycles();

        assertEquals(graph.getAllEdges().size(), 0);
        assertEquals(graph.getAllNodes().size(), 1);
        assertEquals(graph.getNodeForCui(2), graph.getNodeForCui(4));
        assertEquals(graph.getNodeForCui(1), graph.getNodeForCui(3));
        assertEquals(graph.getNodeForCui(4), graph.getNodeForCui(1));
    }

    @Test
    public void testRemoveCyclesNasty() {
        /* this makes the whole graph one big clump */
        graph.addEdge(2, 1);
        graph.addEdge(1, 4);

        graph.removeCycles();

        assertEquals(graph.getAllEdges().size(), 0);
        assertEquals(graph.getAllNodes().size(), 1);
        assertEquals(graph.getNodeForCui(2), graph.getNodeForCui(4));
        assertEquals(graph.getNodeForCui(1), graph.getNodeForCui(3));
        assertEquals(graph.getNodeForCui(4), graph.getNodeForCui(1));

        //System.out.println(graph);
    }

    @Test
    public void testRemoveCyclesNasty2() {
        /* this makes the whole graph one big clump */
        graph.addEdge(1, 3);
        graph.addEdge(1, 4);

        graph.removeCycles();

        assertEquals(graph.getAllEdges().size(), 1);
        assertEquals(graph.getAllNodes().size(), 2);
        assertNotEquals(graph.getNodeForCui(2), graph.getNodeForCui(4));
        assertEquals(graph.getNodeForCui(1), graph.getNodeForCui(3));
        assertEquals(graph.getNodeForCui(4), graph.getNodeForCui(1));

        //System.out.println(graph);
    }

    @Test
    public void testAddTwoSubgraphs() {
        TestSubgraph subgraph = new TestSubgraph();
        subgraph.addEdges(new ArrayList<>(Arrays.asList(Pair.of(4, 2), Pair.of(4, 3), Pair.of(3, 1))));
        TestSubgraph subgraph2 = new TestSubgraph();
        subgraph2.addEdges(Arrays.asList(Pair.of(5, 4), Pair.of(3, 4), Pair.of(1, 2), Pair.of(2, 1)));

        UMLSGraph graph = new UMLSGraph();
        graph.addSubgraph(subgraph);
        graph.addSubgraph(subgraph2);

        // we allow contradictions now, so both should be added
        assertTrue(graph.graphContainsEdge(4, 2));
        assertTrue(graph.graphContainsEdge(4, 3));
        assertTrue(graph.graphContainsEdge(3, 1));
        assertTrue(graph.graphContainsEdge(5, 4));
        assertTrue(graph.graphContainsEdge(3, 4));
        assertTrue(graph.graphContainsEdge(1, 2));
        assertTrue(graph.graphContainsEdge(2, 1));

        assertEquals(graph.getAllEdges().size(), 7);
        assertEquals(graph.getAllNodes().size(), 5);

        //System.out.println(graph);
    }

    @Test
    public void testCollapseNoncycleEdge() {
        Set<Edge> toCollapse = new HashSet<>();
        for (Edge e : graph.getAllEdges()) {
            assertFalse(graph.isInCycle(e));
            if (e.getParentNode().getCuis().equals(new TIntHashSet(Collections.singletonList(4))) &&
                    e.getChildNode().getCuis().equals(new TIntHashSet(Collections.singletonList(2)))) {
                toCollapse.add(e);
            }
        }

        assertEquals(toCollapse.size(), 1);

        for (Edge e : toCollapse) {
            assertTrue(graph.graphContainsEdge(e));
            graph.collapseEdge(e);
        }

        assertEquals(graph.getNodeForCui(2), graph.getNodeForCui(4));
        assertEquals(graph.getAllNodes().size(), 3);
        assertEquals(graph.getAllEdges().size(), 2);
        assertEquals(graph.getOutgoingEdges(4).size(), 1);
        assertEquals(graph.getIncomingEdges(4).size(), 0);
        assertEquals(graph.getOutgoingEdges(3).size(), 1);
        assertEquals(graph.getIncomingEdges(3).size(), 1);
        assertEquals(graph.getOutgoingEdges(2).size(), 1);
        assertEquals(graph.getIncomingEdges(2).size(), 0);
        assertEquals(graph.getOutgoingEdges(1).size(), 0);
        assertEquals(graph.getIncomingEdges(1).size(), 1);

        //System.out.println(graph);

        for (Edge e : graph.getAllEdges()) {
            assertFalse(graph.isInCycle(e));
        }
    }

    @Test
    public void testCollapseCycleEdge() {
        /* this will add a single new edge from node 1 to node 4 */
        graph.addEdge(1, 4);

        Set<Edge> toCollapse = new HashSet<>();
        for (Edge e : graph.getAllEdges()) {
            if (e.getParentNode().containsCui(1) && e.getChildNode().containsCui(4)) {
                assertTrue(graph.isInCycle(e));
                toCollapse.add(e);
            } else if (e.getParentNode().containsCui(3) && e.getChildNode().containsCui(1)) {
                assertTrue(graph.isInCycle(e));
            } else if (e.getParentNode().containsCui(4) && e.getChildNode().containsCui(3)) {
                assertTrue(graph.isInCycle(e));
            } else {
                assertFalse(graph.isInCycle(e));
            }
        }

        assertEquals(toCollapse.size(), 1);

        for (Edge e : toCollapse) {
            assertTrue(graph.graphContainsEdge(e));
            graph.collapseEdge(e);
        }

        /* here we've collapsed one edge and are checking to make sure there is no trace of previous edges */
        assertEquals(graph.getAllNodes().size(), 3);
        assertEquals(graph.getAllEdges().size(), 3);
        assertEquals(graph.getNodeForCui(1), graph.getNodeForCui(4));

        assertEquals(graph.getOutgoingEdges(4).size(), 2);
        assertEquals(graph.getIncomingEdges(4).size(), 1);
        assertEquals(graph.getOutgoingEdges(3).size(), 1);
        assertEquals(graph.getIncomingEdges(3).size(), 1);
        assertEquals(graph.getOutgoingEdges(2).size(), 0);
        assertEquals(graph.getIncomingEdges(2).size(), 1);
        assertEquals(graph.getOutgoingEdges(1).size(), 2);
        assertEquals(graph.getIncomingEdges(1).size(), 1);

        //System.out.println(graph);

        for (Edge e : graph.getAllEdges()) {
            assertTrue(graph.graphContainsEdge(e));
            if (e.getParentNode().getCuis().contains(3) &&
                    (e.getChildNode().getCuis().contains(4) && e.getChildNode().getCuis().contains(1))) {
                assertTrue(graph.isInCycle(e));
            } else if ((e.getParentNode().getCuis().contains(4) && e.getParentNode().getCuis().contains(1))
                    && e.getChildNode().getCuis().contains(3)) {
                assertTrue(graph.isInCycle(e));
            } else if ((e.getParentNode().getCuis().contains(4) && e.getParentNode().getCuis().contains(1)
                    && e.getChildNode().getCuis().contains(2))) {
                assertFalse(graph.isInCycle(e));
            } else {
                throw new IllegalArgumentException("WTF edge is this? " + e.toString());
            }
        }

        /* now collapse the two-cycle that is generated as a result of this */
        toCollapse = new HashSet<>();
        for (Edge edge : graph.getAllEdges()) {
            if (edge.getParentNode().getCuis().equals(new TIntHashSet(Arrays.asList(4, 1)))
                    && edge.getChildNode().getCuis().equals(new TIntHashSet(Collections.singletonList(3)))) {
                toCollapse.add(edge);
            }
        }

        for (Edge edge : toCollapse) {
            graph.collapseEdge(edge);
        }

        assertEquals(graph.getAllNodes().size(), 2);
        assertEquals(graph.getAllEdges().size(), 1);
        assertEquals(graph.getNodeForCui(1), graph.getNodeForCui(4));
        assertEquals(graph.getNodeForCui(1), graph.getNodeForCui(3));

        assertEquals(graph.getOutgoingEdges(4).size(), 1);
        assertEquals(graph.getIncomingEdges(4).size(), 0);
        assertEquals(graph.getOutgoingEdges(3).size(), 1);
        assertEquals(graph.getIncomingEdges(3).size(), 0);
        assertEquals(graph.getOutgoingEdges(2).size(), 0);
        assertEquals(graph.getIncomingEdges(2).size(), 1);
        assertEquals(graph.getOutgoingEdges(1).size(), 1);
        assertEquals(graph.getIncomingEdges(1).size(), 0);

        //System.out.println(graph);
    }

    @Test
    public void testAddOrphanNode() {
        graph.addNode(new Node(new TIntHashSet(Collections.singletonList(20)),
                new HashSet<>(),
                new HashSet<>(Collections.singletonList("orphannode|orphaneynode")),
                new HashSet<>(Collections.singletonList(SemanticType.T001)),
                new HashSet<>(Collections.singletonList(SemanticGroup.DISO)),
                new HashSet<>(Collections.singletonList(OntologyType.SNOMEDCT_US))));
        assertEquals(graph.getAllNodes().size(), 5);
        assertEquals(graph.getAllEdges().size(), 3);
        assertEquals(graph.getAllCuis().size(), 5);
    }

    @Test
    public void testAddSubgraph() {
        assertEquals(graph.getAllNodes().size(), 4);
        assertEquals(graph.getAllEdges().size(), 3);
        assertEquals(graph.getAllCuis().size(), 4);
    }

    @Test
    public void testIncomingOutgoingEdges() {
        assertEquals(graph.getOutgoingEdges(4).size(), 2);
        assertEquals(graph.getIncomingEdges(4).size(), 0);
        assertEquals(graph.getOutgoingEdges(3).size(), 1);
        assertEquals(graph.getIncomingEdges(3).size(), 1);
        assertEquals(graph.getOutgoingEdges(2).size(), 0);
        assertEquals(graph.getIncomingEdges(2).size(), 1);
        assertEquals(graph.getOutgoingEdges(1).size(), 0);
        assertEquals(graph.getIncomingEdges(1).size(), 1);
    }

    @Test
    public void testExpandDownward() {
        TIntSet cuisRepresented = new TIntHashSet();
        for (Node expandedNode : graph.expandNodeDownward(graph.getNodeForCui(4))) {
            cuisRepresented.addAll(expandedNode.getCuis());
        }
        assertEquals(cuisRepresented, new TIntHashSet(Arrays.asList(1, 2, 3, 4)));

        cuisRepresented = new TIntHashSet();
        for (Node expandedNode : graph.expandNodeDownward(graph.getNodeForCui(3))) {
            cuisRepresented.addAll(expandedNode.getCuis());
        }
        assertEquals(cuisRepresented, new TIntHashSet(Arrays.asList(1, 3)));

        cuisRepresented = new TIntHashSet();
        for (Node expandedNode : graph.expandNodeDownward(graph.getNodeForCui(2))) {
            cuisRepresented.addAll(expandedNode.getCuis());
        }
        assertEquals(cuisRepresented, new TIntHashSet(Collections.singletonList(2)));

        cuisRepresented = new TIntHashSet();
        for (Node expandedNode : graph.expandNodeDownward(graph.getNodeForCui(1))) {
            cuisRepresented.addAll(expandedNode.getCuis());
        }
        assertEquals(cuisRepresented, new TIntHashSet(Collections.singletonList(1)));
    }

    @Test
    public void testExpandUpward() {
        TIntSet cuisRepresented = new TIntHashSet();
        for (Node expandedNode : graph.expandNodeUpward(graph.getNodeForCui(4))) {
            cuisRepresented.addAll(expandedNode.getCuis());
        }
        assertEquals(cuisRepresented, new TIntHashSet(Collections.singletonList(4)));

        cuisRepresented = new TIntHashSet();
        for (Node expandedNode : graph.expandNodeUpward(graph.getNodeForCui(3))) {
            cuisRepresented.addAll(expandedNode.getCuis());
        }
        assertEquals(cuisRepresented, new TIntHashSet(Arrays.asList(3, 4)));

        cuisRepresented = new TIntHashSet();
        for (Node expandedNode : graph.expandNodeUpward(graph.getNodeForCui(2))) {
            cuisRepresented.addAll(expandedNode.getCuis());
        }
        assertEquals(cuisRepresented, new TIntHashSet(Arrays.asList(2, 4)));

        cuisRepresented = new TIntHashSet();
        for (Node expandedNode : graph.expandNodeUpward(graph.getNodeForCui(1))) {
            cuisRepresented.addAll(expandedNode.getCuis());
        }
        assertEquals(cuisRepresented, new TIntHashSet(Arrays.asList(1, 3, 4)));
    }

    @Test
    public void testGetChildren() {
        TIntSet cuisRepresented = new TIntHashSet();
        for (Node expandedNode : graph.getChildren(graph.getNodeForCui(4))) {
            cuisRepresented.addAll(expandedNode.getCuis());
        }
        assertEquals(cuisRepresented, new TIntHashSet(Arrays.asList(2, 3)));

        cuisRepresented = new TIntHashSet();
        for (Node expandedNode : graph.getChildren(graph.getNodeForCui(3))) {
            cuisRepresented.addAll(expandedNode.getCuis());
        }
        assertEquals(cuisRepresented, new TIntHashSet(Collections.singletonList(1)));

        cuisRepresented = new TIntHashSet();
        for (Node expandedNode : graph.getChildren(graph.getNodeForCui(2))) {
            cuisRepresented.addAll(expandedNode.getCuis());
        }
        assertEquals(cuisRepresented, new TIntHashSet());

        cuisRepresented = new TIntHashSet();
        for (Node expandedNode : graph.getChildren(graph.getNodeForCui(1))) {
            cuisRepresented.addAll(expandedNode.getCuis());
        }
        assertEquals(cuisRepresented, new TIntHashSet());
    }

    @Test
    public void testGetParents() {
        TIntSet cuisRepresented = new TIntHashSet();
        for (Node expandedNode : graph.getParents(graph.getNodeForCui(4))) {
            cuisRepresented.addAll(expandedNode.getCuis());
        }
        assertEquals(cuisRepresented, new TIntHashSet());

        cuisRepresented = new TIntHashSet();
        for (Node expandedNode : graph.getParents(graph.getNodeForCui(3))) {
            cuisRepresented.addAll(expandedNode.getCuis());
        }
        assertEquals(cuisRepresented, new TIntHashSet(Collections.singletonList(4)));

        cuisRepresented = new TIntHashSet();
        for (Node expandedNode : graph.getParents(graph.getNodeForCui(2))) {
            cuisRepresented.addAll(expandedNode.getCuis());
        }
        assertEquals(cuisRepresented, new TIntHashSet(Collections.singletonList(4)));

        cuisRepresented = new TIntHashSet();
        for (Node expandedNode : graph.getParents(graph.getNodeForCui(1))) {
            cuisRepresented.addAll(expandedNode.getCuis());
        }
        assertEquals(cuisRepresented, new TIntHashSet(Collections.singletonList(3)));
    }

    /////////////////////////////////////////////////////////////////////////////////////

    private class TestSubgraph implements Subgraph {
        final TIntObjectMap<TIntSet> cuiToParents = new TIntObjectHashMap<>();
        final TIntObjectMap<TIntSet> cuiToChildren = new TIntObjectHashMap<>();

        void addEdge(int parent, int child) {
            if (!cuiToParents.containsKey(child)) {
                cuiToParents.put(child, new TIntHashSet());
            }
            if (!cuiToChildren.containsKey(parent)) {
                cuiToChildren.put(parent, new TIntHashSet());
            }
            cuiToParents.get(child).add(parent);
            cuiToChildren.get(parent).add(child);
        }

        void addEdges(List<Pair<Integer, Integer>> edges) {
            for (Pair<Integer, Integer> pair : edges) {
                addEdge(pair.getLeft(), pair.getRight());
            }
        }

        @Override
        public TIntSet getChildren(int cui) {
            if (!cuiToChildren.containsKey(cui)) {
                return new TIntHashSet();
            }
            return cuiToChildren.get(cui);
        }

        @Override
        public TIntSet getParents(int cui) {
            if (!cuiToParents.containsKey(cui)) {
                return new TIntHashSet();
            }
            return cuiToParents.get(cui);
        }

        @Override
        public TIntList allCuis() {
            TIntList allCuis = new TIntArrayList(cuiToChildren.keySet());
            allCuis.addAll(cuiToParents.keySet());
            return allCuis;
        }

    }
}
