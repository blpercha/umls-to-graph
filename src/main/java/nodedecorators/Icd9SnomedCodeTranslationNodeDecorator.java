package nodedecorators;

import org.apache.commons.lang3.tuple.Pair;
import utils.OntologyType;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;

public class Icd9SnomedCodeTranslationNodeDecorator extends CodeTranslationNodeDecorator {

    public Icd9SnomedCodeTranslationNodeDecorator(InputStream resourceFileInputStream) throws IOException {
        super(resourceFileInputStream);
    }

    Icd9SnomedCodeTranslationNodeDecorator(InputStream map1to1, InputStream map1toM) throws IOException {
        this.allowOneToMany = true;
        readFromOriginalData(map1to1, map1toM);
    }

    private void readFromOriginalData(InputStream diagnoses1to1, InputStream diagnoses1toM) throws IOException {
        readSingleDataInputStream(diagnoses1to1);
        readSingleDataInputStream(diagnoses1toM);
    }

    private void readSingleDataInputStream(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

        String line;
        String headerLine = bufferedReader.readLine();
        String[] headers = headerLine.split("\t");
        int icdCodeIndex = 0;
        int snomedCidIndex = 0;
        int coreUsageIndex = 0;
        for (int i = 0; i < headers.length; i++) {
            switch (headers[i]) {
                case "ICD_CODE":
                    icdCodeIndex = i;
                    break;
                case "SNOMED_CID":
                    snomedCidIndex = i;
                    break;
                case "CORE_USAGE":
                    coreUsageIndex = i;
                    break;
            }
        }
        while ((line = bufferedReader.readLine()) != null) {
            String[] sl = line.split("\\t");
            try {
                String icd9Code = sl[icdCodeIndex];
                String snomedCode = sl[snomedCidIndex];
                if (!(coreUsageIndex == 0)) {
                    if (sl[coreUsageIndex].isEmpty()) {
                        continue; // field might be empty
                    }
                    if (sl[coreUsageIndex].equals("NULL")) {
                        continue; // field might be NULL
                    }
                    double coreUsage = Double.parseDouble(sl[coreUsageIndex]);
                    if (!(coreUsage > 0.0)) { // require core usage greater than zero to be mapped
                        continue;
                    }
                }
                if (!icd9Code.matches("[A-Z0-9.]+")) { /* check formatting for icd9 and snomed codes */
                    System.out.println("Illegal ICD9 code: " + icd9Code);
                    continue;
                }
                if (!snomedCode.matches("[0-9]+")) {
                    System.out.println("Illegal SNOMED code: " + snomedCode);
                    continue;
                }
                Pair<OntologyType, String> pairIcd9 = Pair.of(OntologyType.ICD9CM, icd9Code);
                Pair<OntologyType, String> pairSnomed = Pair.of(OntologyType.SNOMEDCT_US, snomedCode);
                if (!codeMap.containsKey(pairIcd9)) {
                    codeMap.put(pairIcd9, new HashSet<>());
                }
                if (!codeMap.containsKey(pairSnomed)) {
                    codeMap.put(pairSnomed, new HashSet<>());
                }
                codeMap.get(pairIcd9).add(pairSnomed);
                codeMap.get(pairSnomed).add(pairIcd9);
            } catch (ArrayIndexOutOfBoundsException e) {
                System.out.println("No conversion specified:\t" + line);
            }
        }
        bufferedReader.close();
    }

    public static void main(String[] args) throws IOException {
        String map1to1File = args[0];
        String map1toMFile = args[1];
        String outputFileIcd9ToSnomed = args[2];

        Icd9SnomedCodeTranslationNodeDecorator map = new Icd9SnomedCodeTranslationNodeDecorator(
                new FileInputStream(map1to1File), new FileInputStream(map1toMFile));
        map.writeResourceFile(new FileOutputStream(outputFileIcd9ToSnomed));
    }
}
