package nodedecorators;

import gnu.trove.set.hash.TIntHashSet;
import org.apache.commons.lang3.tuple.Pair;
import utils.OntologyType;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.regex.Pattern;

public class GenericCodeNodeDecorator extends CodeNodeDecorator {

    public GenericCodeNodeDecorator(InputStream resourceFileInputStream) throws IOException {
        readResourceFile(resourceFileInputStream);
    }

    private GenericCodeNodeDecorator(InputStream mrConsoFileInputStream, OntologyType ontologyType) throws IOException {
        readMrConso(mrConsoFileInputStream, ontologyType);
    }

    private void readMrConso(InputStream inputStream, OntologyType ontologyType) throws IOException {
        Pattern recordSplitPattern = Pattern.compile("\\|");
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        while ((line = reader.readLine()) != null) {
            String[] splitLine = recordSplitPattern.split(line, -1);
            processMrConsoRecord(splitLine, ontologyType);
        }
        reader.close();
    }

    private void processMrConsoRecord(String[] data, OntologyType desiredOntology) {
        OntologyType ontologyType;
        try {
            ontologyType = OntologyType.valueOf(data[11]);
        } catch (IllegalArgumentException e) { // unrecognized ontology
            return;
        }
        if (!(ontologyType.equals(desiredOntology))) { // only take codes from desired ontology
            return;
        }

        int cui = Integer.parseInt(data[0].substring(1));
        String codeString = data[13];
        Pair<OntologyType, String> code = Pair.of(ontologyType, codeString);

        if (!codeToCuis.containsKey(code)) {
            codeToCuis.put(code, new TIntHashSet());
        }
        codeToCuis.get(code).add(cui);
        cuiToCodes.putIfAbsent(cui, new HashSet<>());
        cuiToCodes.get(cui).add(code);
    }

    public static void main(String[] args) throws IOException {
        String mrConsoFile = args[0];
        String ontologyTypeString = args[1];
        String outputResourceFile = args[2];

        OntologyType ontologyType = OntologyType.valueOf(ontologyTypeString);
        GenericCodeNodeDecorator map = new GenericCodeNodeDecorator(new FileInputStream(mrConsoFile), ontologyType);
        map.writeResourceFile(new FileOutputStream(outputResourceFile));
    }
}
