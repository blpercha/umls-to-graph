package nodedecorators;

import org.junit.Before;
import org.junit.Test;
import utils.SemanticType;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class SemanticTypeNodeDecoratorTest {
    private SemanticTypeNodeDecorator map;

    @Before
    public void setUp() throws Exception {
        String mrStySample = "C0026106|T033|A2.2|Finding|AT70799168|3840|\n" +
                "C0026106|T048|B2.2.1.2.1.1|Mental or Behavioral Dysfunction|AT41939002|3840|\n" +
                "C2875007|T048|B2.2.1.2.1.1|Mental or Behavioral Dysfunction|AT128848695||\n" +
                "C2875008|T048|B2.2.1.2.1.1|Mental or Behavioral Dysfunction|AT128848696||\n" +
                "C2909477|T048|B2.2.1.2.1.1|Mental or Behavioral Dysfunction|AT128848697|256|\n" +
                "C2909479|T048|B2.2.1.2.1.1|Mental or Behavioral Dysfunction|AT128848698|256|\n" +
                "C2909641|T048|B2.2.1.2.1.1|Mental or Behavioral Dysfunction|AT128848699||\n" +
                "C2909642|T048|B2.2.1.2.1.1|Mental or Behavioral Dysfunction|AT128848700|256|\n" +
                "C2909643|T048|B2.2.1.2.1.1|Mental or Behavioral Dysfunction|AT128848701|256|\n" +
                "C2909644|T048|B2.2.1.2.1.1|Mental or Behavioral Dysfunction|AT128848702|256|\n" +
                "C2909645|T048|B2.2.1.2.1.1|Mental or Behavioral Dysfunction|AT128848703|256|\n" +
                "C2909646|T048|B2.2.1.2.1.1|Mental or Behavioral Dysfunction|AT128848704|256|\n" +
                "C2909647|T048|B2.2.1.2.1.1|Mental or Behavioral Dysfunction|AT128848705||\n" +
                "C2909663|T048|B2.2.1.2.1.1|Mental or Behavioral Dysfunction|AT128848706||\n";
        map = new SemanticTypeNodeDecorator(new ByteArrayInputStream(mrStySample.getBytes()), false);
    }

    @Test
    public void testGetCuiDecorations() {
        assertEquals(map.getSemanticTypesForCui(26106), new HashSet<>(Arrays.asList(SemanticType.T033, SemanticType.T048)));
        assertEquals(map.getSemanticTypesForCui(2909647), new HashSet<>(Collections.singletonList(SemanticType.T048)));
    }

    @Test
    public void testAllCuisInMap() {
        assertArrayEquals(map.allCuis().toArray(), new int[]{26106, 2875007, 2875008, 2909477, 2909479, 2909641,
                2909642, 2909643, 2909644, 2909645, 2909646, 2909647, 2909663})
        ;
    }

    @Test
    public void testAllDecorationsInMap() {
        assertArrayEquals(map.allSemanticTypes().toArray(), new SemanticType[]{SemanticType.T033, SemanticType.T048});
    }

    @Test
    public void testReadWriteResourceFile() throws IOException {
        String resourceFileSample = "1\tT116\n2\tT123\n3\tT131\n4\tT116\n";
        map = new SemanticTypeNodeDecorator(new ByteArrayInputStream(resourceFileSample.getBytes()), true);
        assertEquals(map.getSemanticTypesForCui(1), new HashSet<>(Collections.singletonList(SemanticType.T116)));
        assertEquals(map.getSemanticTypesForCui(2), new HashSet<>(Collections.singletonList(SemanticType.T123)));
        assertEquals(map.getSemanticTypesForCui(3), new HashSet<>(Collections.singletonList(SemanticType.T131)));
        assertEquals(map.getSemanticTypesForCui(4), new HashSet<>(Collections.singletonList(SemanticType.T116)));
        OutputStream outputStream = new ByteArrayOutputStream();
        map.writeResourceFile(outputStream);
        assertEquals(outputStream.toString(), resourceFileSample);
    }
}
