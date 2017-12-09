package nodemodifiers;

import graph.Node;
import org.junit.Test;

import static org.junit.Assert.*;

public class ModifiedStringsNodeModifierTest {
    private final NodeModifier modifier = new ModifiedStringsNodeModifier();

    @Test
    public void testModify() throws Exception {
        Node n1 = new Node();
        n1.addString("Embolism, Venous");
        modifier.modify(n1);
        assertTrue(n1.getStrings().contains("Venous Embolism"));
        assertTrue(n1.getStrings().contains("Embolism Venous"));
        assertFalse(n1.getStrings().contains("venous embolism"));
    }
}