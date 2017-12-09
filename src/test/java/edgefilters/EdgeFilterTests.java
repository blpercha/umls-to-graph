package edgefilters;

import graph.Edge;
import graph.UMLSGraph;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;

import static org.junit.Assert.assertEquals;

public class EdgeFilterTests {
    @Test
    public void testPruneEdgesWithMissingNodes() throws IOException {
        String decorationsFileSample = "1\t2,3\tCuis123|cuis123\tICD10PCS,1.0|ICD9CM," +
                "10.0\tT059\tPROC\tICD9CM\n" +
                "2\t1,3\tCuis123|cuis123\tICD10PCS,1.0|ICD9CM,10.0\tT059\tPROC\tICD9CM\n" +
                "3\t1,2\tCuis123|cuis123\tICD10PCS,1.0|ICD9CM,10.0\tT059\tPROC\tICD9CM\n" +
                "5\t\tcui5\t\tT121\tCHEM\tICD9CM\n" +
                "6\t\tcui6\t\t\tCHEM\tICD9CM\n" +
                "7\t\tcui7\t\t\tPROC\tICD9CM\n";
        String structureFileSample = "1.0.0\n1\t4\n1\t5\n2\t4\n2\t5\n3\t4\n3\t5\n5\t6\n5\t7\n";
        UMLSGraph graph = new UMLSGraph(new ByteArrayInputStream(structureFileSample.getBytes()),
                new ByteArrayInputStream(decorationsFileSample.getBytes()),
                null, null, null);

        assertEquals(graph.getAllEdges().size(), 3);
        assertEquals(graph.getAllEdges(),
                new HashSet<>(Arrays.asList(
                        new Edge(graph.getNodeForCui(1), graph.getNodeForCui(5)),
                        new Edge(graph.getNodeForCui(5), graph.getNodeForCui(6)),
                        new Edge(graph.getNodeForCui(5), graph.getNodeForCui(7)))));
    }

    @Test
    public void testSemanticGroupMismatchEdgeFilter() throws IOException {
        String decorationsFileSample = "1\t2,3\tCuis123|cuis123\tICD10PCS,1.0|ICD9CM," +
                "10.0\tT059\tPROC\tICD9CM\n" +
                "2\t1,3\tCuis123|cuis123\tICD10PCS,1.0|ICD9CM,10.0\tT059\tPROC\tICD9CM\n" +
                "3\t1,2\tCuis123|cuis123\tICD10PCS,1.0|ICD9CM,10.0\tT059\tPROC\tICD9CM\n" +
                "4\t\tcui4\t\tT047\tDISO|PROC\tICD9CM\n" +
                "5\t\tcui5\t\tT121\tCHEM\tICD9CM\n" +
                "6\t\tcui6\t\t\tCHEM\tICD9CM\n" +
                "7\t\tcui7\t\t\tPROC\tICD9CM\n";
        String structureFileSample = "1\t4\n1\t5\n2\t4\n2\t5\n3\t4\n3\t5\n5\t6\n5\t7\n";
        UMLSGraph graph = new UMLSGraph(new ByteArrayInputStream(structureFileSample.getBytes()),
                new ByteArrayInputStream(decorationsFileSample.getBytes()), null,
                new SemanticGroupMismatchEdgeFilter(), null);
        assertEquals(graph.getAllEdges().size(), 2);
        assertEquals(graph.getAllEdges(),
                new HashSet<>(Arrays.asList(
                        new Edge(graph.getNodeForCui(1), graph.getNodeForCui(4)),
                        new Edge(graph.getNodeForCui(5), graph.getNodeForCui(6)))));
    }
}
