package graph;

import edgefilters.EdgeFilter;
import gnu.trove.TDecorators;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import nodedecorators.NodeDecorator;
import nodefilters.NodeFilter;
import nodemodifiers.NodeModifier;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import subgraphs.Subgraph;
import utils.OntologyType;
import utils.SemanticGroup;
import utils.SemanticType;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public class UMLSGraph {
    private final Map<Node, Set<Edge>> outgoingEdges = new HashMap<>();
    private final Map<Node, Set<Edge>> incomingEdges = new HashMap<>();
    private final Set<Edge> allEdges = new HashSet<>();
    private final TIntObjectMap<Node> cuiToNodeMap = new TIntObjectHashMap<>();

    public UMLSGraph() {
    }

    UMLSGraph(InputStream inputStreamStructure, InputStream inputStreamDecorations) throws IOException {
        readResourceFiles(inputStreamStructure, inputStreamDecorations);
    }

    public UMLSGraph(InputStream inputStreamStructure, InputStream inputStreamDecorations,
                     NodeFilter nodeFilter, EdgeFilter edgeFilter, NodeModifier nodeModifier) throws IOException {
        readResourceFiles(inputStreamStructure, inputStreamDecorations, nodeFilter, edgeFilter, nodeModifier);
    }

    boolean graphContainsCui(int cui) {
        return cuiToNodeMap.containsKey(cui);
    }

    private boolean graphContainsNode(Node node) {
        return outgoingEdges.containsKey(node) || incomingEdges.containsKey(node);
    }

    boolean graphContainsEdge(Edge edge) {
        return allEdges.contains(edge);
    }

    boolean graphContainsEdge(int parentCui, int childCui) {
        return graphContainsEdge(new Edge(getNodeForCui(parentCui), getNodeForCui(childCui)));
    }

    void addEdge(int parentCui, int childCui) {
        if (!graphContainsCui(parentCui) || !graphContainsCui(childCui)) {
            System.err.println("Warning: cuis not in graph: " + parentCui + "\t" + childCui);
            return;
        }
        addEdge(getNodeForCui(parentCui), getNodeForCui(childCui));
    }

    private void addEdge(Node parentNode, Node childNode) {
        if (!graphContainsNode(parentNode)) {
            throw new IllegalArgumentException("Can't add edge to nonexistent node: " + parentNode);
        }
        if (!graphContainsNode(childNode)) {
            throw new IllegalArgumentException("Can't add edge to nonexistent node: " + childNode);
        }
        addEdge(new Edge(parentNode, childNode));
    }

    private void addEdge(Edge newEdge) {
        if (newEdge.getParentNode().equals(newEdge.getChildNode())) {
            return; /* do not add self loops */
        }

        outgoingEdges.get(newEdge.getParentNode()).add(newEdge);
        incomingEdges.get(newEdge.getChildNode()).add(newEdge);
        allEdges.add(newEdge);
    }

    void addNode(final Node node) {
        TIntSet cuis = new TIntHashSet(node.getCuis());
        cuis.retainAll(cuiToNodeMap.keySet());
        if (!cuis.isEmpty()) {
            throw new IllegalArgumentException("Can't add new node: " + node + " due to CUI overlap.");
        }

        node.getCuis().forEach(cui -> {
            cuiToNodeMap.put(cui, node);
            return true;
        });

        outgoingEdges.put(node, new HashSet<>());
        incomingEdges.put(node, new HashSet<>());
    }

    public Set<Edge> getAllEdges() {
        return allEdges;
    }

    private Set<Edge> getOutgoingEdges(Node node) {
        if (!outgoingEdges.containsKey(node)) {
            System.err.println("Outgoing edges map does not contain key: " + node);
            return new HashSet<>();
        }
        return outgoingEdges.get(node);
    }

    Set<Edge> getOutgoingEdges(int cui) {
        return getOutgoingEdges(getNodeForCui(cui));
    }

    private Set<Edge> getIncomingEdges(Node node) {
        if (!incomingEdges.containsKey(node)) {
            return new HashSet<>();
        }
        return incomingEdges.get(node);
    }

    Set<Edge> getIncomingEdges(int cui) {
        return getIncomingEdges(getNodeForCui(cui));
    }

    private Set<Node> expandNodesUpward(Set<Node> nodes) {
        final Set<Node> nodesExpansion = new HashSet<>();
        for (Node node : nodes) {
            nodesExpansion.addAll(expandNodeUpward(node));
        }
        return nodesExpansion;
    }

    Set<Node> expandNodeUpward(Node node) {
        final Set<Node> nodeExpansion = new HashSet<>();
        nodeExpansion.addAll(expandNodesUpward(getParents(node)));
        nodeExpansion.add(node);
        return nodeExpansion;
    }

    private Set<Node> expandNodesDownward(Set<Node> nodes) {
        final Set<Node> nodesExpansion = new HashSet<>();
        for (Node node : nodes) {
            nodesExpansion.addAll(expandNodeDownward(node));
        }
        return nodesExpansion;
    }

    Set<Node> expandNodeDownward(Node node) {
        final Set<Node> nodeExpansion = new HashSet<>();
        nodeExpansion.addAll(expandNodesDownward(getChildren(node)));
        nodeExpansion.add(node);
        return nodeExpansion;
    }

    Set<Node> getChildren(Node node) {
        final Set<Node> children = new HashSet<>();
        for (Edge e : getOutgoingEdges(node)) {
            children.add(e.getChildNode());
        }
        return children;
    }

    Set<Node> getParents(Node node) {
        final Set<Node> parents = new HashSet<>();
        for (Edge e : getIncomingEdges(node)) {
            parents.add(e.getParentNode());
        }
        return parents;
    }

    public Node getNodeForCui(int cui) {
        if (!cuiToNodeMap.containsKey(cui)) {
            return null;
        }
        return cuiToNodeMap.get(cui);
    }

    public void decorateNodes(final NodeDecorator nodeDecorator) {
        List<Node> allNodes = new ArrayList<>(getAllNodes());
        for (Node node : allNodes) {
            nodeDecorator.decorateNode(node);
        }
    }

    boolean isInCycle(Edge edge) {
        final Set<Edge> visited = new HashSet<>();
        for (Edge childEdge : getOutgoingEdges(edge.getChildNode())) {
            dfs(childEdge, visited);
        }
        return visited.contains(edge);
    }

    private void dfs(Edge edge, final Set<Edge> visited) {
        visited.add(edge);
        Set<Edge> children = getOutgoingEdges(edge.getChildNode());
        for (Edge childEdge : children) {
            if (!visited.contains(childEdge)) {
                dfs(childEdge, visited);
            }
        }
    }

    public void addSubgraph(final Subgraph subgraph) {
        /* add nodes */
        subgraph.allCuis().forEach(cui -> {
            if (cuiToNodeMap.containsKey(cui)) {
                return true; /* already added in previous subgraph */
            }
            Node node = new Node();
            node.addCui(cui);
            outgoingEdges.put(node, new HashSet<>());
            incomingEdges.put(node, new HashSet<>());
            cuiToNodeMap.put(cui, node);
            return true;
        });

        /* add edges, ensuring no contradictions with what's already there */
        final Set<Edge> newEdges = new HashSet<>();
        for (final Node node : getAllNodes()) {
            node.getCuis().forEach(cui -> {
                subgraph.getChildren(cui).forEach(childCui -> {
                    Node childNode = cuiToNodeMap.get(childCui);
                    newEdges.add(new Edge(node, childNode));
                    return true;
                });
                subgraph.getParents(cui).forEach(parentCui -> {
                    Node parentNode = cuiToNodeMap.get(parentCui);
                    newEdges.add(new Edge(parentNode, node));
                    return true;
                });
                return true;
            });
        }
        for (Edge newEdge : newEdges) {
            addEdge(newEdge);
        }
    }

    boolean collapseEdge(Edge edge) {
        if (!graphContainsEdge(edge)) {
            return false;
        }

        final Node parent = edge.getParentNode();
        final Node child = edge.getChildNode();

        /* create new combined node with information from both nodes of edge */
        final Node newNode = new Node();
        newNode.addNodeInfo(parent);
        newNode.addNodeInfo(child);

        /* get information about outgoing and incoming edges for the new combined node */
        Set<Edge> newOutgoingEdges = new HashSet<>();
        for (Edge outgoingEdge : getOutgoingEdges(parent)) {
            if (outgoingEdge.getChildNode().equals(parent) || outgoingEdge.getChildNode().equals(child)) {
                continue;
            }
            newOutgoingEdges.add(new Edge(newNode, outgoingEdge.getChildNode()));
        }
        for (Edge outgoingEdge : getOutgoingEdges(child)) {
            if (outgoingEdge.getChildNode().equals(parent) || outgoingEdge.getChildNode().equals(child)) {
                continue;
            }
            newOutgoingEdges.add(new Edge(newNode, outgoingEdge.getChildNode()));
        }

        Set<Edge> newIncomingEdges = new HashSet<>();
        for (Edge incomingEdge : getIncomingEdges(parent)) {
            if (incomingEdge.getParentNode().equals(parent) || incomingEdge.getParentNode().equals(child)) {
                continue;
            }
            newIncomingEdges.add(new Edge(incomingEdge.getParentNode(), newNode));
        }
        for (Edge incomingEdge : getIncomingEdges(child)) {
            if (incomingEdge.getParentNode().equals(parent) || incomingEdge.getParentNode().equals(child)) {
                continue;
            }
            newIncomingEdges.add(new Edge(incomingEdge.getParentNode(), newNode));
        }

        /* remove old child and parent nodes from everything */
        removeAllTrace(parent, child);

        /* add new node to graph */
        outgoingEdges.put(newNode, newOutgoingEdges);
        for (Edge newEdge : newOutgoingEdges) {
            incomingEdges.get(newEdge.getChildNode()).add(newEdge);
        }
        incomingEdges.put(newNode, newIncomingEdges);
        for (Edge newEdge : newIncomingEdges) {
            outgoingEdges.get(newEdge.getParentNode()).add(newEdge);
        }
        newNode.getCuis().forEach(cui -> {
            cuiToNodeMap.put(cui, newNode);
            return true;
        });
        allEdges.addAll(newOutgoingEdges);
        allEdges.addAll(newIncomingEdges);

        return true;
    }

    private void removeAllTrace(Node parent, Node child) {
        removeAllTrace(parent);
        removeAllTrace(child);
    }

    private void removeAllTrace(final Node node) {
        TIntSet associatedCuis = node.getCuis();
        associatedCuis.forEach(cui -> {
            cuiToNodeMap.remove(cui);
            return true;
        });

        allEdges.removeAll(outgoingEdges.get(node));
        allEdges.removeAll(incomingEdges.get(node));
        outgoingEdges.remove(node);
        incomingEdges.remove(node);

        for (Node otherNode : outgoingEdges.keySet()) {
            Set<Edge> toRemove = new HashSet<>();
            for (Edge outgoingEdge : outgoingEdges.get(otherNode)) {
                if (outgoingEdge.getChildNode().equals(node)) {
                    toRemove.add(outgoingEdge);
                }
            }
            outgoingEdges.get(otherNode).removeAll(toRemove);
        }

        for (Node otherNode : incomingEdges.keySet()) {
            Set<Edge> toRemove = new HashSet<>();
            for (Edge incomingEdge : incomingEdges.get(otherNode)) {
                if (incomingEdge.getParentNode().equals(node)) {
                    toRemove.add(incomingEdge);
                }
            }
            incomingEdges.get(otherNode).removeAll(toRemove);
        }
    }

    public void removeCycles() {
        /* get list of edges in cycles, sorted so process is deterministic */
        List<Edge> sortedCycleEdges = new ArrayList<>();
        for (Edge edge : getAllEdges()) {
            if (isInCycle(edge)) {
                sortedCycleEdges.add(edge);
            }
        }
        Collections.sort(sortedCycleEdges);
        System.out.println("Total cycle edges to remove this round: " + sortedCycleEdges.size());

        int numberRemoved = 0;
        for (Edge edge : sortedCycleEdges) {
            boolean collapsed = collapseEdge(edge);
            if (collapsed) {
                System.err.println("Cycle edge collapse : " + edge);
                numberRemoved++;
            }
        }

        if (numberRemoved > 0) {
            System.out.println("Cycle edges collapsed this round: " + numberRemoved);
            removeCycles();
        }
    }

    Set<Node> getAllNodes() {
        Set<Node> allNodes = new HashSet<>();
        allNodes.addAll(getAllParentNodes());
        allNodes.addAll(getAllChildNodes());
        return allNodes;
    }

    private Set<Node> getAllParentNodes() {
        return outgoingEdges.keySet();
    }

    private Set<Node> getAllChildNodes() {
        return incomingEdges.keySet();
    }

    public TIntSet getAllCuis() {
        return cuiToNodeMap.keySet();
    }

    @Override
    public String toString() {
        OutputStream stream = new ByteArrayOutputStream();
        final PrintWriter printWriter = new PrintWriter(stream);
        getAllNodes().forEach(printWriter::println);
        getAllEdges().forEach(printWriter::println);
        cuiToNodeMap.forEachEntry((cui, node) -> {
            printWriter.println(cui + "\t" + node);
            return true;
        });
        printWriter.flush();
        printWriter.close();
        return stream.toString();
    }

    private void readResourceFiles(InputStream inputStreamStructure, InputStream inputStreamDecorations)
            throws IOException {
        readResourceFiles(inputStreamStructure, inputStreamDecorations, null, null, null);
    }

    private void readResourceFiles(InputStream inputStreamStructure, InputStream inputStreamDecorations,
                                   NodeFilter nodeFilter, EdgeFilter edgeFilter, NodeModifier nodeModifier) throws IOException {
        readDecorationsFile(inputStreamDecorations, nodeFilter, nodeModifier);
        readStructureFile(inputStreamStructure, edgeFilter);
    }

    private void readDecorationsFile(InputStream inputStreamDecorations, NodeFilter nodeFilter,
                                     NodeModifier nodeModifier) throws IOException {
        Pattern recordSplitPattern = Pattern.compile("\\t");
        Pattern cuiSplitPattern = Pattern.compile(",");
        Pattern infoSplitPattern = Pattern.compile("\\|");

        BufferedReader decorationsReader = new BufferedReader(new InputStreamReader(inputStreamDecorations));
        decorationsReader.readLine(); // throw away version number
        String line;
        while ((line = decorationsReader.readLine()) != null) {
            String[] record = recordSplitPattern.split(line);
            if (record.length != 7) {
                throw new IllegalArgumentException("Decorations record is wrong length: " + line);
            }
            processDecorationsFileLine(record, cuiSplitPattern, infoSplitPattern, nodeFilter, nodeModifier);
        }
        decorationsReader.close();
    }

    private void readStructureFile(InputStream inputStreamStructure, EdgeFilter edgeFilter) {
        Pattern recordSplitPattern = Pattern.compile("\\t");
        BufferedReader structureReader = new BufferedReader(new InputStreamReader(inputStreamStructure));
        try {
            structureReader.readLine(); // throw away version number
            String line;
            while ((line = structureReader.readLine()) != null) {
                String[] record = recordSplitPattern.split(line);

                int parentCui = Integer.parseInt(record[0]);
                int childCui = Integer.parseInt(record[1]);

                if (!graphContainsCui(parentCui)) {
                    System.out.println("Rejected edge due to nonexistent node for parent: " + parentCui);
                    continue;
                }
                if (!graphContainsCui(childCui)) {
                    System.out.println("Rejected edge due to nonexistent node for child: " + childCui);
                    continue;
                }

                Edge edge = new Edge(getNodeForCui(parentCui), getNodeForCui(childCui));

                if (!(edgeFilter == null) && edgeFilter.reject(edge)) { // possibly prune edge
                    System.out.println("Rejected edge: " + edge);
                    continue;
                }

                addEdge(edge);
            }
            structureReader.close();
        } catch (IOException e) {
            System.err.println("Can't read graph structure resource file.");
        }
    }

    private void processDecorationsFileLine(String[] record, Pattern cuiSplitPattern, Pattern infoSplitPattern,
                                            NodeFilter nodeFilter, NodeModifier nodeModifier) {
        /* get all node info from record */
        TIntSet cuisThisNode = new TIntHashSet();
        int cui = Integer.parseInt(record[0]);
        cuisThisNode.add(cui);

        if (cuiToNodeMap.containsKey(cui)) {
            return; // this cui was already added via its sibling
        }

        TIntSet siblingCuis = new TIntHashSet();
        siblingCuis.add(cui); /* to handle cases where there are no siblings */
        if (record[1].length() > 0) {
            for (String siblingCuiString : cuiSplitPattern.split(record[1])) {
                siblingCuis.add(Integer.parseInt(siblingCuiString));
            }
        }

        /* node not present: make new node */
        final Node node = new Node();
        node.addCui(cui);
        node.addCuis(siblingCuis);
        if (record[2].length() > 0) { // descriptions
            node.addStrings(new HashSet<>(Arrays.asList(infoSplitPattern.split(record[2]))));
        }
        if (record[3].length() > 0) { // codes
            String[] codePairStrings = infoSplitPattern.split(record[3]);
            for (String codePairString : codePairStrings) {
                node.addCode(pairFromString(codePairString));
            }
        }
        if (record[4].length() > 0) { // semantic types
            String[] semanticTypeStrings = infoSplitPattern.split(record[4]);
            for (String semanticTypeString : semanticTypeStrings) {
                node.addSemanticType(SemanticType.valueOf(semanticTypeString));
            }
        }
        if (record[5].length() > 0) { // semantic groups
            String[] semanticGroupStrings = infoSplitPattern.split(record[5]);
            for (String semanticGroupString : semanticGroupStrings) {
                node.addSemanticGroup(SemanticGroup.valueOf(semanticGroupString));
            }
        }
        for (String ontologyTypeString : infoSplitPattern.split(record[6])) {
            OntologyType ontologyType = OntologyType.valueOf(ontologyTypeString);
            node.addOntology(ontologyType);
        }

        if (!(nodeFilter == null) && nodeFilter.reject(node)) { // possibly prune node
            System.out.println("Rejected node: " + node);
            return;
        }

        if (!(nodeModifier == null)) {
            nodeModifier.modify(node); // make any necessary modifications
        }

        addNode(node);
    }

    public void writeResourceFiles(OutputStream outputStreamStructure, OutputStream outputStreamDecorations) {
        writeDecorationsFile(outputStreamDecorations);
        writeStructureFile(outputStreamStructure);
    }

    private void writeDecorationsFile(OutputStream outputStreamDecorations) {
        final PrintWriter printWriterDecorations = new PrintWriter(outputStreamDecorations);

        TIntList allCuis = new TIntArrayList(cuiToNodeMap.keySet());
        allCuis.sort();

        allCuis.forEach(cui -> {
            Node node = getNodeForCui(cui); /* there is only a single parent node for each cui */
            if (node == null) {
                System.err.println("No node for CUI: " + cui);
                return true;
            }
            Set<String> descriptions = node.getStrings();
            Set<Pair<OntologyType, String>> codePairs = node.getCodes();
            List<String> codesAsStrings = new ArrayList<>();
            for (Pair<OntologyType, String> codePair : codePairs) {
                codesAsStrings.add(pairToString(codePair));
            }
            Collections.sort(codesAsStrings);
            TIntList siblingCuis = new TIntArrayList(node.getCuis());
            siblingCuis.remove(cui);
            siblingCuis.sort();
            printWriterDecorations.println(cui + "\t" + StringUtils.join(TDecorators.wrap(siblingCuis), ",") +
                    "\t" + StringUtils.join(descriptions, "|") + "\t" + StringUtils.join(codesAsStrings, "|") +
                    "\t" + StringUtils.join(node.getSemanticTypeList(),
                    "|") + "\t" + StringUtils.join(node.getSemanticGroupList(), "|") +
                    "\t" + StringUtils.join(node.getOntologyList(), "|"));
            return true;
        });

        printWriterDecorations.flush();
        printWriterDecorations.close();
    }

    private void writeStructureFile(OutputStream outputStreamStructure) {
        final PrintWriter printWriterStructure = new PrintWriter(outputStreamStructure);

        List<Node> parentNodes = new ArrayList<>(outgoingEdges.keySet());
        Collections.sort(parentNodes);
        for (final Node node : parentNodes) {
            TIntList cuis = new TIntArrayList(node.getCuis());
            cuis.sort();
            cuis.forEach(cui -> {
                Set<Edge> outgoingEdgeSet = getOutgoingEdges(node);
                if (outgoingEdgeSet.isEmpty()) { // it's possible for a node to have only incoming edges
                    return true;
                }
                List<Edge> outgoingEdgeList = new ArrayList<>(outgoingEdgeSet);
                Collections.sort(outgoingEdgeList);
                for (Edge edge : outgoingEdgeList) {
                    Node childNode = edge.getChildNode();
                    TIntList childCuis = new TIntArrayList(childNode.getCuis());
                    childCuis.sort();
                    childCuis.forEach(childCui -> {
                        printWriterStructure.println(cui + "\t" + childCui);
                        return true;
                    });
                }
                return true;
            });
        }
        printWriterStructure.flush();
        printWriterStructure.close();
    }

    private String pairToString(Pair<OntologyType, String> codePair) {
        return codePair.getLeft() + "," + codePair.getRight();
    }

    private Pair<OntologyType, String> pairFromString(String codePairString) {
        String[] splitString = codePairString.split(",");
        if (splitString.length != 2) {
            throw new IllegalArgumentException("Illegal ontology pair string: " + codePairString);
        }
        return Pair.of(OntologyType.valueOf(splitString[0]), splitString[1]);
    }
}
