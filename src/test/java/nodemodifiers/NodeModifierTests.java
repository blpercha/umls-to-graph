package nodemodifiers;

import graph.UMLSGraph;
import org.junit.Test;
import utils.SemanticGroup;
import utils.SemanticType;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.HashSet;

import static org.junit.Assert.assertEquals;

public class NodeModifierTests {
    private final String decorationsFileSample = "1\t2,3\tCuis123|cuis123\tICD10PCS,1.0|ICD9CM," +
            "10.0\tT059\tPROC\tICD9CM\n" +
            "2\t1,3\tCuis123|cuis123\tICD10PCS,1.0|ICD9CM,10.0\tT059\tPROC\tICD9CM\n" +
            "3\t1,2\tCuis123|cuis123\tICD10PCS,1.0|ICD9CM,10.0\tT059\tPROC\tICD9CM\n" +
            "4\t\tcui4\t\tT047\tDISO|PROC\tICD9CM\n" +
            "5\t\tcui5\t\tT121\tCHEM\tICD9CM\n" +
            "6\t\tcui6\t\t\tPROC\tICD9CM\n" +
            "7\t\tcui7\t\t\t\tICD9CM\n";
    private final String structureFileSample = "1\t4\n1\t5\n2\t4\n2\t5\n3\t4\n3\t5\n5\t6\n5\t7\n";

    @Test
    public void testSemanticGroupRemovalNodeModifier() throws IOException {
        UMLSGraph graph = new UMLSGraph(new ByteArrayInputStream(structureFileSample.getBytes()),
                new ByteArrayInputStream(decorationsFileSample.getBytes()), null, null,
                new SemanticGroupRemovalNodeModifier(new HashSet<>(Collections.singletonList(SemanticGroup.PROC))));
        OutputStream outputStreamStructure = new ByteArrayOutputStream();
        OutputStream outputStreamDecorations = new ByteArrayOutputStream();
        graph.writeResourceFiles(outputStreamStructure, outputStreamDecorations);
        assertEquals(outputStreamStructure.toString(),
                "1\t4\n" +
                        "1\t5\n" +
                        "2\t4\n" +
                        "2\t5\n" +
                        "3\t4\n" +
                        "3\t5\n" +
                        "5\t6\n" +
                        "5\t7\n");
        assertEquals(outputStreamDecorations.toString(),
                "1\t2,3\tCuis123|cuis123\tICD10PCS,1.0|ICD9CM,10.0\tT059\tPROC\tICD9CM\n" +
                        "2\t1,3\tCuis123|cuis123\tICD10PCS,1.0|ICD9CM,10.0\tT059\tPROC\tICD9CM\n" +
                        "3\t1,2\tCuis123|cuis123\tICD10PCS,1.0|ICD9CM,10.0\tT059\tPROC\tICD9CM\n" +
                        "4\t\tcui4\t\tT047\tPROC\tICD9CM\n" +
                        "5\t\tcui5\t\tT121\t\tICD9CM\n" +
                        "6\t\tcui6\t\t\tPROC\tICD9CM\n" +
                        "7\t\tcui7\t\t\t\tICD9CM\n");
    }

    @Test
    public void testSemanticTypeRemovalNodeModifier() throws IOException {
        UMLSGraph graph = new UMLSGraph(new ByteArrayInputStream(structureFileSample.getBytes()),
                new ByteArrayInputStream(decorationsFileSample.getBytes()), null, null,
                new SemanticTypeRemovalNodeModifier(new HashSet<>(Collections.singletonList(SemanticType.T059))));
        OutputStream outputStreamStructure = new ByteArrayOutputStream();
        OutputStream outputStreamDecorations = new ByteArrayOutputStream();
        graph.writeResourceFiles(outputStreamStructure, outputStreamDecorations);
        assertEquals(outputStreamStructure.toString(),
                "1\t4\n" +
                        "1\t5\n" +
                        "2\t4\n" +
                        "2\t5\n" +
                        "3\t4\n" +
                        "3\t5\n" +
                        "5\t6\n" +
                        "5\t7\n");
        assertEquals(outputStreamDecorations.toString(),
                "1\t2,3\tCuis123|cuis123\tICD10PCS,1.0|ICD9CM,10.0\t\tPROC\tICD9CM\n" +
                        "2\t1,3\tCuis123|cuis123\tICD10PCS,1.0|ICD9CM,10.0\t\tPROC\tICD9CM\n" +
                        "3\t1,2\tCuis123|cuis123\tICD10PCS,1.0|ICD9CM,10.0\t\tPROC\tICD9CM\n" +
                        "4\t\tcui4\t\tT047\tDISO|PROC\tICD9CM\n" +
                        "5\t\tcui5\t\tT121\tCHEM\tICD9CM\n" +
                        "6\t\tcui6\t\t\tPROC\tICD9CM\n" +
                        "7\t\tcui7\t\t\t\tICD9CM\n");
    }
}
