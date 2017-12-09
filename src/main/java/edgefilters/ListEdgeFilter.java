package edgefilters;


import graph.Edge;

import java.util.List;

public class ListEdgeFilter implements EdgeFilter {
    private final List<EdgeFilter> edgeFilters;

    public ListEdgeFilter(List<EdgeFilter> edgeFilters) {
        this.edgeFilters = edgeFilters;
    }

    @Override
    public boolean reject(Edge edge) {
        for (EdgeFilter edgeFilter : edgeFilters) {
            if (edgeFilter.reject(edge)) {
                return true;
            }
        }
        return false;
    }
}
