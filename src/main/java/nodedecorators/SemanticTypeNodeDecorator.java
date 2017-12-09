package nodedecorators;

import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import graph.Node;
import org.apache.commons.lang3.StringUtils;
import utils.SemanticType;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public class SemanticTypeNodeDecorator implements NodeDecorator {
    private final TIntObjectMap<Set<SemanticType>> cuiToSemanticTypes = new TIntObjectHashMap<>();
    private final Map<SemanticType, TIntSet> semanticTypeToCuis = new HashMap<>();

    public SemanticTypeNodeDecorator(InputStream inputStream, boolean resourceFile) throws IOException {
        if (resourceFile) {
            readResourceFile(inputStream);
        } else {
            readMrSty(inputStream);
        }
    }

    private void readMrSty(final InputStream inputStream) throws IOException {
        Pattern recordSplitPattern = Pattern.compile("\\|");
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        while ((line = reader.readLine()) != null) {
            String[] record = recordSplitPattern.split(line, -1);
            final int cui = Integer.parseInt(record[0].substring(1));
            SemanticType semanticType = SemanticType.valueOf(record[1]);
            if (!semanticTypeToCuis.containsKey(semanticType)) {
                semanticTypeToCuis.put(semanticType, new TIntHashSet());
            }
            semanticTypeToCuis.get(semanticType).add(cui);
            cuiToSemanticTypes.putIfAbsent(cui, new HashSet<>());
            cuiToSemanticTypes.get(cui).add(semanticType);
        }
        reader.close();
    }

    Set<SemanticType> getSemanticTypesForCui(int cui) {
        if (!cuiToSemanticTypes.containsKey(cui)) {
            return new HashSet<>();
        }
        return cuiToSemanticTypes.get(cui);
    }

    private Set<SemanticType> getSemanticTypesForCuis(TIntSet cuis) {
        final Set<SemanticType> allTypes = new HashSet<>();
        cuis.forEach(cui -> {
            allTypes.addAll(getSemanticTypesForCui(cui));
            return true;
        });
        return allTypes;
    }

    TIntList allCuis() {
        TIntList allCuis = new TIntArrayList(cuiToSemanticTypes.keySet());
        allCuis.sort();
        return allCuis;
    }

    List<SemanticType> allSemanticTypes() {
        List<SemanticType> allTypes = new ArrayList<>(semanticTypeToCuis.keySet());
        Collections.sort(allTypes);
        return allTypes;
    }

    private void readResourceFile(InputStream inputStream) {
        Pattern tabSplitPattern = Pattern.compile("\t");
        Pattern pipeSplitPattern = Pattern.compile("\\|");
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        try {
            while ((line = reader.readLine()) != null) {
                String[] record = tabSplitPattern.split(line);
                int cui = Integer.parseInt(record[0]);
                String[] typeStrings = pipeSplitPattern.split(record[1]);
                for (String typeString : typeStrings) {
                    SemanticType type = SemanticType.valueOf(typeString);
                    if (!semanticTypeToCuis.containsKey(type)) {
                        semanticTypeToCuis.put(type, new TIntHashSet());
                    }
                    semanticTypeToCuis.get(type).add(cui);
                    cuiToSemanticTypes.putIfAbsent(cui, new HashSet<>());
                    cuiToSemanticTypes.get(cui).add(type);
                }
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writeResourceFile(OutputStream outputStream) {
        final PrintWriter printWriter = new PrintWriter(outputStream);
        allCuis().forEach(cui -> {
            List<SemanticType> types = new ArrayList<>(getSemanticTypesForCui(cui));
            Collections.sort(types);
            printWriter.println(cui + "\t" + StringUtils.join(types, "|"));
            return true;
        });
        printWriter.flush();
        printWriter.close();
    }

    @Override
    public void decorateNode(Node node) {
        node.addSemanticTypes(getSemanticTypesForCuis(node.getCuis()));
    }

    public static void main(String[] args) throws IOException {
        String mrStyFile = args[0];
        String outputFile = args[1];

        SemanticTypeNodeDecorator map = new SemanticTypeNodeDecorator(new FileInputStream(mrStyFile), false);
        map.writeResourceFile(new FileOutputStream(outputFile));
    }
}
