package nodedecorators;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Before;
import org.junit.Test;
import utils.OntologyType;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashSet;

import static org.junit.Assert.assertEquals;

public class Icd9SnomedCodeTranslationNodeDecoratorTest {
    private Icd9SnomedCodeTranslationNodeDecorator decorator;

    @Before
    public void setUp() throws Exception {
        String data1to1 = "ICD_CODE\tICD_NAME\tIS_CURRENT_ICD\tIP_USAGE\tOP_USAGE\tAVG_USAGE\tIS_NEC\tSNOMED_CID\tSNOMED_FSN\tIS_1-1MAP\tCORE_USAGE\tIN_CORE\n" +
                "427.31\tAtrial fibrillation\t1\t1.89778\t3.20644\t2.55211\t0\t49436004\tAtrial fibrillation (disorder)\t1\t0.499\t1\n" +
                "599.0\tUrinary tract infection, site not specified\t1\t2.13933\t1.6533\t1.896315\t0\t68566005\tUrinary tract infectious disease (disorder)\t1\t0.5362\t1\n" +
                "486\tPneumonia, organism unspecified\t1\t3.4176\t0.30077\t1.859185\t0\t233604007\tPneumonia (disorder)\t1\t0.4722\t1\n" +
                "585.6\tEnd stage renal disease\t1\t0.04875\t3.23327\t1.64101\t0\t46177005\tEnd stage renal disease (disorder)\t1\t0.0057\t1\n" +
                "414.01\tCoronary atherosclerosis of native coronary artery\t1\t2.58624\t0.68026\t1.63325\t0\t53741008\tCoronary arteriosclerosis (disorder)\t1\t0.8621\t1\n" +
                "401.9\tUnspecified essential hypertension\t1\t0.21617\t2.73234\t1.474255\t0\t59621000\tEssential hypertension (disorder)\t1\t0.9291\t1\n" +
                "428.0\tCongestive heart failure, unspecified\t1\t2.01765\t0.82211\t1.41988\t0\t42343007\tCongestive heart failure (disorder)\t1\t0.4631\t1\n" +
                "491.21\tObstructive chronic bronchitis with (acute) exacerbation\t1\t2.41931\t0.13943\t1.27937\t0\t195951007\tAcute exacerbation of chronic obstructive airways disease (disorder)\t1\t0.0521\t1\n" +
                "038.9\tUnspecified septicemia\t1\t2.46447\t0.02169\t1.24308\t0\t105592009\tSepticemia (disorder)\t1\t0.0808\t1\n" +
                "285.9\tAnemia, unspecified\t1\t0.25523\t1.58376\t0.919495\t0\t271737000\tAnemia (disorder)\t1\t0.5822\t1\n" +
                "584.9\tAcute kidney failure, unspecified\t1\t1.69018\t0.06297\t0.876575\t0\t14669001\tAcute renal failure syndrome (disorder)\t1\t0.0541\t1\n" +
                "780.2\tSyncope and collapse\t1\t1.0951\t0.33795\t0.716525\t0\t309585006\tSyncope and collapse (disorder)\t1\t0.0025\t1\n" +
                "786.50\tChest pain, unspecified\t1\t0.49112\t0.82387\t0.657495\t0\t29857009\tChest pain (finding)\t1\t0.4714\t1\n" +
                "401.1\tBenign essential hypertension\t1\t0.00147\t1.21016\t0.605815\t0\t1201005\tBenign essential hypertension (disorder)\t1\t0.1219\t1\n" +
                "185\tMalignant neoplasm of prostate\t1\t0.24826\t0.82335\t0.535805\t0\t399068003\tMalignant tumor of prostate (disorder)\t1\t0.2915\t1\n" +
                "518.81\tAcute respiratory failure\t1\t1.03558\t0.01745\t0.526515\t0\t65710008\tAcute respiratory failure (disorder)\t1\t0.0009\t1\n" +
                "507.0\tPneumonitis due to inhalation of food or vomitus\t1\t1.03489\t0.00852\t0.521705\t0\t196032009\tPneumonitis due to inhalation of food or vomitus (disorder)\t1\t0.0145\t1\n" +
                "276.51\tDehydration\t1\t0.90459\t0.11995\t0.51227\t0\t34095006\tDehydration (disorder)\t1\t0.0478\t1\n" +
                "428.23\tAcute on chronic systolic heart failure\t1\t0.90805\t0.00988\t0.458965\t0\t443253003\tAcute on chronic systolic heart failure (disorder)\t1\t\t0\n" +
                "244.9\tUnspecified acquired hypothyroidism\t1\t0.02025\t0.87624\t0.448245\t0\t40930008\tHypothyroidism (disorder)\t1\t0.8226\t1\n" +
                "272.0\tPure hypercholesterolemia\t1\t0.00013\t0.88307\t0.4416\t0\t267432004\tPure hypercholesterolemia (disorder)\t1\t0.0305\t1\n" +
                "789.00\tAbdominal pain, unspecified site\t1\t0.10988\t0.75424\t0.43206\t0\t21522001\tAbdominal pain (finding)\t1\t0.6117\t1\n" +
                "724.2\tLumbago\t1\t0.07047\t0.76572\t0.418095\t0\t279039007\tLow back pain (finding)\t1\t0.5862\t1";
        String data1toM = "ICD_CODE\tICD_NAME\tIS_CURRENT_ICD\tIP_USAGE\tOP_USAGE\tAVG_USAGE\tIS_NEC\tSNOMED_CID\tSNOMED_FSN\tIS_1-1MAP\tCORE_USAGE\tIN_CORE\n" +
                "V76.12\tOther screening mammogram\t1\t\t2.88756\t1.44378\t1\t\t\t0\t\t0\n" +
                "250.00\tDiabetes mellitus without mention of complication, type II or unspecified type, not stated as uncontrolled\t1\t0.02583\t2.73307\t1.37945\t0\t44054006\tDiabetes mellitus type 2 (disorder)\t0\t1.0432\t1\n" +
                "250.00\tDiabetes mellitus without mention of complication, type II or unspecified type, not stated as uncontrolled\t1\t0.02583\t2.73307\t1.37945\t0\t73211009\tDiabetes mellitus (disorder)\t0\t0.9239\t1\n" +
                "250.00\tDiabetes mellitus without mention of complication, type II or unspecified type, not stated as uncontrolled\t1\t0.02583\t2.73307\t1.37945\t0\t313436004\tType II diabetes mellitus without complication (disorder)\t0\t0.0937\t1\n" +
                "250.00\tDiabetes mellitus without mention of complication, type II or unspecified type, not stated as uncontrolled\t1\t0.02583\t2.73307\t1.37945\t0\t11530004\tBrittle diabetes mellitus (finding)\t0\t0.0398\t1\n" +
                "250.00\tDiabetes mellitus without mention of complication, type II or unspecified type, not stated as uncontrolled\t1\t0.02583\t2.73307\t1.37945\t0\t237599002\tInsulin treated type 2 diabetes mellitus (disorder)\t0\t0.0139\t1\n" +
                "250.00\tDiabetes mellitus without mention of complication, type II or unspecified type, not stated as uncontrolled\t1\t0.02583\t2.73307\t1.37945\t0\t81531005\tDiabetes mellitus type 2 in obese (disorder)\t0\t0.0022\t1\n" +
                "250.00\tDiabetes mellitus without mention of complication, type II or unspecified type, not stated as uncontrolled\t1\t0.02583\t2.73307\t1.37945\t0\t199230006\tPre-existing type 2 diabetes mellitus (disorder)\t0\t0.0004\t1\n" +
                "250.00\tDiabetes mellitus without mention of complication, type II or unspecified type, not stated as uncontrolled\t1\t0.02583\t2.73307\t1.37945\t0\t609572000\tMaturity-onset diabetes of the young, type 5 (disorder)\t0\t\t0\n" +
                "250.00\tDiabetes mellitus without mention of complication, type II or unspecified type, not stated as uncontrolled\t1\t0.02583\t2.73307\t1.37945\t0\t609573005\tMaturity-onset diabetes of the young, type 6 (disorder)\t0\t\t0\n" +
                "250.00\tDiabetes mellitus without mention of complication, type II or unspecified type, not stated as uncontrolled\t1\t0.02583\t2.73307\t1.37945\t0\t609574004\tMaturity-onset diabetes of the young, type 7 (disorder)\t0\t\t0\n" +
                "250.00\tDiabetes mellitus without mention of complication, type II or unspecified type, not stated as uncontrolled\t1\t0.02583\t2.73307\t1.37945\t0\t609575003\tMaturity-onset diabetes of the young, type 8 (disorder)\t0\t\t0\n" +
                "250.00\tDiabetes mellitus without mention of complication, type II or unspecified type, not stated as uncontrolled\t1\t0.02583\t2.73307\t1.37945\t0\t609576002\tMaturity-onset diabetes of the young, type 9 (disorder)\t0\t\t0\n" +
                "250.00\tDiabetes mellitus without mention of complication, type II or unspecified type, not stated as uncontrolled\t1\t0.02583\t2.73307\t1.37945\t0\t609577006\tMaturity-onset diabetes of the young, type 10 (disorder)\t0\t\t0\n" +
                "250.00\tDiabetes mellitus without mention of complication, type II or unspecified type, not stated as uncontrolled\t1\t0.02583\t2.73307\t1.37945\t0\t609578001\tMaturity-onset diabetes of the young, type 11 (disorder)\t0\t\t0\n" +
                "250.00\tDiabetes mellitus without mention of complication, type II or unspecified type, not stated as uncontrolled\t1\t0.02583\t2.73307\t1.37945\t0\t111552007\tDiabetes mellitus without complication (disorder)\t0\t\t0\n" +
                "250.00\tDiabetes mellitus without mention of complication, type II or unspecified type, not stated as uncontrolled\t1\t0.02583\t2.73307\t1.37945\t0\t166928007\tGlucose tolerance test indicates diabetes mellitus (finding)\t0\t\t0\n" +
                "250.00\tDiabetes mellitus without mention of complication, type II or unspecified type, not stated as uncontrolled\t1\t0.02583\t2.73307\t1.37945\t0\t170745003\tDiabetic on diet only (finding)\t0\t\t0\n" +
                "250.00\tDiabetes mellitus without mention of complication, type II or unspecified type, not stated as uncontrolled\t1\t0.02583\t2.73307\t1.37945\t0\t170746002\tDiabetic on oral treatment (finding)\t0\t\t0\n" +
                "250.00\tDiabetes mellitus without mention of complication, type II or unspecified type, not stated as uncontrolled\t1\t0.02583\t2.73307\t1.37945\t0\t237604008\tMaturity onset diabetes of the young, type 2 (disorder)\t0\t\t0\n" +
                "250.00\tDiabetes mellitus without mention of complication, type II or unspecified type, not stated as uncontrolled\t1\t0.02583\t2.73307\t1.37945\t0\t237608006\tLipodystrophy, partial, with Rieger anomaly, short stature, and insulinopenic diabetes mellitus (disorder)\t0\t\t0\n" +
                "250.00\tDiabetes mellitus without mention of complication, type II or unspecified type, not stated as uncontrolled\t1\t0.02583\t2.73307\t1.37945\t0\t237610008\tAcrorenal field defect, ectodermal dysplasia, and lipoatrophic diabetes (disorder)\t0\t\t0\n" +
                "250.00\tDiabetes mellitus without mention of complication, type II or unspecified type, not stated as uncontrolled\t1\t0.02583\t2.73307\t1.37945\t0\t237611007";
        decorator = new Icd9SnomedCodeTranslationNodeDecorator(new ByteArrayInputStream(data1to1.getBytes()),
                new ByteArrayInputStream(data1toM.getBytes()));
    }

