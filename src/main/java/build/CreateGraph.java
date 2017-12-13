package build;

import edgefilters.EdgeFilter;
import edgefilters.ListEdgeFilter;
import edgefilters.SemanticGroupMismatchEdgeFilter;
import graph.UMLSGraph;
import nodedecorators.GenericCodeNodeDecorator;
import nodedecorators.Icd9Icd10CodeTranslationNodeDecorator;
import nodedecorators.Icd9SnomedCodeTranslationNodeDecorator;
import nodedecorators.NdcCodeNodeDecorator;
import nodedecorators.NodeDecorator;
import nodedecorators.OntologyNodeDecorator;
import nodedecorators.SemanticGroupNodeDecorator;
import nodedecorators.SemanticTypeNodeDecorator;
import nodedecorators.StringsNodeDecorator;
import nodefilters.ListNodeFilter;
import nodefilters.NodeFilter;
import nodefilters.SemanticGroupNodeFilter;
import nodefilters.SemanticTypeNodeFilter;
import nodemodifiers.ListNodeModifier;
import nodemodifiers.ModifiedStringsNodeModifier;
import nodemodifiers.NodeModifier;
import nodemodifiers.SemanticGroupRemovalNodeModifier;
import nodemodifiers.SemanticTypeRemovalNodeModifier;
import subgraphs.DrugIngredientSubgraph;
import subgraphs.MrHierSubgraph;
import subgraphs.Subgraph;
import utils.OntologyType;
import utils.SemanticGroup;
import utils.SemanticType;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.GZIPInputStream;

class CreateGraph {
    public static void main(String[] args) throws IOException {
        String graphConfigFile = args[0];
        String outputFileStructure = args[1];
        String outputFileDecorations = args[2];

        UMLSGraph umlsGraph = new UMLSGraph();

        /* lists of graph components */
        List<Subgraph> subgraphs = new ArrayList<>();
        List<NodeDecorator> nodeDecorators = new ArrayList<>();
        List<NodeDecorator> translatorDecorators = new ArrayList<>();
        List<NodeFilter> nodeFilters = new ArrayList<>();
        List<NodeModifier> nodeModifiers = new ArrayList<>();
        List<EdgeFilter> edgeFilters = new ArrayList<>();

        /* create graph components */
        createElements(graphConfigFile, subgraphs, nodeDecorators, translatorDecorators,
                nodeFilters, nodeModifiers, edgeFilters);

        /* add components to graph */
        System.out.println("Creating graph...");
        while (!subgraphs.isEmpty()) {
            umlsGraph.addSubgraph(subgraphs.remove(0));
            System.out.println("Subgraph countdown: " + subgraphs.size());
        }
        while (!nodeDecorators.isEmpty()) {
            umlsGraph.decorateNodes(nodeDecorators.remove(0));
            System.out.println("Node decorator countdown: " + nodeDecorators.size());
        }
        while (!translatorDecorators.isEmpty()) {
            umlsGraph.decorateNodes(translatorDecorators.remove(0));
            System.out.println("Translator decorators countdown: " + translatorDecorators.size());
        }

        /* write intermediate graph output */
        OutputStream intermediateStructure = new ByteArrayOutputStream();
        OutputStream intermediateDecorations = new ByteArrayOutputStream();
        umlsGraph.writeResourceFiles(intermediateStructure, intermediateDecorations);

        /* do filtering and modifying on re-read */
        umlsGraph = new UMLSGraph(new ByteArrayInputStream(intermediateStructure.toString().getBytes()),
                new ByteArrayInputStream(intermediateDecorations.toString().getBytes()),
                new ListNodeFilter(nodeFilters), new ListEdgeFilter(edgeFilters),
                new ListNodeModifier(nodeModifiers));

        /* remove cycles */
        umlsGraph.removeCycles();
        System.out.println("Done removing cycles...");

        /* write final graph output */
        umlsGraph.writeResourceFiles(new FileOutputStream(outputFileStructure),
                new FileOutputStream(outputFileDecorations));
    }

