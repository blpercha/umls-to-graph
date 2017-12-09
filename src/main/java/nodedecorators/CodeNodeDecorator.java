package nodedecorators;

import gnu.trove.TDecorators;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import graph.Node;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import utils.OntologyType;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public abstract class CodeNodeDecorator implements NodeDecorator {
    final TIntObjectMap<Set<Pair<OntologyType, String>>> cuiToCodes = new TIntObjectHashMap<>();
    final Map<Pair<OntologyType, String>, TIntSet> codeToCuis = new HashMap<>();

    private Set<Pair<OntologyType, String>> getCodesForCui(int cui) {
        if (!cuiToCodes.containsKey(cui)) {
            return new HashSet<>();
        }
        return cuiToCodes.get(cui);
    }

    private Set<Pair<OntologyType, String>> getCodesForCuis(TIntSet cuis) {
        final Set<Pair<OntologyType, String>> codes = new HashSet<>();
        cuis.forEach(cui -> {
            codes.addAll(getCodesForCui(cui));
            return true;
        });
        return codes;
    }

    private TIntSet getCuisFromCode(Pair<OntologyType, String> code) {
        if (!codeToCuis.containsKey(code)) {
            return new TIntHashSet();
        }
        return codeToCuis.get(code);
    }

    private List<Pair<OntologyType, String>> allCodes() {
        List<Pair<OntologyType, String>> codeList = new ArrayList<>(codeToCuis.keySet());
        Collections.sort(codeList);
        return codeList;
    }

    void writeResourceFile(OutputStream outputStream) {
        final PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(outputStream));
        List<Pair<OntologyType, String>> codeList = allCodes();
        Collections.sort(codeList);
        for (Pair<OntologyType, String> code : codeList) {
            TIntList cuis = new TIntArrayList(getCuisFromCode(code));
            cuis.sort();
            printWriter.println(code.getLeft() + "," + code.getRight() + "\t" +
                    StringUtils.join(TDecorators.wrap(cuis), ","));
        }
        printWriter.flush();
        printWriter.close();
    }

    @Override
    public void decorateNode(Node node) {
        node.addCodes(getCodesForCuis(node.getCuis()));
    }

    void readResourceFile(InputStream inputStream) throws IOException {
        Pattern recordSplitPattern = Pattern.compile("\t");
        Pattern commaSplitPattern = Pattern.compile(",");
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        while ((line = reader.readLine()) != null) {
            String[] record = recordSplitPattern.split(line);
            String[] codeString = commaSplitPattern.split(record[0]);
            OntologyType ontologyType = OntologyType.fromString(codeString[0]);
            String codeValue = codeString[1];
            Pair<OntologyType, String> code = Pair.of(ontologyType, codeValue);
            codeToCuis.put(code, new TIntHashSet());
            for (String cuiString : commaSplitPattern.split(record[1])) {
                int cui = Integer.parseInt(cuiString);
                codeToCuis.get(code).add(cui);
                cuiToCodes.putIfAbsent(cui, new HashSet<>());
                cuiToCodes.get(cui).add(code);
            }
        }
        reader.close();
    }
}
