package subgraphs;

import gnu.trove.TDecorators;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import org.apache.commons.lang3.StringUtils;
import utils.OntologyType;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.regex.Pattern;

public class MrHierSubgraph implements Subgraph {
    private final TIntObjectMap<TIntSet> cuiToChildren = new TIntObjectHashMap<>();
    private final TIntObjectMap<TIntSet> cuiToParents = new TIntObjectHashMap<>();
    private final TIntObjectMap<TIntSet> auiToCuis = new TIntObjectHashMap<>();

    public MrHierSubgraph(InputStream resourceFileInputStream) throws IOException {
        readResourceFile(resourceFileInputStream);
    }

    private MrHierSubgraph(InputStream mrHierInputStream, InputStream mrConsoInputStream,
                           OntologyType acceptableOntology) throws IOException {
        readMrConso(mrConsoInputStream);
        readMrHier(mrHierInputStream, acceptableOntology);
        System.out.println("Total number of CUIs in hierarchies: " + allCuis().size());
    }

    private void readMrConso(final InputStream inputStream) throws IOException {
        Pattern recordSplitPattern = Pattern.compile("\\|");
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        while ((line = reader.readLine()) != null) {
            String[] record = recordSplitPattern.split(line, -1);
            final OntologyType ontologyType = OntologyType.fromString(record[11]);
            if (ontologyType.equals(OntologyType.OTHER)) { // unrecognized ontology
                continue;
            }
            final char suppressFlag = record[16].charAt(0);
            if (suppressFlag == 'E' || suppressFlag == 'O' || suppressFlag == 'Y') { // suppress this AUI
                continue;
            }
            int aui = Integer.parseInt(record[7].substring(1));
            int cui = Integer.parseInt(record[0].substring(1));
            auiToCuis.putIfAbsent(aui, new TIntHashSet());
            auiToCuis.get(aui).add(cui);
        }
        reader.close();
    }

    private void readMrHier(InputStream inputStream, OntologyType acceptableOntology) {
        Pattern recordSplitPattern = Pattern.compile("\\|");
        BufferedReader mrHierReader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        try {
            while ((line = mrHierReader.readLine()) != null) {
                processMrHierRecord(recordSplitPattern.split(line, -1), acceptableOntology);
            }
            mrHierReader.close();
        } catch (IOException e) {
            System.err.println("CuiHierarchiesSubgraph: Cannot read MRHIER.");
            e.printStackTrace();
        }
    }

    private void processMrHierRecord(String[] record, OntologyType acceptableOntology) {
        final OntologyType ontologyType = OntologyType.fromString(record[4]);
        if (acceptableOntology != null && !acceptableOntology.equals(ontologyType)) {
            return; /* relationship must be asserted by our specific ontology */
        }

        final int cui = Integer.parseInt(record[0].substring(1));
        final int childAui = Integer.parseInt(record[1].substring(1));
        if (!auiToCuis.containsKey(childAui)) {
            return; /* child aui must be in allowed set from MRCONSO */
        }

        int parentAui;
        try {
            parentAui = Integer.parseInt(record[3].substring(1));
        } catch (StringIndexOutOfBoundsException e) {
            System.err.println("MrHierSubgraph: No parent aui listed for child " + childAui);
            return;
        }

        if (!auiToCuis.containsKey(parentAui)) { /* parent must also be in allowed set from MRCONSO */
            return;
        }

        auiToCuis.get(parentAui).forEach(parentCui -> {
            cuiToChildren.putIfAbsent(parentCui, new TIntHashSet());
            cuiToChildren.get(parentCui).add(cui);
            cuiToParents.putIfAbsent(cui, new TIntHashSet());
            cuiToParents.get(cui).add(parentCui);
            return true;
        });
    }

    private void readResourceFile(InputStream resourceFileInputStream) throws IOException {
        Pattern recordSplitPattern = Pattern.compile("\t");
        Pattern cuiSplitPattern = Pattern.compile(",");
        BufferedReader resourceReader = new BufferedReader(new InputStreamReader(resourceFileInputStream));
        String line;
        while ((line = resourceReader.readLine()) != null) {
            String[] record = recordSplitPattern.split(line);
            int cui = Integer.parseInt(record[0]);
            for (String child : cuiSplitPattern.split(record[1])) {
                int childInt = Integer.parseInt(child);
                cuiToChildren.putIfAbsent(cui, new TIntHashSet());
                cuiToChildren.get(cui).add(childInt);
                cuiToParents.putIfAbsent(childInt, new TIntHashSet());
                cuiToParents.get(childInt).add(cui);
            }
        }
        resourceReader.close();
    }

    @Override
    public TIntSet getChildren(int cui) {
        if (!cuiToChildren.containsKey(cui)) {
            return new TIntHashSet();
        }
        return cuiToChildren.get(cui);
    }

    @Override
    public TIntSet getParents(int cui) {
        if (!cuiToParents.containsKey(cui)) {
            return new TIntHashSet();
        }
        return cuiToParents.get(cui);
    }

    @Override
    public TIntList allCuis() {
        TIntSet cuiSet = new TIntHashSet(cuiToChildren.keySet());
        cuiSet.addAll(cuiToParents.keySet());
        TIntList cuiList = new TIntArrayList(cuiSet);
        cuiList.sort();
        return cuiList;
    }

    private void writeResourceFile(OutputStream outputStreamMap) {
        final PrintWriter printWriter = new PrintWriter(outputStreamMap);
        final TIntList cuiList = new TIntArrayList(cuiToChildren.keySet());
        cuiList.sort();

        cuiList.forEach(i -> {
            TIntSet children = getChildren(i);
            if (children.isEmpty()) {
                return true;
            }
            printWriter.println(i + "\t" + StringUtils.join(TDecorators.wrap(children), ","));
            return true;
        });

        printWriter.flush();
        printWriter.close();
    }

    public static void main(String args[]) throws IOException {
        String mrHierFile = args[0];            // MRHIER.RRF
        String mrConsoFile = args[1];           // MRCONSO.RRF
        String outputFile = args[2];            // umls-hier-<ontologyname>.txt
        String ontologyType = args[3];          // we create one ontology subgraph file at a time

        MrHierSubgraph subgraph = new MrHierSubgraph(new FileInputStream(mrHierFile),
                new FileInputStream(mrConsoFile), OntologyType.fromString(ontologyType));

        subgraph.writeResourceFile(new FileOutputStream(outputFile));
    }
}
