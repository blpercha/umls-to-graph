package nodedecorators;

import org.apache.commons.lang3.tuple.Pair;
import utils.OntologyType;
import utils.TextFormatUtils;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;

public class Icd9Icd10CodeTranslationNodeDecorator extends CodeTranslationNodeDecorator {

    public Icd9Icd10CodeTranslationNodeDecorator(InputStream resourceFileInputStream) throws IOException {
        super(resourceFileInputStream);
    }

    Icd9Icd10CodeTranslationNodeDecorator(InputStream inputStreamI9gem, InputStream inputStreamI10gem,
                                          boolean allowOneToMany) throws IOException {
        this.allowOneToMany = allowOneToMany;
        readFromOriginalData(inputStreamI9gem, inputStreamI10gem);
    }

    private void readFromOriginalData(InputStream inputStreamI9gem, InputStream inputStreamI10gem) throws IOException {
        readI10File(inputStreamI10gem);
        readI9File(inputStreamI9gem);
    }

    private void readI9File(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

        String line;
        while ((line = bufferedReader.readLine()) != null) {
            String[] sl = line.split("\\s+");
            if (sl[0].equals("NoDx") || sl[1].equals("NoDx")) {
                continue;
            }
            if (sl[2].startsWith("1")) { // approximate match
                continue;
            }
            String icd9Code = TextFormatUtils.insertPeriods(sl[0]);
            String icd10Code = TextFormatUtils.insertPeriods(sl[1]);
            addToCodeMap(icd9Code, icd10Code);
        }
        bufferedReader.close();
    }

    private void readI10File(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

        String line;
        while ((line = bufferedReader.readLine()) != null) {
            String[] sl = line.split("\\s+");
            if (sl[0].equals("NoDx") || sl[1].equals("NoDx")) {
                continue;
            }
            if (sl[2].startsWith("1")) { // approximate match
                continue;
            }
            String icd10Code = TextFormatUtils.insertPeriods(sl[0]);
            String icd9Code = TextFormatUtils.insertPeriods(sl[1]);
            addToCodeMap(icd9Code, icd10Code);
        }
        bufferedReader.close();
    }

    private void addToCodeMap(String icd9Code, String icd10Code) {
        Pair<OntologyType, String> icd9CodePair = Pair.of(OntologyType.ICD9CM, icd9Code);
        Pair<OntologyType, String> icd10CodePair = Pair.of(OntologyType.ICD10PCS, icd10Code);
        if (!codeMap.containsKey(icd9CodePair)) {
            codeMap.put(icd9CodePair, new HashSet<>());
        }
        if (!codeMap.containsKey(icd10CodePair)) {
            codeMap.put(icd10CodePair, new HashSet<>());
        }
        codeMap.get(icd9CodePair).add(icd10CodePair);
        codeMap.get(icd10CodePair).add(icd9CodePair);
    }

    public static void main(String[] args) throws IOException {
        String i9GemFile = args[0];
        String i10GemFile = args[1];
        String outputResourceFile = args[2];

        Icd9Icd10CodeTranslationNodeDecorator map =
                new Icd9Icd10CodeTranslationNodeDecorator(new FileInputStream(i9GemFile),
                        new FileInputStream(i10GemFile), false);
        map.writeResourceFile(new FileOutputStream(outputResourceFile));
    }

}
