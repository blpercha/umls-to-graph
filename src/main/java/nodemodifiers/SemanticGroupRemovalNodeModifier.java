package nodemodifiers;


import graph.Node;
import utils.SemanticGroup;

import java.util.Set;

public class SemanticGroupRemovalNodeModifier implements NodeModifier {
    private final Set<SemanticGroup> allowedGroups;

    public SemanticGroupRemovalNodeModifier(Set<SemanticGroup> allowedGroups) {
        this.allowedGroups = allowedGroups;
    }

    @Override
    public void modify(Node node) {
        node.retainSemanticGroups(allowedGroups);
    }
}