    @Test
    public void testGetSnomedFromIcd9() {
        assertEquals(decorator.getAlternateCodePairs(Pair.of(OntologyType.ICD9CM, "250.0")),
                new HashSet<Pair<OntologyType, String>>());
    }

    @Test
    public void testReadWriteFromResourceFile() throws IOException {
        OutputStream outputStream = new ByteArrayOutputStream();
        decorator.writeResourceFile(outputStream);
        String resourceFileAsWritten = outputStream.toString();
        assertEquals(resourceFileAsWritten, "ICD9CM,038.9\tSNOMEDCT_US,105592009\n" +
                "ICD9CM,185\tSNOMEDCT_US,399068003\n" +
                "ICD9CM,244.9\tSNOMEDCT_US,40930008\n" +
                "ICD9CM,250.00\tSNOMEDCT_US,11530004\n" +
                "ICD9CM,250.00\tSNOMEDCT_US,199230006\n" +
                "ICD9CM,250.00\tSNOMEDCT_US,237599002\n" +
                "ICD9CM,250.00\tSNOMEDCT_US,313436004\n" +
                "ICD9CM,250.00\tSNOMEDCT_US,44054006\n" +
                "ICD9CM,250.00\tSNOMEDCT_US,73211009\n" +
                "ICD9CM,250.00\tSNOMEDCT_US,81531005\n" +
                "ICD9CM,272.0\tSNOMEDCT_US,267432004\n" +
                "ICD9CM,276.51\tSNOMEDCT_US,34095006\n" +
                "ICD9CM,285.9\tSNOMEDCT_US,271737000\n" +
                "ICD9CM,401.1\tSNOMEDCT_US,1201005\n" +
                "ICD9CM,401.9\tSNOMEDCT_US,59621000\n" +
                "ICD9CM,414.01\tSNOMEDCT_US,53741008\n" +
                "ICD9CM,427.31\tSNOMEDCT_US,49436004\n" +
                "ICD9CM,428.0\tSNOMEDCT_US,42343007\n" +
                "ICD9CM,486\tSNOMEDCT_US,233604007\n" +
                "ICD9CM,491.21\tSNOMEDCT_US,195951007\n" +
                "ICD9CM,507.0\tSNOMEDCT_US,196032009\n" +
                "ICD9CM,518.81\tSNOMEDCT_US,65710008\n" +
                "ICD9CM,584.9\tSNOMEDCT_US,14669001\n" +
                "ICD9CM,585.6\tSNOMEDCT_US,46177005\n" +
                "ICD9CM,599.0\tSNOMEDCT_US,68566005\n" +
                "ICD9CM,724.2\tSNOMEDCT_US,279039007\n" +
                "ICD9CM,780.2\tSNOMEDCT_US,309585006\n" +
                "ICD9CM,786.50\tSNOMEDCT_US,29857009\n" +
                "ICD9CM,789.00\tSNOMEDCT_US,21522001\n" +
                "SNOMEDCT_US,105592009\tICD9CM,038.9\n" +
                "SNOMEDCT_US,11530004\tICD9CM,250.00\n" +
                "SNOMEDCT_US,1201005\tICD9CM,401.1\n" +
                "SNOMEDCT_US,14669001\tICD9CM,584.9\n" +
                "SNOMEDCT_US,195951007\tICD9CM,491.21\n" +
                "SNOMEDCT_US,196032009\tICD9CM,507.0\n" +
                "SNOMEDCT_US,199230006\tICD9CM,250.00\n" +
                "SNOMEDCT_US,21522001\tICD9CM,789.00\n" +
                "SNOMEDCT_US,233604007\tICD9CM,486\n" +
                "SNOMEDCT_US,237599002\tICD9CM,250.00\n" +
                "SNOMEDCT_US,267432004\tICD9CM,272.0\n" +
                "SNOMEDCT_US,271737000\tICD9CM,285.9\n" +
                "SNOMEDCT_US,279039007\tICD9CM,724.2\n" +
                "SNOMEDCT_US,29857009\tICD9CM,786.50\n" +
                "SNOMEDCT_US,309585006\tICD9CM,780.2\n" +
                "SNOMEDCT_US,313436004\tICD9CM,250.00\n" +
                "SNOMEDCT_US,34095006\tICD9CM,276.51\n" +
                "SNOMEDCT_US,399068003\tICD9CM,185\n" +
                "SNOMEDCT_US,40930008\tICD9CM,244.9\n" +
                "SNOMEDCT_US,42343007\tICD9CM,428.0\n" +
                "SNOMEDCT_US,44054006\tICD9CM,250.00\n" +
                "SNOMEDCT_US,46177005\tICD9CM,585.6\n" +
                "SNOMEDCT_US,49436004\tICD9CM,427.31\n" +
                "SNOMEDCT_US,53741008\tICD9CM,414.01\n" +
                "SNOMEDCT_US,59621000\tICD9CM,401.9\n" +
                "SNOMEDCT_US,65710008\tICD9CM,518.81\n" +
                "SNOMEDCT_US,68566005\tICD9CM,599.0\n" +
                "SNOMEDCT_US,73211009\tICD9CM,250.00\n" +
                "SNOMEDCT_US,81531005\tICD9CM,250.00\n");
        InputStream inputStream = new ByteArrayInputStream(resourceFileAsWritten.getBytes());
        decorator = new Icd9SnomedCodeTranslationNodeDecorator(inputStream);
        OutputStream newOutputStream = new ByteArrayOutputStream();
        decorator.writeResourceFile(newOutputStream);
        assertEquals(newOutputStream.toString(), resourceFileAsWritten);
    }
}
