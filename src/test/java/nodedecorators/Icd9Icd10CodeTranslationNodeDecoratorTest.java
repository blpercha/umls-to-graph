package nodedecorators;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Before;
import org.junit.Test;
import utils.OntologyType;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.HashSet;

import static org.junit.Assert.assertEquals;

public class Icd9Icd10CodeTranslationNodeDecoratorTest {
    private Icd9Icd10CodeTranslationNodeDecorator conversion;

    @Before
    public void setUp() throws Exception {
        String i10ResourceData = "A0220   00320 00000\n" +
                "A0221   00321 00000\n" +
                "A0222   00322 00000\n" +
                "A0223   00323 00000\n" +
                "A0224   00324 00000\n" +
                "A0225   00329 10000\n" +
                "A0229   00329 10000\n" +
                "A028    0038  00000\n" +
                "A029    0039  00000\n" +
                "A033    0043  00000";
        String i9ResourceData = "0010  A000    00000\n" +
                "0011  A001    00000\n" +
                "0019  A009    00000\n" +
                "0020  A0100   10000\n" +
                "01142 A150    10000\n" +
                "04149 B9620   10000\n" +
                "04149 B9629   10000\n" +
                "3898  H918X9  10000\n" +
                "00581 A055    00000";
        conversion = new Icd9Icd10CodeTranslationNodeDecorator(new ByteArrayInputStream(i9ResourceData.getBytes()),
                new ByteArrayInputStream(i10ResourceData.getBytes()), true);
    }

    @Test
    public void testGetIcd10FromIcd9() {
        assertEquals(conversion.getAlternateCodePairs(Pair.of(OntologyType.ICD9CM, "003.20")),
                new HashSet<>(Collections.singletonList(Pair.of(OntologyType.ICD10PCS, "A02.20"))));
        assertEquals(conversion.getAlternateCodePairs(Pair.of(OntologyType.ICD9CM, "001.1")),
                new HashSet<>(Collections.singletonList(Pair.of(OntologyType.ICD10PCS, "A00.1"))));
        assertEquals(conversion.getAlternateCodePairs(Pair.of(OntologyType.ICD9CM, "005.81")),
                new HashSet<>(Collections.singletonList(Pair.of(OntologyType.ICD10PCS, "A05.5"))));
        assertEquals(conversion.getAlternateCodePairs(Pair.of(OntologyType.ICD9CM, "389.8")),
                new HashSet<Pair<OntologyType, String>>());
    }

    @Test
    public void testReadWriteFromResourceFile() throws IOException {
        OutputStream outputStream = new ByteArrayOutputStream();
        conversion.writeResourceFile(outputStream);
        String resourceFileAsWritten = outputStream.toString();
        assertEquals(resourceFileAsWritten, "ICD10PCS,A00.0\tICD9CM,001.0\n" +
                "ICD10PCS,A00.1\tICD9CM,001.1\n" +
                "ICD10PCS,A00.9\tICD9CM,001.9\n" +
                "ICD10PCS,A02.20\tICD9CM,003.20\n" +
                "ICD10PCS,A02.21\tICD9CM,003.21\n" +
                "ICD10PCS,A02.22\tICD9CM,003.22\n" +
                "ICD10PCS,A02.23\tICD9CM,003.23\n" +
                "ICD10PCS,A02.24\tICD9CM,003.24\n" +
                "ICD10PCS,A02.8\tICD9CM,003.8\n" +
                "ICD10PCS,A02.9\tICD9CM,003.9\n" +
                "ICD10PCS,A03.3\tICD9CM,004.3\n" +
                "ICD10PCS,A05.5\tICD9CM,005.81\n" +
                "ICD9CM,001.0\tICD10PCS,A00.0\n" +
                "ICD9CM,001.1\tICD10PCS,A00.1\n" +
                "ICD9CM,001.9\tICD10PCS,A00.9\n" +
                "ICD9CM,003.20\tICD10PCS,A02.20\n" +
                "ICD9CM,003.21\tICD10PCS,A02.21\n" +
                "ICD9CM,003.22\tICD10PCS,A02.22\n" +
                "ICD9CM,003.23\tICD10PCS,A02.23\n" +
                "ICD9CM,003.24\tICD10PCS,A02.24\n" +
                "ICD9CM,003.8\tICD10PCS,A02.8\n" +
                "ICD9CM,003.9\tICD10PCS,A02.9\n" +
                "ICD9CM,004.3\tICD10PCS,A03.3\n" +
                "ICD9CM,005.81\tICD10PCS,A05.5\n");
        InputStream inputStream = new ByteArrayInputStream(resourceFileAsWritten.getBytes());
        conversion = new Icd9Icd10CodeTranslationNodeDecorator(inputStream);
        OutputStream newOutputStream = new ByteArrayOutputStream();
        conversion.writeResourceFile(newOutputStream);
        assertEquals(newOutputStream.toString(), resourceFileAsWritten);
    }
}
