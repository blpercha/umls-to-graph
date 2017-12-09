package nodemodifiers;

import graph.Node;
import utils.SemanticType;

import java.util.Set;

public class SemanticTypeRemovalNodeModifier implements NodeModifier {
    private final Set<SemanticType> disallowedTypes;

    public SemanticTypeRemovalNodeModifier(Set<SemanticType> disallowedTypes) {
        this.disallowedTypes = disallowedTypes;
    }

    @Override
    public void modify(Node node) {
        node.removeSemanticTypes(disallowedTypes);
    }
}
