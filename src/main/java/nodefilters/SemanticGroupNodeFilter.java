package nodefilters;


import graph.Node;
import utils.SemanticGroup;

import java.util.HashSet;
import java.util.Set;

public class SemanticGroupNodeFilter implements NodeFilter {
    private Set<SemanticGroup> allowedGroups = new HashSet<>();

    public SemanticGroupNodeFilter(Set<SemanticGroup> allowedGroups) {
        this.allowedGroups = allowedGroups;
    }

    @Override
    public boolean reject(Node node) {
        Set<SemanticGroup> groupsPresent = new HashSet<>(node.getSemanticGroups());
        groupsPresent.retainAll(allowedGroups);
        return groupsPresent.isEmpty();
    }
}
