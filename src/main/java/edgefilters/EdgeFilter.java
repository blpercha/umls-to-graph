package edgefilters;


import graph.Edge;

public interface EdgeFilter {

    boolean reject(Edge edge);
}
