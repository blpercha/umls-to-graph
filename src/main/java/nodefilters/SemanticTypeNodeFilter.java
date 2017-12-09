package nodefilters;

import graph.Node;
import utils.SemanticType;

import java.util.HashSet;
import java.util.Set;

public class SemanticTypeNodeFilter implements NodeFilter {
    private final Set<SemanticType> disallowedTypes;

    public SemanticTypeNodeFilter(Set<SemanticType> disallowedTypes) {
        this.disallowedTypes = disallowedTypes;
    }

    @Override
    public boolean reject(Node node) {
        Set<SemanticType> typesPresent = new HashSet<>(node.getSemanticTypes());
        typesPresent.removeAll(disallowedTypes); // if only disallowed types, typesPresent will now be empty
        return typesPresent.isEmpty();
    }
}
