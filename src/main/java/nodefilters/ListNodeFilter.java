package nodefilters;


import graph.Node;

import java.util.List;

public class ListNodeFilter implements NodeFilter {
    private final List<NodeFilter> nodeFilters;

    public ListNodeFilter(List<NodeFilter> nodeFilters) {
        this.nodeFilters = nodeFilters;
    }

    @Override
    public boolean reject(Node node) {
        for (NodeFilter nodeFilter : nodeFilters) {
            if (nodeFilter.reject(node)) {
                return true;
            }
        }
        return false;
    }
}
