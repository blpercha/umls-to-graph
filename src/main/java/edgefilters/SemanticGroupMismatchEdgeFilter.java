package edgefilters;


import graph.Edge;
import utils.SemanticGroup;

import java.util.HashSet;
import java.util.Set;

public class SemanticGroupMismatchEdgeFilter implements EdgeFilter {

    @Override
    public boolean reject(Edge edge) {
        Set<SemanticGroup> overlap = new HashSet<>(edge.getParentNode().getSemanticGroups());
        overlap.retainAll(edge.getChildNode().getSemanticGroups());

        return overlap.isEmpty(); // not a single match
    }
}
