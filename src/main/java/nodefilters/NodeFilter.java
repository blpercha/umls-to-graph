package nodefilters;


import graph.Node;

public interface NodeFilter {

    boolean reject(Node node);
}
