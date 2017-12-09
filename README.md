# umls-to-graph
Code for manipulating the structure of UMLS files and converting ontologies or sets of ontologies into a graph structure.

## What is UMLS? And why a graph?

The Unified Medical Language System (UMLS) is a set of ontologies and associated resource files, lexicons, etc. that together provide a comprehensive set of structured concepts, codes, and text descriptors for the biomedical domain. UMLS is widely used in biomedical informatics research and in a variety of clinical software products, but the raw UMLS files are a bit unwieldy. If you want to do more than simply load UMLS into a database and search for a given concept, code, etc. its structure can be difficult to understand and manipulate.

My own work has focused on natural language processing (NLP) and I've often encountered situations where I want to find all of the synonyms for a given clinical concept, or I want to know hierarchical relationships for a given term (Ex. "Atherosclerosis is a type of cardiovascular disease."). In many of these cases, UMLS can help, but it often doesn't seem worth the effort to parse all of the raw UMLS files.

Even though it wasn't designed to be used this way, I've found that it often helps to think of UMLS as a graph. Different ontologies can be combined in a flexible way to produce graph structures that represent different subsets of UMLS, and the nodes of the graph (concept unique identifiers, or CUIs) can be decorated with synonyms, billing codes, or whatever you want. So I've written some basic code that converts the raw UMLS files into a graph structure, the details of which can be manipulated using a single configuration file.

## Download and Install UMLS

