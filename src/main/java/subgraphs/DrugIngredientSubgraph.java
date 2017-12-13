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
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

public class DrugIngredientSubgraph implements Subgraph {
    private final TIntObjectMap<TIntSet> rxNormCuiToIngredients = new TIntObjectHashMap<>();
    private final TIntObjectMap<TIntSet> ingredientToRxNormCuis = new TIntObjectHashMap<>();
    private final TIntSet allowedCuis = new TIntHashSet();

    private final static Set<String> allowedTty = new HashSet<>(Arrays.asList("SBD", "SBDC", "SBDF",
            "SCDF", "SCDC", "SCD", "IN", "BN", "BD", "PIN"));

    public DrugIngredientSubgraph(InputStream resourceFileInputStream) throws IOException {
        readResourceFile(resourceFileInputStream);
    }

    private DrugIngredientSubgraph(InputStream mrRelInputStream, InputStream mrConsoInputStream) throws IOException {
        readMrConso(mrConsoInputStream); // sets up list of allowed CUIs - must be done first
        readMrRel(mrRelInputStream);
    }

    private void readMrConso(final InputStream inputStream) throws IOException {
        Pattern recordSplitPattern = Pattern.compile("\\|");
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        while ((line = reader.readLine()) != null) {
            String[] record = recordSplitPattern.split(line, -1);
            OntologyType ontologyType;
            try {
                ontologyType = OntologyType.valueOf(record[11]);
            } catch (IllegalArgumentException e) { // unrecognized ontology
                continue;
            }
            if (!ontologyType.equals(OntologyType.RXNORM)) {
                continue;
            }
            final String tty = record[12];
            if (!allowedTty.contains(tty)) {
                continue;
            }
            int cui = Integer.parseInt(record[0].substring(1));
            allowedCuis.add(cui);
        }
        reader.close();
    }

    private void readMrRel(InputStream inputStream) throws IOException {
        Pattern recordSplitPattern = Pattern.compile("\\|");
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        while ((line = reader.readLine()) != null) {
            String[] record = recordSplitPattern.split(line);
            processMrRelRecord(record);
        }
        reader.close();
    }

    private void processMrRelRecord(String[] record) {
        OntologyType ontologyType;
        try {
            ontologyType = OntologyType.valueOf(record[10]);
        } catch (IllegalArgumentException e) { // unrecognized ontology
            return;
        }
        if (!ontologyType.equals(OntologyType.RXNORM)) {
            return;
        }

        final int ingredientCui = Integer.parseInt(record[0].substring(1));
        final int specificCui = Integer.parseInt(record[4].substring(1));

        if (!allowedCuis.contains(ingredientCui) || !allowedCuis.contains(specificCui)) {
            return;
        }

        final String relationshipType = record[7];

        if (relationshipType.equals("has_ingredient") || relationshipType.equals("form_of") ||
                relationshipType.equals("tradename_of") || relationshipType.equals("consists_of") ||
                relationshipType.equals("isa") || relationshipType.equals("has_precise_ingredient")) {
            rxNormCuiToIngredients.putIfAbsent(specificCui, new TIntHashSet());
            rxNormCuiToIngredients.get(specificCui).add(ingredientCui);
            ingredientToRxNormCuis.putIfAbsent(ingredientCui, new TIntHashSet());
            ingredientToRxNormCuis.get(ingredientCui).add(specificCui);
        }
    }

    private void readResourceFile(InputStream resourceFileInputStream) throws IOException {
        Pattern recordSplitPattern = Pattern.compile("\t");
        Pattern cuiSplitPattern = Pattern.compile(",");
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(resourceFileInputStream));
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            String[] record = recordSplitPattern.split(line);
            int cui = Integer.parseInt(record[0]);
            rxNormCuiToIngredients.putIfAbsent(cui, new TIntHashSet());
            for (String ingredientCuiString : cuiSplitPattern.split(record[1])) {
                int ingredientCui = Integer.parseInt(ingredientCuiString);
                rxNormCuiToIngredients.get(cui).add(ingredientCui);
                ingredientToRxNormCuis.putIfAbsent(ingredientCui, new TIntHashSet());
                ingredientToRxNormCuis.get(ingredientCui).add(cui);
            }
        }
        bufferedReader.close();
    }

    @Override
    public TIntSet getChildren(int cui) {
        if (!ingredientToRxNormCuis.containsKey(cui)) {
            return new TIntHashSet();
        }
        return ingredientToRxNormCuis.get(cui);
    }

    @Override
    public TIntSet getParents(int cui) {
        if (!rxNormCuiToIngredients.containsKey(cui)) {
            return new TIntHashSet();
        }
        return rxNormCuiToIngredients.get(cui);
    }

    @Override
    public TIntList allCuis() {
        TIntSet cuiSet = new TIntHashSet(rxNormCuiToIngredients.keySet());
        cuiSet.addAll(ingredientToRxNormCuis.keySet());
        TIntList cuiList = new TIntArrayList(cuiSet);
        cuiList.sort();
        return cuiList;
    }

    private void writeResourceFile(OutputStream outputStreamCuiToIngredients) {
        final PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(outputStreamCuiToIngredients));
        TIntList cuiList = new TIntArrayList(rxNormCuiToIngredients.keySet());
        cuiList.sort();
        cuiList.forEach(cui -> {
            printWriter.println(cui + "\t" + StringUtils.join(TDecorators.wrap(getParents(cui)), ","));
            return true;
        });
        printWriter.flush();
        printWriter.close();
    }

    public static void main(String args[]) throws IOException {
        String mrRelFile = args[0];             // MRREL.RRF
        String mrConsoFile = args[1];           // MRCONSO.RRF
        String outputFile = args[2];            // umls-rxnormcui-ingredients.txt

        DrugIngredientSubgraph subgraph = new DrugIngredientSubgraph(new FileInputStream(mrRelFile),
                new FileInputStream(mrConsoFile));
        subgraph.writeResourceFile(new FileOutputStream(outputFile));
    }
}
