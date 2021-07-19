# Edwin

A framework for building and analysing Equivalence Set Graphs.


#### Installation

Edwin can be installed using maven or docker.

#### Maven

```
$ git clone https://github.com/luigi-asprino/edwin.git
$ cd edwin
$ mvn clean install
```
Once installed, you can add Edwin as a dependency of your maven project.
```
<dependency>
  <groupId>it.cnr.istc.stlab</groupId>
  <artifactId>edwin</artifactId>
  <version>0.0.1</version>
</dependency>
```
### Docker

* todo

#### Usage

You can compute an Equivalence Set Graph from Java as follows.

```
Edwin.computeESG("/path/to/configuration/file");
```

#### Configuration file

```
# MANDATORY: Path to RDF-HDT file to analyse
hdtFilePath=/Volumes/L2TB/LOD_a_lot/LOD_a_lot_v1.hdt

# MANDATORY: URI of the equivalence property to observe (cf. [1])
equivalencePropertyToObserve=http://www.w3.org/2002/07/owl#equivalentProperty

# MANDATORY: URI of the equivalence property to use for retrieving equivalence relations among properties  (cf. [1])
equivalencePropertyForProperties=http://www.w3.org/2002/07/owl#equivalentProperty

# OPTIONAL: A list of properties to not be considered as equivalence properties (cf. [1])
notEquivalenceProperties=http://www.w3.org/1999/02/22-rdf-syntax-ns#type,http://www.w3.org/2000/01/rdf-schema#subClassOf,http://www.w3.org/2002/07/owl#equivalentClass

# MANDATORY: URI of the specialization property to observe (cf. [1])
specializationPropertyToObserve=http://www.w3.org/2000/01/rdf-schema#subPropertyOf

# MANDATORY: URI of the specialization property to use for retrieving specialization relations among properties (cf. [1])
specializationPropertyForProperties=http://www.w3.org/2000/01/rdf-schema#subPropertyOf

# OPTIONAL: A list of properties to not be considered as specialization properties (cf. [1])
notSpecializationProperties=http://www.w3.org/1999/02/22-rdf-syntax-ns#type,http://www.w3.org/2000/01/rdf-schema#subClassOf,http://www.w3.org/2002/07/owl#equivalentClass

# OPTIONAL: A class that implements a set of criteria for selecting entities to observe
observedEntitiesSelector=it.cnr.istc.stlab.edwin.PropertiesSelector

# OPTIONAL: A class that implements a set of criteria for estimating the extensional size of the observed entities
extensionalSizeEstimator=it.cnr.istc.stlab.edwin.PropertySizeEstimator

# OPTIONAL: A precomputed ESG for properties and for classes (these ESGs might be useful for methods for selecting observed entities and estimate their extensional size)
esgPropertiesFolder=/path/to/ESG/for/properties
esgClassesFolder=/path/to/ESG/for/classes

# MANDATORY: The path where the ESG will be stored
esgFolder=/path/to/esgFolder
```

#### Output

In esgFolder you can find:

1. The computed ESG is stored in a number of key-value maps and can be loaded  from Java as follows:
```
EquivalenceSetGraph esg = EquivalenceSetGraphLoader.loadEquivalenceSetGraphFromFolder("/path/to/esgFolder");
```
2. A TSV file (stats.tsv) that summarizes a set of statistics computed on the ESG.
3. Two TSV files (nodelist.tsv and edgelist.tsv) that allow to analyse the structural features of the graph using [SNAP](http://snap.stanford.edu/snappy/index.html). An analysis of the connected components of the ESG is performed by the script ``src/main/python/compute_graph_stats.py``. This script takes as input the folder where the nodelist.tsv and edgelist.tsv are stored (which is /path/to/esgFolder by default) and the path where to store the connected components of the graph.
4. The triplification of the ESG following the [Edwin Ontology](https://w3id.org/edwin/ontology/) (cf. [2]).


### Computed Equivalence Set Graphs

A number of Computed Equivalence Set Graphs are available for download from this [page](ComputedESGs.md). 

### Publications

For citing Edwin in academic papers please use:

[1] Luigi Asprino, Wouter Beek, Paolo Ciancarini, Frank van Harmelen and Valentina Presutti. Observing LOD using Equivalent Set Graphs: it is mostly flat and sparsely linked. In: Proceedings of the 18th International Semantic Web Conference (ISWC 2019). (to appear). [Preprint](http://arxiv.org/abs/1906.08097)

[2] Luigi Asprino, Wouter Beek, Paolo Ciancarini, Frank van Harmelen and Valentina Presutti. Triplifying Equivalence Set Graphs. In: Proceedings of the ISWC 2019 Posters & Demonstrations, Industry, and Outrageous Ideas Tracks co-located with 18th International Semantic Web Conference (ISWC 2019) 

# License 

[Apache 2.0](https://github.com/luigi-asprino/edwin/blob/master/LICENSE)

#### This framework is named in honor of the American astronomer [Edwin Powell Hubble](https://en.wikipedia.org/wiki/Edwin_Hubble)