The first thing you need to do is download UMLS. The full release is available [here](https://www.nlm.nih.gov/research/umls/licensedcontent/umlsknowledgesources.html) but you need to apply for a license before you can download it.

Once you've downloaded it, follow the instructions in the README to install it. You need to use the MetamorphoSys app (included in the download) to install the raw resource files.

A couple of notes:
- On the "Select Default Subset Configuration" screen, I always choose "Select all non-level 0 sources except SNOMED_CT US". SNOMED contains a ton of useful terms and hierarchies.
- Once the resource files are installed, there will be many more than you need for the graph. You can delete pretty much everything except: MRCONSO.RRF (concept names and sources), MRHIER.RRF (hierarchies), MRREL.RRF (related concepts), MRSAT.RRF (simple concept and atom attributes) and MRSTY.RRF (semantic types). You can find complete descriptions of all of the UMLS files [here](https://www.ncbi.nlm.nih.gov/books/NBK9685/#_ch03_sec3_3_).

## The Graph Structure

Graphs have nodes and edges. The UMLS graph will have nodes that correspond to CUIs in UMLS and edges that correspond to hierarchical relationships from the various ontologies within UMLS.

The graph lives in two files: a "structure" file and a "decorations" file. The structure file contains the edges and the decorations file contains the nodes, along with a bunch of metadata about them.

The specifics of both files are up to you.  

## Subgraphs (Edges)

The edges of the UMLS graph come from the hierarchical relationships within UMLS. The relationships from each ontology constitute a *subgraph*. Multiple subgraphs are combined to create the complete graph. You can choose to include relationships from as many ontologies as you want.

The ontologies that have hierarchical relationships represented in MRHIER.RRF are (as of the 2017 release):

* [AIR](https://www.nlm.nih.gov/research/umls/sourcereleasedocs/current/AIR/) (1512 hierarchical relationships in MRHIER.RRF)
* [AOD](https://www.nlm.nih.gov/research/umls/sourcereleasedocs/current/AOD/) (14284)
* [AOT](https://www.nlm.nih.gov/research/umls/sourcereleasedocs/current/AOT/) (350)
* [ATC](https://www.nlm.nih.gov/research/umls/sourcereleasedocs/current/ATC) (6083)
* [CCS](https://www.nlm.nih.gov/research/umls/sourcereleasedocs/current/CCS) (1099)
* [CCS_10](https://www.nlm.nih.gov/research/umls/sourcereleasedocs/current/CCS_10) (372)
* [CSP](https://www.nlm.nih.gov/research/umls/sourcereleasedocs/current/CSP)  (14582)
* [CST](https://www.nlm.nih.gov/research/umls/sourcereleasedocs/current/CST)
(3331)
* [FMA](https://www.nlm.nih.gov/research/umls/sourcereleasedocs/current/FMA)
(104084)
* [GO](https://www.nlm.nih.gov/research/umls/sourcereleasedocs/current/GO)
(809207)
* [HL7V2.5](https://www.nlm.nih.gov/research/umls/sourcereleasedocs/current/HL7V25) (5034)
* [HL7V3.0](https://www.nlm.nih.gov/research/umls/sourcereleasedocs/current/HL7V30) (10622)
* [HPO](https://www.nlm.nih.gov/research/umls/sourcereleasedocs/current/HPO/) (81558)
* [ICD10PCS](https://www.nlm.nih.gov/research/umls/sourcereleasedocs/current/ICD10PCS/) (190176)
* [ICD9CM](https://www.nlm.nih.gov/research/umls/sourcereleasedocs/current/ICD9CM) (22407)
* [ICPC](https://www.nlm.nih.gov/research/umls/sourcereleasedocs/current/ICPC) (1433)
* [LNC](https://www.nlm.nih.gov/research/umls/sourcereleasedocs/current/LNC/) (270837)
* [MEDLINEPLUS](https://www.nlm.nih.gov/research/umls/sourcereleasedocs/current/MEDLINEPLUS/) (1812)
* [MSH](https://www.nlm.nih.gov/research/umls/sourcereleasedocs/current/MSH/) (58859)
* [MTHHH](https://www.nlm.nih.gov/research/umls/sourcereleasedocs/current/MTHHH/) (6937)
* [NCBI](https://www.nlm.nih.gov/research/umls/sourcereleasedocs/current/NCBI/) (1285985)
* [NCI](https://www.nlm.nih.gov/research/umls/sourcereleasedocs/current/NCI/) (312695)
* [NDFRT](https://www.nlm.nih.gov/research/umls/sourcereleasedocs/current/NDFRT/) (45364)
* [OMIM](https://www.nlm.nih.gov/research/umls/sourcereleasedocs/current/OMIM) (51769)
* [PDQ](https://www.nlm.nih.gov/research/umls/sourcereleasedocs/current/PDQ/) (3816)
* [SNOMEDCT_US](https://www.nlm.nih.gov/research/umls/sourcereleasedocs/current/SNOMEDCT_US) (9561078)
* [SOP](https://www.nlm.nih.gov/research/umls/sourcereleasedocs/current/SOP) (156)
* [TKMT](https://www.nlm.nih.gov/research/umls/sourcereleasedocs/current/TKMT) (372)
* [USPMG](https://www.nlm.nih.gov/research/umls/sourcereleasedocs/current/USPMG) (1910)
* [UWDA](https://www.nlm.nih.gov/research/umls/sourcereleasedocs/current/UWDA) (419453)

The ontologies I typically use in my graph are: ATC, FMA, GO, HPO, ICD10PCS, ICD9CM, LNC, MSH, MTHHH, NCBI, NCI, NDFRT, OMIM, and SNOMEDCT_US.

You need to build a resource file for each subgraph before the final graph can be created. This is really useful for debugging later, since you can see where all of the edges in the final graph come from.

### MRHIER Subgraphs

For each ontology in MRHIER.RRF for which you want to create a subgraph, run the following:

` java subgraphs.MrHierSubgraph <umls-location>/MRHIER.RRF <umls-location>/MRCONSO.RRF <output-subgraph-file> <ontology-name> `

For example:

`java subgraphs.MrHierSubgraph /Users/beth/Documents/data/2017AB-full/2017AB/META/MRHIER.RRF /Users/beth/Documents/data/2017AB-full/2017AB/META/MRCONSO.RRF /Users/beth/Desktop/subgraphs/umls-subgraph-omim.txt OMIM`

Ensure that the string you use to reference the ontology (argument 4, above) matches one of the recognized ontology types listed above. Otherwise no output will be produced.

The output format is a two-column tab-delimited file; the first column is a parent CUI and the second column is a comma-separated list of child CUIs. Note that instead of being stored as strings (`"C4228946"`) the CUIs are stored as integers to save space. So the CUI listed as `543` in the subgraph files corresponds to the CUI listed as `C0000543` in the UMLS files.

### Drug-Ingredient Subgraph

Many different branded drugs correspond to the same active ingredient. It is therefore useful to create a second type of subgraph that maps individual drug preparations ("children") to their active ingredients ("parents"). This is totally optional and distinct from the MRHIER.RRF subgraphs.

To get the drug-ingredient relationships, you need MRREL.RRF and MRCONSO.RRF. To create this subgraph, run:

` java subgraphs.DrugIngredientSubgraph <umls-location>/MRREL.RRF <umls-location>/MRCONSO.RRF <output-subgraph-file> `

For example:

` /Users/beth/Documents/data/2017AB-full/2017AB/META/MRREL.RRF /Users/beth/Documents/data/2017AB-full/2017AB/META/MRCONSO.RRF /Users/beth/Desktop/subgraphs/umls-subgraph-drug-ingredient.txt `

## Decorators (Nodes)

The nodes of the subgraphs are all CUIs. CUIs are not very useful on their own for most applications. Normally we'll want to start with a string descriptor of a concept ("diabetes") or a billing code (such as from NDC or ICD9), map it to a CUI, and use the graph to find parent or child CUIs (and their associated strings and codes). To decorate the nodes of the subgraphs with all of this other information, we use an object called a *decorator*. 

The metadata that decorators use to decorate the nodes also lives in resource files. You'll need to create one resource file for each type of decoration you wish to include.

### String Annotation Decorators

For most NLP applications, string annotations for UMLS concepts will be the most important thing that comes out of this graph. Each CUI is attached to a set of descriptors from various ontologies. The SPECIALIST lexicon (which comes with UMLS) also provides a list of alternate spellings for various terms in the LRSPL file. The string annotation decorator finds all strings from recognized ontologies (everything represented in the OntologyType enum) and the alternate spellings from LRSPL, and attaches them to nodes in the graph. To generate a resource file for this code decorator, run the following:

``` java nodedecorators.StringsNodeDecorator <umls-location>/MRCONSO.RRF <umls-location>/LRSPL <output-resource-file> ```

### Code Annotation Decorators

These decorators map ontology-specific codes to CUIs. Some ontologies whose codes are frequently used in medical practice and billing include: ICD9CM, MTHICD9, ICD10PCS, SNOMEDCT_US, and LOINC (LNC). To generate a resource file for a code decorator, do the following:

` java nodedecorators.GenericCodeNodeDecorator <umls-location>/MRCONSO.RRF <ontology-type> <output-resource-file> `

For example:

` java nodedecorators.GenericCodeNodeDecorator /Users/beth/Documents/data/2017AB-full/2017AB/META/MRCONSO.RRF ICD9CM /Users/beth/Desktop/subgraphs/umls-decorator-icd9cm.txt `

will generate a resource file for ICD9 codes.

NDC codes are not accessible from MRCONSO.RRF; the mapping needs to be created separately using MRSAT.RRF. To generate a resource file for NDC codes, run:

` java nodedecorators.NdcCodeNodeDecorator <umls-location>/MRSAT.RRF <output-decorator-map-file> `

Code decorators are a work in progress and I'd encourage people to reach out if they need a way to include other code types in the graph.

### Code Translation Decorators

There have been various attempts to map ICD9 codes to ICD10 codes and ICD9 codes to SNOMED codes, mostly to facilitate changes in medical billing and electronic medical record practices. NLM has provided a variety of mapping files that can translate between codes. I've created an object called a `CodeTranslationNodeDecorator` to handle these mappings. The decorator looks at the current set of codes attached to a node and then adds codes from the other ontologies. So for example, if a node is decorated with ICD9 codes, the mappings will add codes for ICD10 or SNOMED. The reverse is also possible.

There are currently two types of code translation node decorators.

1. _ICD9 <-> ICD10 translation_
Download the general equivalence mapping (GEM) files from [here](https://www.cms.gov/Medicare/Coding/ICD10/Downloads/2017-GEM-DC.zip). When you unzip the archive, you'll see two files called `2017_I9gem.txt` and `2017_I10gem.txt`. Create the resource file using:
` java nodedecorators.Icd9Icd10CodeTranslationNodeDecorator <path-to-gems>/2017_I9gem.txt <path-to-gems>/2017_I10gem.txt <output-resource-file>`.

2. _ICD9 <-> SNOMED translation_
You need to use your UMLS license to download the ICD9-to-SNOMED maps from NLM. You can get them [here](https://download.nlm.nih.gov/umls/kss/mappings/ICD9CM_TO_SNOMEDCT/ICD9CM_SNOMEDCT_map_201612.zip). When you unzip the archive, you'll see two files called `ICD9CM_SNOMED_MAP_1TO1_201612.txt` and `ICD9CM_SNOMED_MAP_1TOM_201612.txt`. Create the resource file using: ` java nodedecorators.Icd9SnomedCodeTranslationNodeDecorator <path-to-map-files>/ICD9CM_SNOMED_MAP_1TO1_201612.txt <path-to-map-files>/ICD9CM_SNOMED_MAP_1TOM_201612.txt <output-resource-file>`. 

### Ontology Node Decorator

Not all CUIs are represented in all ontologies. If you want to know which ontologies each CUI belongs to, you can create a resource file that will add a list of ontologies to the CUIs in the graph decorations file. Just do:

` java nodedecorators.OntologyNodeDecorator <umls-location>/MRCONSO.RRF <output-resource-file> `

### Semantic Group and Semantic Type Node Decorators

UMLS provides semantic group and semantic type annotations for all CUIs. The definitions of these types and groups can be found [here](https://metamap.nlm.nih.gov/SemanticTypesAndGroups.shtml). These are particularly useful in cases where you only want a graph that includes certain categories, like disorders, chemicals, etc.

Create the resource files for semantic types and groups by doing the following:

` java nodedecorators.SemanticGroupNodeDecorator <umls-location>/MRSTY.RRF <output-resource-file> ` 

` java nodedecorators.SemanticTypeNodeDecorator <umls-location>/MRSTY.RRF <output-resource-file> `

## Node Filters and Node Modifiers

Sometimes we want to remove nodes from the graph based on certain properties. For example, maybe we want our graph to include only chemicals (semantic group: CHEM). The code accomplishes this using objects called NodeFilters and NodeModifiers. You don't need to generate resource files for these - just include the following line in your graph config file to restrict by semantic group:

``` nodefilter	SEMGROUP	<allowed types> ```

where `<allowed-types>` is a pipe-delimited list of allowed types; for example, `CHEM|DISO`. The code will automatically remove nodes that have only those types. For nodes with multiple types, only the allowed types will be retained in the final graph.

To restrict by semantic type, you do the opposite:

``` nodefilter	SEMTYPE	<disallowed-types> ```

You list the disallowed types, since there are so many semantic types and more often than not, you simply wish to remove one or two types from the graph. Disallowed types will also be removed from the nodes that have multiple types.

The final type of node modifier adds modified strings. This is a bit of a hack, but in practice I've noticed that ontology terms will frequently have the form `diabetes, type II` when what appears most often in real text is `type II diabetes`. So this modifier switches those around and also adds and subtracts apostrophes from terms like `Alzheimer's Disease`. LRSPL takes care of some of these, but this node modifier basically acts as a check, making sure that these common variants are represented. You don't have to include it.

To include the modified strings, add the following line to your config file:

``` nodemodifier	MODIFIEDSTRINGS ```

and the modified strings will automatically be added to the list of string descriptions for each node.

If you need/want another type of node filter or modifier, please reach out.

## Edge Filters

You can also filter out edges in the graph using an object called an EdgeFilter. The edge filter looks at the nodes on either side of an edge and removes the edge if the nodes don't fulfill certain properties. For example, occasionally you can end up with a situation where a `CHEM` node is a parent of a `DISO` or something like that. This can lead to weird properties in the final graph, where you have a drug being the parent of a disease, etc. just because of a quirk that occurred when two ontologies were merged. 

Right now the only type of edge filter that exists removes edges where the connected nodes have different semantic types. To include the filter, add the following line to your config file:

```edgefilter	SEMGROUPMISMATCH```

Again, if you can think of another type of edge filter that would be useful, please reach out.

## Removing Cycles

Occasionally two ontologies will contradict each other (one will have CUI A as the parent of CUI B and the other will have B as the parent of A). To ensure the final graph is a DAG, the code will automatically collapse these cycles and create a new "meta-node" that includes both A and B. All of the CUIs in the cycle (or cycles) that get merged will end up in the same node. These are called "sibling CUIs". If you want to avoid this, you'll need to figure out which ontology or ontologies is causing the cycle and remove those edges. 

## Creating the Graph

Graph construction proceeds in two stages. In the first stage, the code constructs a preliminary graph that includes all of the subgraphs and decorations you've decided on and built resource files for in the previous steps. Then the code applies all of the node filters, node modifiers, and edge filters to produce a final graph. 

You construct the graph by creating a configuration file (mine is called `graph-config.txt`) that looks something like this:

``` 
 # subgraphs
 subgraph	ATC	/Users/beth/Desktop/subgraphs/umls-subgraph-atc.txt
 subgraph	RXNORM	/Users/beth/Desktop/subgraphs/umls-subgraph-drug-ingredient.txt
 subgraph	FMA	/Users/beth/Desktop/subgraphs/umls-subgraph-fma.txt
 subgraph	GO	/Users/beth/Desktop/subgraphs/umls-subgraph-go.txt
 subgraph	HPO	/Users/beth/Desktop/subgraphs/umls-subgraph-hpo.txt
 subgraph	ICD10PCS	/Users/beth/Desktop/subgraphs/umls-subgraph-icd10pcs.txt
 subgraph	ICD9CM	/Users/beth/Desktop/subgraphs/umls-subgraph-icd9cm.txt
 subgraph	LNC	/Users/beth/Desktop/subgraphs/umls-subgraph-lnc.txt
 subgraph	MSH	/Users/beth/Desktop/subgraphs/umls-subgraph-msh.txt
 subgraph	MTHHH	/Users/beth/Desktop/subgraphs/umls-subgraph-mthhh.txt
 subgraph	NCBI	/Users/beth/Desktop/subgraphs/umls-subgraph-ncbi.txt
 subgraph	NCI	/Users/beth/Desktop/subgraphs/umls-subgraph-nci.txt
 subgraph	NDFRT	/Users/beth/Desktop/subgraphs/umls-subgraph-ndfrt.txt
 subgraph	OMIM	/Users/beth/Desktop/subgraphs/umls-subgraph-omim.txt
 subgraph	SNOMEDCT_US	/Users/beth/Desktop/subgraphs/umls-subgraph-snomed-ct-us.txt
 
 # code decorators
 codedecorator	ICD9CM	/Users/beth/Desktop/subgraphs/umls-decorator-icd9cm.txt
 codedecorator	MTHICD9	/Users/beth/Desktop/subgraphs/umls-decorator-mthicd9.txt
 codedecorator	ICD10PCS	/Users/beth/Desktop/subgraphs/umls-decorator-icd10pcs.txt
 codedecorator	LNC	/Users/beth/Desktop/subgraphs/umls-decorator-lnc.txt
 codedecorator	SNOMEDCT_US	/Users/beth/Desktop/subgraphs/umls-decorator-snomedct-us.txt
 codedecorator	NDC	/Users/beth/Desktop/subgraphs/umls-decorator-ndc.txt
 
 # code translation decorators
 translation	ICD9CM	ICD10PCS	/Users/beth/Desktop/subgraphs/umls-decorator-icd9-icd10.txt
 translation	ICD9CM	SNOMEDCT_US	/Users/beth/Desktop/subgraphs/umls-decorator-icd9-snomed.txt
 
 # other decorators
 decorator	ONTOLOGY	/Users/beth/Desktop/subgraphs/umls-decorator-ontology.txt
 decorator	SEMGROUP	/Users/beth/Desktop/subgraphs/umls-decorator-semantic-group.txt
 decorator	SEMTYPE	/Users/beth/Desktop/subgraphs/umls-decorator-semantic-type.txt
 
 # edge filters
 edgefilter	SEMGROUPMISMATCH
 
 # node filters
 nodefilter	SEMGROUP	ACTI|ANAT|CHEM|CONC|DEVI|DISO|GENE|GEOG|LIVB|OBJC|OCCU|ORGA|PHEN|PHYS|PROC
 nodefilter	SEMTYPE	T121
 
 # node modifiers
 nodemodifier	MODIFIEDSTRINGS
```

You can leave out any subgraphs or decorators you want, or add additional subgraphs for other ontologies.

Then you build the graph by running `java build.CreateUMLSGraph <graph-config-file> <output-structure-file> <output-decorations-file>`. 

## Format of the Graph Files

Both of the graph output files are tab-delimited.

The structure file has two columns:

* parent CUI
* child CUI

The decorations file has the following columns:

* CUI
* sibling CUIs (if any; comma-delimited)
* string descriptions (pipe-delimited)
* codes (pipe-delimited, with sources)
* semantic type(s) (pipe-delimited)
* semantic group(s) (pipe-delimited)
* ontologies (pipe-delimited)

## Some Final Notes

This is just some utility code that I've found to be useful over the years - it is not production-grade and there are bound to be errors. Please let me know if you find any. My plan is to continually refine the project in the coming months. If you're part of the biomedical research community and have any advice or want to contribute, I'd love to hear from you. I can be reached at bethany.percha@mssm.edu.

Please note that this code is released under the [GPL](https://www.gnu.org/licenses/gpl-3.0.en.html). 
