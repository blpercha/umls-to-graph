package nodedecorators;

import graph.Node;
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

public abstract class CodeTranslationNodeDecorator implements NodeDecorator {
    final Map<Pair<OntologyType, String>, Set<Pair<OntologyType, String>>> codeMap = new HashMap<>();
    boolean allowOneToMany = true;

    CodeTranslationNodeDecorator(InputStream resourceFileStream) throws IOException {
        readResourceFile(resourceFileStream);
    }

    protected CodeTranslationNodeDecorator() {
    }

    private List<Pair<OntologyType, String>> getAllCodes() {
        List<Pair<OntologyType, String>> codeList = new ArrayList<>(codeMap.keySet());
        Collections.sort(codeList);
        return codeList;
    }

    private void readResourceFile(InputStream inputStream) throws IOException {
        Pattern commaSplitPattern = Pattern.compile(",");
        Pattern tabSplitPattern = Pattern.compile("\t");
        BufferedReader resourceFileReader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        while ((line = resourceFileReader.readLine()) != null) {
            String[] record = tabSplitPattern.split(line);
            String[] firstOntologyPair = commaSplitPattern.split(record[0]);
            String[] secondOntologyPair = commaSplitPattern.split(record[1]);
            Pair<OntologyType, String> firstPair = Pair.of(OntologyType.valueOf(firstOntologyPair[0]),
                    firstOntologyPair[1]);
            Pair<OntologyType, String> secondPair = Pair.of(OntologyType.valueOf(secondOntologyPair[0]),
                    secondOntologyPair[1]);
            if (!codeMap.containsKey(firstPair)) {
                codeMap.put(firstPair, new HashSet<>());
            }
            if (!codeMap.containsKey(secondPair)) {
                codeMap.put(secondPair, new HashSet<>());
            }
            codeMap.get(firstPair).add(secondPair);
            codeMap.get(secondPair).add(firstPair);
        }
        resourceFileReader.close();
    }

    public void writeResourceFile(OutputStream outputStreamResourceFile) {
        final PrintWriter printWriterResourceFile = new PrintWriter(new OutputStreamWriter(outputStreamResourceFile));
        List<Pair<OntologyType, String>> codePairList = getAllCodes();
        Collections.sort(codePairList);
        for (Pair<OntologyType, String> code : codePairList) {
            List<Pair<OntologyType, String>> secondOntologyCodePairs = new ArrayList<>(codeMap.get(code));
            if (!allowOneToMany && secondOntologyCodePairs.size() > 1) {
                continue;
            }
            Collections.sort(secondOntologyCodePairs);
            for (Pair<OntologyType, String> secondOntologyCodePair : secondOntologyCodePairs) {
                if (!allowOneToMany && codeMap.get(secondOntologyCodePair).size() > 1) {
                    continue;
                }
                printWriterResourceFile.println(code.getLeft() + "," + code.getRight() + "\t" +
                        secondOntologyCodePair.getLeft() + "," + secondOntologyCodePair.getRight());
            }
        }
        printWriterResourceFile.flush();
        printWriterResourceFile.close();
    }

    @Override
    public void decorateNode(Node node) {
        Set<Pair<OntologyType, String>> codePairsAlreadyPresent = new HashSet<>(node.getCodes());
        Set<Pair<OntologyType, String>> newCodePairs = new HashSet<>();
        for (Pair<OntologyType, String> codePair : codePairsAlreadyPresent) {
            newCodePairs.addAll(getAlternateCodePairs(codePair));
        }
        node.addCodes(newCodePairs);
    }

    Set<Pair<OntologyType, String>> getAlternateCodePairs(Pair<OntologyType, String> codePair) {
        if (!codeMap.containsKey(codePair)) {
            return new HashSet<>();
        }
        return codeMap.get(codePair);
    }
}
