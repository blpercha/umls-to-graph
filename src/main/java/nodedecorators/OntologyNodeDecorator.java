package nodedecorators;

import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import graph.Node;
import org.apache.commons.lang3.StringUtils;
import utils.OntologyType;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

public class OntologyNodeDecorator implements NodeDecorator {
    private final TIntObjectMap<Set<OntologyType>> ontologies = new TIntObjectHashMap<>();

    public OntologyNodeDecorator(InputStream inputStream, boolean resourceFile) throws IOException {
        if (resourceFile) {
            readResourceFile(inputStream);
        } else {
            readMrConso(inputStream);
        }
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
            final int cui = Integer.parseInt(record[0].substring(1));
            ontologies.putIfAbsent(cui, new HashSet<>());
            ontologies.get(cui).add(ontologyType);
        }
        reader.close();
    }

    private void readResourceFile(InputStream inputStream) throws IOException {
        Pattern recordSplitPattern = Pattern.compile("\t");
        Pattern commaSplitPattern = Pattern.compile(",");
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        while ((line = reader.readLine()) != null) {
            String[] record = recordSplitPattern.split(line);
            int cui = Integer.parseInt(record[0]);
            String[] ontologyStrings = commaSplitPattern.split(record[1]);
            for (String ontologyString : ontologyStrings) {
                OntologyType ontologyType = OntologyType.valueOf(ontologyString);
                ontologies.putIfAbsent(cui, new HashSet<>());
                ontologies.get(cui).add(ontologyType);
            }
        }
        reader.close();
    }

    private TIntList allCuis() {
        TIntList cuiList = new TIntArrayList(ontologies.keySet());
        cuiList.sort();
        return cuiList;
    }

    private void writeResourceFile(OutputStream outputStream) {
        final PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(outputStream));
        allCuis().forEach(cui -> {
            List<OntologyType> ontologyList = new ArrayList<>(ontologies.get(cui));
            Collections.sort(ontologyList);
            printWriter.println(cui + "\t" + StringUtils.join(ontologyList, ","));
            return true;
        });
        printWriter.flush();
        printWriter.close();
    }


    @Override
    public void decorateNode(final Node node) {
        node.getCuis().forEach(cui -> {
            if (!ontologies.containsKey(cui)) {
                return true;
            }
            ontologies.get(cui).forEach(node::addOntology);
            return true;
        });
    }

    public static void main(String[] args) throws IOException {
        String mrConsoFile = args[0];
        String outputResourceFile = args[1];

        OntologyNodeDecorator ontologyNodeDecorator = new OntologyNodeDecorator(
                new FileInputStream(mrConsoFile), false);
        ontologyNodeDecorator.writeResourceFile(new FileOutputStream(outputResourceFile));
    }
}