    private static void createElements(String graphConfigFile,
                                       List<Subgraph> subgraphs,
                                       List<NodeDecorator> nodeDecorators,
                                       List<NodeDecorator> translatorDecorators,
                                       List<NodeFilter> nodeFilters,
                                       List<NodeModifier> nodeModifiers,
                                       List<EdgeFilter> edgeFilters) throws IOException {
        BufferedReader configReader = new BufferedReader(new FileReader(graphConfigFile));
        String configLine;
        while ((configLine = configReader.readLine()) != null) {
            if (configLine.startsWith("#")) {
                continue;
            }
            if (configLine.isEmpty()) {
                continue;
            }
            String[] configInfo = configLine.trim().split("\t");
            String componentType = configInfo[0];

            /* subgraphs */
            if (componentType.equals("subgraph")) {
                OntologyType ontologyType;
                try {
                    ontologyType = OntologyType.valueOf(configInfo[1]);
                } catch (IllegalArgumentException e) {
                    System.err.println("Unrecognized subgraph type: " + configInfo[1]);
                    continue;
                }
                String subgraphFilePath = configInfo[2];
                Subgraph subgraph;
                if (ontologyType.equals(OntologyType.RXNORM)) {
                    subgraph = new DrugIngredientSubgraph(new FileInputStream(subgraphFilePath));
                } else {
                    subgraph = new MrHierSubgraph(new FileInputStream(subgraphFilePath));
                }
                subgraphs.add(subgraph);
                System.out.println("Added subgraph " + ontologyType + " from " + subgraphFilePath + ".");
                continue;
            }

            /* code decorators */
            if (componentType.equals("codedecorator")) {
                OntologyType ontologyType = OntologyType.valueOf(configInfo[1]);
                String decoratorResourceFile = configInfo[2];
                NodeDecorator decorator;
                if (ontologyType.equals(OntologyType.NDC)) {
                    decorator = new NdcCodeNodeDecorator(new FileInputStream(decoratorResourceFile), true);
                } else {
                    decorator = new GenericCodeNodeDecorator(new FileInputStream(decoratorResourceFile));
                }
                nodeDecorators.add(decorator);
                System.out.println("Added decorator " + ontologyType + " from " + decoratorResourceFile + ".");
                continue;
            }

            /* code translators */
            if (componentType.equals("translation")) {
                OntologyType ontologyType1 = OntologyType.valueOf(configInfo[1]);
                OntologyType ontologyType2 = OntologyType.valueOf(configInfo[2]);
                String resourceFile = configInfo[3];
                NodeDecorator decorator;
                if ((ontologyType1.equals(OntologyType.ICD9CM) && ontologyType2.equals(OntologyType.ICD10PCS)) ||
                        (ontologyType1.equals(OntologyType.ICD10PCS) && ontologyType2.equals(OntologyType.ICD9CM))) {
                    decorator = new Icd9Icd10CodeTranslationNodeDecorator(new FileInputStream(resourceFile));
                    translatorDecorators.add(decorator);
                    System.out.println("Added ICD9-ICD10 translator from " + resourceFile);
                } else if ((ontologyType1.equals(OntologyType.ICD9CM) && ontologyType2.equals(OntologyType.SNOMEDCT_US)) ||
                        (ontologyType1.equals(OntologyType.SNOMEDCT_US) && ontologyType2.equals(OntologyType.ICD9CM))) {
                    decorator = new Icd9SnomedCodeTranslationNodeDecorator(new FileInputStream(resourceFile));
                    translatorDecorators.add(decorator);
                    System.out.println("Added ICD9-SNOMED translator from " + resourceFile);
                } else {
                    System.err.println("Unrecognized translator type: " + ontologyType1 + " " + ontologyType2);
                }
                continue;
            }

            /* other decorators */
            if (componentType.equals("decorator")) {
                String decoratorType = configInfo[1];
                String resourceFile = configInfo[2];
                switch (decoratorType) {
                    case "ONTOLOGY":
                        nodeDecorators.add(new OntologyNodeDecorator(new FileInputStream(resourceFile), true));
                        System.out.println("Added ONTOLOGY node decorations from " + resourceFile);
                        break;
                    case "SEMGROUP":
                        nodeDecorators.add(new SemanticGroupNodeDecorator(new FileInputStream(resourceFile), true));
                        System.out.println("Added SEMANTIC GROUP node decorations from " + resourceFile);
                        break;
                    case "SEMTYPE":
                        nodeDecorators.add(new SemanticTypeNodeDecorator(new FileInputStream(resourceFile), true));
                        System.out.println("Added SEMANTIC TYPE node decorations from " + resourceFile);
                        break;
                    case "STRINGS":
                        nodeDecorators.add(new StringsNodeDecorator(new GZIPInputStream(new FileInputStream(resourceFile))));
                        System.out.println("Added STRING node decorations from " + resourceFile);
                        break;
                    default:
                        System.err.println("Unrecognized decorator type: " + decoratorType);
                }
                continue;
            }

            /* node filters and modifiers */
            if (componentType.equals("nodefilter")) {
                String filterType = configInfo[1];
                switch (filterType) {
                    case "SEMGROUP":
                        Set<SemanticGroup> allowedGroups = new HashSet<>();
                        Arrays.asList(configInfo[2].split("\\|")).forEach(groupName ->
                                allowedGroups.add(SemanticGroup.valueOf(groupName)));
                        nodeFilters.add(new SemanticGroupNodeFilter(allowedGroups));
                        nodeModifiers.add(new SemanticGroupRemovalNodeModifier(allowedGroups));
                        System.out.println("Added SEMANTIC GROUP node filter with allowed groups: " + configInfo[2]);
                        break;
                    case "SEMTYPE":
                        Set<SemanticType> disallowedTypes = new HashSet<>();
                        Arrays.asList(configInfo[2].split("\\|")).forEach(typeName ->
                                disallowedTypes.add(SemanticType.valueOf(typeName)));
                        nodeFilters.add(new SemanticTypeNodeFilter(disallowedTypes));
                        nodeModifiers.add(new SemanticTypeRemovalNodeModifier(disallowedTypes));
                        System.out.println("Added SEMANTIC TYPE node filter with disallowed types: " + configInfo[2]);
                        break;
                    default:
                        System.err.println("Unrecognized node filter type: " + filterType);
                        break;
                }
                continue;
            }

            /* edge filters */
            if (componentType.equals("edgefilter")) {
                String filterType = configInfo[1];
                if (filterType.equals("SEMGROUPMISMATCH")) {
                    edgeFilters.add(new SemanticGroupMismatchEdgeFilter());
                    System.out.println("Added SEMANTIC GROUP MISMATCH edge filter. No mismatched edges will be allowed.");
                } else {
                    System.err.println("Unrecognized edge filter type: " + filterType);
                }
                continue;
            }

            /* node modifiers (alone) */
            if (componentType.equals("nodemodifier")) {
                String modifierType = configInfo[1];
                if (modifierType.equals("MODIFIEDSTRINGS")) {
                    nodeModifiers.add(new ModifiedStringsNodeModifier());
                    System.out.println("Added MODIFIED STRINGS node modifier. String rearrangements and capitalization variants will be added.");
                } else {
                    System.err.println("Unrecognized node modifier type: " + modifierType);
                }
            }
        }
        configReader.close();
    }
}
