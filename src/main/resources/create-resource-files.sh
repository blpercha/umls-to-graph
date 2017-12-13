#!/usr/bin/env bash
GRAPHINFODIRECTORY=/Users/beth/Desktop/graph-info
GRAPHJARLOCATION=/Users/beth/Documents/code/umls-to-graph/target/umls-to-graph-1.0-SNAPSHOT-jar-with-dependencies.jar
UMLSLOCATION=/Users/beth/Documents/phd/data/umls/2017AB-full/2017AB/META

declare -a MRHIER_ONTOLOGIES=('AIR' 'AOD' 'AOT' 'ATC' 'CCS' 'CCS_10' 'CPT' 'CSP' 'CST' 'FMA' 'GO' 'HPO' 'ICD10' 'ICD10CM' 'ICD10PCS' 'ICD9CM' 'ICPC' 'LNC' 'MEDLINEPLUS' 'MSH' 'MTHHH' 'NCBI' 'NCI' 'NDFRT' 'OMIM' 'PDQ' 'SNOMEDCT_US' 'SOP' 'TKMT' 'USPMG' 'UWDA');

for ONTOLOGYNAME in "${MRHIER_ONTOLOGIES[@]}"
do
	echo -e "subgraph\t${ONTOLOGYNAME}\t${GRAPHINFODIRECTORY}/umls-subgraph-${ONTOLOGYNAME}.txt"
	java -mx4g -cp ${GRAPHJARLOCATION} subgraphs.MrHierSubgraph ${UMLSLOCATION}/MRHIER.RRF ${UMLSLOCATION}/MRCONSO.RRF ${GRAPHINFODIRECTORY}/umls-subgraph-${ONTOLOGYNAME}.txt ${ONTOLOGYNAME}
done

echo -e "subgraph\tRXNORM\t${GRAPHINFODIRECTORY}/umls-subgraph-drug-ingredient.txt"
java -mx4g -cp ${GRAPHJARLOCATION} subgraphs.DrugIngredientSubgraph ${UMLSLOCATION}/MRREL.RRF ${UMLSLOCATION}/MRCONSO.RRF ${GRAPHINFODIRECTORY}/umls-subgraph-drug-ingredient.txt

declare -a CODENAMES=('MDR' 'ICD9CM' 'MTHICD9' 'ICD10' 'ICD10CM' 'ICD10PCS' 'LNC' 'SNOMEDCT_US');

for CODENAME in "${CODENAMES[@]}"
do
	echo -e "codedecorator\t${CODENAME}\t${GRAPHINFODIRECTORY}/umls-code-decorator-${CODENAME}.txt"
	java -mx4g -cp ${GRAPHJARLOCATION} nodedecorators.GenericCodeNodeDecorator ${UMLSLOCATION}/MRCONSO.RRF  ${CODENAME} ${GRAPHINFODIRECTORY}/umls-code-decorator-${CODENAME}.txt
done

echo -e "codedecorator\tNDC\t${GRAPHINFODIRECTORY}/umls-code-decorator-NDC.txt"
java -mx4g -cp ${GRAPHJARLOCATION} nodedecorators.NdcCodeNodeDecorator ${UMLSLOCATION}/MRSAT.RRF ${GRAPHINFODIRECTORY}/umls-code-decorator-NDC.txt

echo -e "translation\tICD9CM\tICD10PCS\t${GRAPHINFODIRECTORY}/umls-translation-decorator-icd9-icd10.txt"
java -mx4g -cp ${GRAPHJARLOCATION} nodedecorators.Icd9Icd10CodeTranslationNodeDecorator ${UMLSLOCATION}/2017_I9gem.txt ${UMLSLOCATION}/2017_I10gem.txt ${GRAPHINFODIRECTORY}/umls-translation-decorator-icd9-icd10.txt

echo -e "translation\tICD9CM\tSNOMEDCT_US\t${GRAPHINFODIRECTORY}/umls-translation-decorator-icd9-icd10.txt"
java -mx4g -cp ${GRAPHJARLOCATION} nodedecorators.Icd9SnomedCodeTranslationNodeDecorator ${UMLSLOCATION}/ICD9CM_SNOMED_MAP_1TO1_201612.txt ${UMLSLOCATION}/ICD9CM_SNOMED_MAP_1TOM_201612.txt ${GRAPHINFODIRECTORY}/umls-translation-decorator-icd9-snomed.txt

echo -e "decorator\tONTOLOGY\t${GRAPHINFODIRECTORY}/umls-decorator-ontology.txt"
java -mx4g -cp ${GRAPHJARLOCATION} nodedecorators.OntologyNodeDecorator ${UMLSLOCATION}/MRCONSO.RRF ${GRAPHINFODIRECTORY}/umls-decorator-ontology.txt

echo -e "decorator\tSEMGROUP\t${GRAPHINFODIRECTORY}/umls-decorator-semantic-group.txt"
java -mx4g -cp ${GRAPHJARLOCATION} nodedecorators.SemanticGroupNodeDecorator ${UMLSLOCATION}/MRSTY.RRF ${GRAPHINFODIRECTORY}/umls-decorator-semantic-group.txt

echo -e "decorator\tSEMTYPE\t${GRAPHINFODIRECTORY}/umls-decorator-semantic-type.txt"
java -mx4g -cp ${GRAPHJARLOCATION} nodedecorators.SemanticTypeNodeDecorator ${UMLSLOCATION}/MRSTY.RRF ${GRAPHINFODIRECTORY}/umls-decorator-semantic-type.txt

echo -e "decorator\tSTRINGS\t${GRAPHINFODIRECTORY}/umls-decorator-strings.txt.gz"
java -mx4g -cp ${GRAPHJARLOCATION} nodedecorators.StringsNodeDecorator ${UMLSLOCATION}/MRCONSO.RRF ${UMLSLOCATION}/LRSPL ${GRAPHINFODIRECTORY}/umls-decorator-strings.txt.gz

echo -e "edgefilter\tSEMGROUPMISMATCH"

echo -e "nodemodifier\tMODIFIEDSTRINGS"
