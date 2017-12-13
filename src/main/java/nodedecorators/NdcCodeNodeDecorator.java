package nodedecorators;

import gnu.trove.set.hash.TIntHashSet;
import org.apache.commons.lang3.StringUtils;
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

public class NdcCodeNodeDecorator extends CodeNodeDecorator {

    public NdcCodeNodeDecorator(InputStream inputStream, boolean resourceFile) throws IOException {
        if (resourceFile) {
            readResourceFile(inputStream);
        } else {
            readMrSat(inputStream);
        }
    }

    private void readMrSat(InputStream inputStream) throws IOException {
        Pattern recordSplitPattern = Pattern.compile("\\|");
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        while ((line = reader.readLine()) != null) {
            processMrSatRecord(recordSplitPattern.split(line));
        }
        reader.close();
    }

    private void processMrSatRecord(String[] data) {
        OntologyType firstOntology;

        try {
            firstOntology = OntologyType.valueOf(data[8]);
        } catch (IllegalArgumentException e) {
            return;
        }

        if (!(firstOntology.equals(OntologyType.NDC))) {
            /* note that this assumes ndc will be first and not second... this appears to be consistent in file */
            return;
        }

        int cui = Integer.parseInt(data[0].substring(1));
        String ndc = StringUtils.strip(data[10].replace("*", "0").replace("-", ""));

        Pair<OntologyType, String> ndcCodePair = Pair.of(OntologyType.NDC, ndc);
        if (!codeToCuis.containsKey(ndcCodePair)) {
            codeToCuis.put(ndcCodePair, new TIntHashSet());
        }
        codeToCuis.get(ndcCodePair).add(cui);
        cuiToCodes.putIfAbsent(cui, new HashSet<>());
        cuiToCodes.get(cui).add(ndcCodePair);
    }

    public static void main(String[] args) throws IOException {
        String mrSatFile = args[0];
        String outputFile = args[1];

        NdcCodeNodeDecorator map = new NdcCodeNodeDecorator(new FileInputStream(mrSatFile), false);
        map.writeResourceFile(new FileOutputStream(outputFile));
    }
}
