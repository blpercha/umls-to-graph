package utils;

import org.junit.Test;

import static org.junit.Assert.*;

public class SemanticTypeTest {

    @Test
    public void testGetDescription() throws Exception {
        assertEquals(SemanticType.T001.getDescription(), "Organism");
        assertEquals(SemanticType.T061.getDescription(), "Therapeutic or Preventive Procedure");
    }
}