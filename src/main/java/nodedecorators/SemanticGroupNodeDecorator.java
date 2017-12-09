package nodedecorators;

import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import graph.Node;
import org.apache.commons.lang3.StringUtils;
import utils.SemanticGroup;
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

public class SemanticGroupNodeDecorator implements NodeDecorator {
    private final Map<SemanticGroup, TIntSet> groupToCuis = new HashMap<>();
    private final TIntObjectMap<Set<SemanticGroup>> cuiToGroups = new TIntObjectHashMap<>();

    public SemanticGroupNodeDecorator(InputStream inputStream, boolean resourceFile) throws IOException {
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
            SemanticGroup thisGroup = SemanticGroup.fromType(SemanticType.valueOf(record[1]));
            if (!groupToCuis.containsKey(thisGroup)) {
                groupToCuis.put(thisGroup, new TIntHashSet());
            }
            groupToCuis.get(thisGroup).add(cui);
            cuiToGroups.putIfAbsent(cui, new HashSet<>());
            cuiToGroups.get(cui).add(thisGroup);
        }
        reader.close();
    }

    Set<SemanticGroup> getGroupsForCui(int cui) {
        if (!cuiToGroups.containsKey(cui)) {
            return new HashSet<>();
        }
        return cuiToGroups.get(cui);
    }

    private Set<SemanticGroup> getGroupsForCuis(TIntSet cuis) {
        final Set<SemanticGroup> allGroups = new HashSet<>();
        cuis.forEach(cui -> {
            allGroups.addAll(getGroupsForCui(cui));
            return true;
        });
        return allGroups;
    }

    TIntList allCuis() {
        TIntList allCuis = new TIntArrayList(cuiToGroups.keySet());
        allCuis.sort();
        return allCuis;
    }

    List<SemanticGroup> allGroups() {
        List<SemanticGroup> allGroups = new ArrayList<>(groupToCuis.keySet());
        Collections.sort(allGroups);
        return allGroups;
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
                String[] groupStrings = pipeSplitPattern.split(record[1]);
                for (String groupString : groupStrings) {
                    SemanticGroup group = SemanticGroup.valueOf(groupString);
                    if (!groupToCuis.containsKey(group)) {
                        groupToCuis.put(group, new TIntHashSet());
                    }
                    groupToCuis.get(group).add(cui);
                    cuiToGroups.putIfAbsent(cui, new HashSet<>());
                    cuiToGroups.get(cui).add(group);
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
            List<SemanticGroup> groups = new ArrayList<>(getGroupsForCui(cui));
            Collections.sort(groups);
            printWriter.println(cui + "\t" + StringUtils.join(groups, "|"));
            return true;
        });
        printWriter.flush();
        printWriter.close();
    }

    @Override
    public void decorateNode(Node node) {
        node.addSemanticGroups(getGroupsForCuis(node.getCuis()));
    }

    public static void main(String[] args) throws IOException {
        String mrStyFile = args[0];
        String outputFile = args[1];

        SemanticGroupNodeDecorator map = new SemanticGroupNodeDecorator(new FileInputStream(mrStyFile), false);
        map.writeResourceFile(new FileOutputStream(outputFile));
    }
}
