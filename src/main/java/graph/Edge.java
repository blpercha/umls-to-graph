package graph;

import org.apache.commons.lang3.builder.CompareToBuilder;

import java.io.Serializable;

public class Edge implements Comparable<Edge>, Serializable {
    private final Node parent;
    private final Node child;

    public Edge(Node from, Node to) {
        this.parent = from;
        this.child = to;
    }

    public Node getParentNode() {
        return parent;
    }

    public Node getChildNode() {
        return child;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Edge edge = (Edge) o;

        return child.equals(edge.child) && parent.equals(edge.parent);

    }

    @Override
    public int hashCode() {
        int result = parent.hashCode();
        result = 31 * result + child.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Edge{" +
                "parent=" + parent +
                ", child=" + child +
                '}';
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public int compareTo(Edge o) {
        return new CompareToBuilder().append(getParentNode(), o.getParentNode()).append(getChildNode(), o.getChildNode()).toComparison();
    }

}
