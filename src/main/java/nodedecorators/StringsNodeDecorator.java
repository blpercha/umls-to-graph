package nodedecorators;

import com.google.common.base.CharMatcher;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import graph.Node;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.zip.GZIPOutputStream;

public class StringsNodeDecorator implements NodeDecorator {
    private final TIntObjectHashMap<Set<String>> cuiToDescriptionMap = new TIntObjectHashMap<>();
    private final Map<String, TIntSet> descriptionToCuisMap = new HashMap<>();

    public StringsNodeDecorator(InputStream resourceInputStream) throws IOException {
        readResourceFile(resourceInputStream);
    }

    private StringsNodeDecorator(InputStream mrConsoFile, InputStream lrsplFile) throws IOException {
        readMrConso(mrConsoFile);
        readLrspl(lrsplFile);
    }

    // E0731112|rate independent|rate-independent|
    private void readLrspl(InputStream inputStream) throws IOException {
        Pattern recordSplitPattern = Pattern.compile("\\|");
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        while ((line = reader.readLine()) != null) {
            String[] splitLine = recordSplitPattern.split(line, -1);
            final String s1 = splitLine[1];
            final String s2 = splitLine[2];
            TIntSet cuiS1 = getCuis(s1);
            final TIntSet cuiS2 = getCuis(s2);
            cuiS1.forEach(cui -> {
                cuiToDescriptionMap.get(cui).add(s2);
                if (!descriptionToCuisMap.containsKey(s2)) {
                    descriptionToCuisMap.put(s2, new TIntHashSet());
                }
                descriptionToCuisMap.get(s2).add(cui);
                return true;
            });
            cuiS2.forEach(cui -> {
                cuiToDescriptionMap.get(cui).add(s1);
                if (!descriptionToCuisMap.containsKey(s1)) {
                    descriptionToCuisMap.put(s1, new TIntHashSet());
                }
                descriptionToCuisMap.get(s1).add(cui);
                return true;
            });
        }
        reader.close();
    }

    private void readMrConso(InputStream inputStream) throws IOException {
        Pattern recordSplitPattern = Pattern.compile("\\|");
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        while ((line = reader.readLine()) != null) {
            String[] splitLine = recordSplitPattern.split(line, -1);
            processMrConsoRecord(splitLine);
        }
        reader.close();
    }

    private void processMrConsoRecord(String[] data) {
        int cui = Integer.parseInt(data[0].substring(1));
        String descriptionString = data[14];
        if (!data[1].equals("ENG")) {
            return;
        }
        cuiToDescriptionMap.putIfAbsent(cui, new HashSet<>());
        cuiToDescriptionMap.get(cui).add(descriptionString);
        if (!descriptionToCuisMap.containsKey(descriptionString)) {
            if (CharMatcher.ASCII.matchesAllOf(descriptionString)) {
                descriptionToCuisMap.put(descriptionString, new TIntHashSet());
                descriptionToCuisMap.get(descriptionString).add(cui);
            } else {
                System.err.println("Non-ASCII description; skipping: " + descriptionString);
            }
        }
    }

    private Set<String> getDescriptions(int cui) {
        if (!cuiToDescriptionMap.containsKey(cui)) {
            return new HashSet<>();
        }
        return cuiToDescriptionMap.get(cui);
    }

    private Set<String> getDescriptions(TIntSet cuis) {
        final Set<String> descriptions = new HashSet<>();
        cuis.forEach(cui -> {
            descriptions.addAll(StringsNodeDecorator.this.getDescriptions(cui));
            return true;
        });
        return descriptions;
    }

    private TIntSet getCuis(String description) {
        if (!descriptionToCuisMap.containsKey(description)) {
            return new TIntHashSet();
        }
        return descriptionToCuisMap.get(description);
    }

    private TIntList allCuis() {
        TIntList cuiList = new TIntArrayList(cuiToDescriptionMap.keySet());
        cuiList.sort();
        return cuiList;
    }

    private void readResourceFile(InputStream inputStream) throws IOException {
        Pattern recordSplitPattern = Pattern.compile("\t");
        Pattern descriptionsSplitPattern = Pattern.compile("\\|");
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        while ((line = reader.readLine()) != null) {
            final String[] record = recordSplitPattern.split(line.trim());
            final int cui = Integer.parseInt(record[0]);
            final String[] descriptions = descriptionsSplitPattern.split(record[1]);
            cuiToDescriptionMap.putIfAbsent(cui, new HashSet<>());
            cuiToDescriptionMap.get(cui).addAll(Arrays.asList(descriptions));
            for (String description : descriptions) {
                if (!descriptionToCuisMap.containsKey(description)) {
                    descriptionToCuisMap.put(description, new TIntHashSet());
                }
                descriptionToCuisMap.get(description).add(cui);
            }
        }
        reader.close();
    }

    private void writeResourceFile(OutputStream outputStream) {
        final PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(outputStream));
        TIntList cuiList = allCuis();
        cuiList.forEach(i -> {
            printWriter.println(i + "\t" + StringUtils.join(StringsNodeDecorator.this.getDescriptions(i), "|"));
            return true;
        });
        printWriter.flush();
        printWriter.close();
    }

    @Override
    public void decorateNode(Node node) {
        node.addStrings(getDescriptions(node.getCuis()));
    }

    public static void main(String[] args) throws IOException {
        String mrConsoFile = args[0];
        String lrsplFile = args[1];
        String outputFile = args[2];

        StringsNodeDecorator stringCuiDecoratorMap = new StringsNodeDecorator(
                new FileInputStream(mrConsoFile), new FileInputStream(lrsplFile));
        stringCuiDecoratorMap.writeResourceFile(new GZIPOutputStream(new FileOutputStream(outputFile)));
    }
}
