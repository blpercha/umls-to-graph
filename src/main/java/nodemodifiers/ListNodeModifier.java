package nodemodifiers;

import graph.Node;

import java.util.List;

public class ListNodeModifier implements NodeModifier {
    private final List<NodeModifier> nodeModifiers;

    public ListNodeModifier(List<NodeModifier> nodeModifiers) {
        this.nodeModifiers = nodeModifiers;
    }

    @Override
    public void modify(Node node) {
        for (NodeModifier nodeModifier : nodeModifiers) {
            nodeModifier.modify(node);
        }
    }
}