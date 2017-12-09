package utils;

public enum OntologyType {
    /* MRHIER ontologies */
    AIR,
    AOD,
    AOT,
    ATC,
    CCS,
    CCS_10,
    CSP,
    CST,
    FMA,
    GO,
    HL7V2_5,
    HL7V3_0,
    HPO,
    ICD10PCS,
    ICD9CM,
    ICPC,
    LNC,
    MEDLINEPLUS,
    MSH,
    MTHHH,
    NCBI,
    NCI,
    NDFRT,
    OMIM,
    PDQ,
    SNOMEDCT_US,
    SOP,
    TKMT,
    USPMG,
    UWDA,
    /* MRCONSO ontologies (drug-ingredient) */
    RxNorm,
    /* MRCONSO ontologies (decorators only) */
    MTHICD9,
    /* MRSAT ontologies (codes only) */
    NDC,
    OTHER;

    public static OntologyType fromString(String string) {
        switch (string) {
            case "AIR":
                return OntologyType.AIR;
            case "AOD":
                return OntologyType.AOD;
            case "AOT":
                return OntologyType.AOT;
            case "ATC":
                return OntologyType.ATC;
            case "CCS":
                return OntologyType.CCS;
            case "CCS_10":
                return OntologyType.CCS_10;
            case "CSP":
                return OntologyType.CSP;
            case "CST":
                return OntologyType.CST;
            case "FMA":
                return OntologyType.FMA;
            case "GO":
                return OntologyType.GO;
            case "HL7V2.5":
                return OntologyType.HL7V2_5;
            case "HL7V3.0":
                return OntologyType.HL7V3_0;
            case "HPO":
                return OntologyType.HPO;
            case "ICD10PCS":
                return OntologyType.ICD10PCS;
            case "ICD9CM":
                return OntologyType.ICD9CM;
            case "ICPC":
                return OntologyType.ICPC;
            case "LNC":
                return OntologyType.LNC;
            case "MEDLINEPLUS":
                return OntologyType.MEDLINEPLUS;
            case "MSH":
                return OntologyType.MSH;
            case "MTHICD9":
                return OntologyType.MTHICD9;
            case "MTHHH":
                return OntologyType.MTHHH;
            case "NCBI":
                return OntologyType.NCBI;
            case "NCI":
                return OntologyType.NCI;
            case "NDC":
                return OntologyType.NDC;
            case "NDFRT":
                return OntologyType.NDFRT;
            case "OMIM":
                return OntologyType.OMIM;
            case "PDQ":
                return OntologyType.PDQ;
            case "RXNORM":
                return OntologyType.RxNorm;
            case "SNOMEDCT_US":
                return OntologyType.SNOMEDCT_US;
            case "SOP":
                return OntologyType.SOP;
            case "TKMT":
                return OntologyType.TKMT;
            case "USPMG":
                return OntologyType.USPMG;
            case "UWDA":
                return OntologyType.UWDA;
            default:
                return OntologyType.OTHER;
        }
    }
}
