package graph;

import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import org.apache.commons.lang3.tuple.Pair;
import utils.OntologyType;
import utils.SemanticGroup;
import utils.SemanticType;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Node implements Comparable<Node>, Serializable {
    private TIntSet cuis = new TIntHashSet();
    private Set<Pair<OntologyType, String>> codes = new HashSet<>();
    private Set<String> strings = new HashSet<>();
    private Set<SemanticType> semanticTypes = new HashSet<>();
    private Set<SemanticGroup> semanticGroups = new HashSet<>();
    private Set<OntologyType> ontologies = new HashSet<>();

    public Node() {
    }

    public Node(TIntSet cuis, Set<Pair<OntologyType, String>> codes, Set<String> strings,
                Set<SemanticType> semanticTypes, Set<SemanticGroup> semanticGroups,
                Set<OntologyType> ontologies) {
        this.cuis = cuis;
        this.codes = codes;
        this.strings = strings;
        this.semanticTypes = semanticTypes;
        this.semanticGroups = semanticGroups;
        this.ontologies = ontologies;
    }

    void addNodeInfo(Node otherNode) {
        cuis.addAll(otherNode.getCuis());
        codes.addAll(otherNode.getCodes());
        strings.addAll(otherNode.getStrings());
        semanticTypes.addAll(otherNode.getSemanticTypes());
        semanticGroups.addAll(otherNode.getSemanticGroups());
        ontologies.addAll(otherNode.getOntologies());
    }

    void addSemanticType(SemanticType semanticType) {
        this.semanticTypes.add(semanticType);
    }

    void addSemanticGroup(SemanticGroup semanticGroup) {
        this.semanticGroups.add(semanticGroup);
    }

    public void addSemanticTypes(Set<SemanticType> semanticTypes) {
        this.semanticTypes.addAll(semanticTypes);
    }

    public void addSemanticGroups(Set<SemanticGroup> semanticGroups) {
        this.semanticGroups.addAll(semanticGroups);
    }

    public void retainSemanticGroups(Set<SemanticGroup> allowedGroups) {
        this.semanticGroups.retainAll(allowedGroups);
    }

    void addCui(int cui) {
        cuis.add(cui);
    }

    void addCuis(TIntSet newCuis) {
        cuis.addAll(newCuis);
    }

    void addCode(Pair<OntologyType, String> code) {
        codes.add(code);
    }

    public void addCodes(Set<Pair<OntologyType, String>> codes) {
        for (Pair<OntologyType, String> code : codes) {
            addCode(code);
        }
    }

    public void addString(String newString) {
        strings.add(newString);
    }

    public void addStrings(Set<String> newStrings) {
        strings.addAll(newStrings);
    }

    public TIntSet getCuis() {
        return cuis;
    }

    public Set<Pair<OntologyType, String>> getCodes() {
        return codes;
    }

    public Set<String> getCodes(OntologyType ontologyType) {
        Set<String> codesThisOntology = new HashSet<>();
        for (Pair<OntologyType, String> code : codes) {
            if (code.getLeft().equals(ontologyType)) {
                codesThisOntology.add(code.getRight());
            }
        }
        return codesThisOntology;
    }

    public Set<String> getStrings() {
        return strings;
    }

    boolean containsCui(int cui) {
        return cuis.contains(cui);
    }

    List<SemanticType> getSemanticTypeList() {
        List<SemanticType> asList = new ArrayList<>(semanticTypes);
        Collections.sort(asList);
        return asList;
    }

    List<SemanticGroup> getSemanticGroupList() {
        List<SemanticGroup> asList = new ArrayList<>(semanticGroups);
        Collections.sort(asList);
        return asList;
    }

    public Set<SemanticGroup> getSemanticGroups() {
        return semanticGroups;
    }

    public Set<SemanticType> getSemanticTypes() {
        return semanticTypes;
    }

    public void removeSemanticTypes(Set<SemanticType> nonAllowedTypes) {
        semanticTypes.removeAll(nonAllowedTypes);
    }

    private Set<OntologyType> getOntologies() {
        return ontologies;
    }

    public void addOntology(OntologyType ontologyType) {
        ontologies.add(ontologyType);
    }

    List<OntologyType> getOntologyList() {
        List<OntologyType> ontologyTypeList = new ArrayList<>(getOntologies());
        Collections.sort(ontologyTypeList);
        return ontologyTypeList;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Node node = (Node) o;

        return cuis.equals(node.cuis);

    }

    @Override
    public int hashCode() {
        return cuis.hashCode();
    }

    @Override
    public String toString() {
        return "Node{" +
                "cuis=" + cuis +
                ", codes=" + codes +
                ", strings=" + strings +
                ", semanticTypes=" + semanticTypes +
                ", semanticGroups=" + semanticGroups +
                ", ontologies=" + ontologies +
                '}';
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public int compareTo(Node other) {
        TIntList thisCuis = new TIntArrayList(cuis);
        TIntList otherCuis = new TIntArrayList(other.getCuis());
        return thisCuis.min() > otherCuis.min() ? 1 : -1;
    }

}
