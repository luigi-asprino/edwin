# Edwin

A framework for building and analysing Equivalence Set Graphs.

#### Dependecies

Before running Edwin, make sure that [RocksMap](https://github.com/luigi-asprino/rocks-map) is correctly installed on your machine.

#### Installation

Edwin can be installed using maven.

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

#### Usage

You can compute an Equivalence Set Graph from Java as follows.

```
Edwin.computeESG("/path/to/configuration/file");
```

#### Configuration file

```
# MANDATORY: Path to RDF-HDT file to analyse
hdtFilePath=/Volumes/L2TB/LOD_a_lot/LOD_a_lot_v1.hdt

# MANDATORY: URI of the equivalence property to observe
equivalencePropertyToObserve=http://www.w3.org/2002/07/owl#equivalentProperty

# MANDATORY: URI of the equivalence property to use for retrieving equivalence relations among properties
equivalencePropertyForProperties=http://www.w3.org/2002/07/owl#equivalentProperty

# OPTIONAL: A list of properties to not be considered as equivalence properties
notEquivalenceProperties=http://www.w3.org/1999/02/22-rdf-syntax-ns#type,http://www.w3.org/2000/01/rdf-schema#subClassOf,http://www.w3.org/2002/07/owl#equivalentClass

# MANDATORY: URI of the specialization property to observe
specializationPropertyToObserve=http://www.w3.org/2000/01/rdf-schema#subPropertyOf

# MANDATORY: URI of the specialization property to use for retrieving specialization relations among properties
specializationPropertyForProperties=http://www.w3.org/2000/01/rdf-schema#subPropertyOf

# OPTIONAL: A list of properties to not be considered as specialization properties
notSpecializationProperties=http://www.w3.org/1999/02/22-rdf-syntax-ns#type,http://www.w3.org/2000/01/rdf-schema#subClassOf,http://www.w3.org/2002/07/owl#equivalentClass

# OPTIONAL: A class that implements a set of criteria for selecting entities to observe
observedEntitiesSelector=it.cnr.istc.stlab.edwin.PropertiesSelector

# OPTIONAL: A class that implements a set of criteria for estimating the extensional size of the observed entities
extensionalSizeEstimator=it.cnr.istc.stlab.edwin.PropertySizeEstimator

# OPTIONAL: A precomputed ESG for properties and for classes (these ESGs might be useful for methods for selecting observed entities and estimatica extensional size)
esgPropertiesFolder=/path/to/ESG/for/properties
esgClassesFolder=/path/to/ESG/for/classes

# MANDATORY: The path where the ESG will be stored
esgFolder=/Users/lgu/Desktop/ESGs/properties_1
```

### Publications

For citing Edwin in academic papers please use:

* Luigi Asprino, Wouter Beek, Paolo Ciancarini, Frank van Harmelen and Valentina Presutti. Observing LOD using Equivalent Set Graphs: it is mostly flat and sparsely linked. In: Proceedings of the 18th International Semantic Web Conference (ISWC 2019). (to appear). [Preprint](http://arxiv.org/abs/1906.08097)

* Luigi Asprino, Wouter Beek, Paolo Ciancarini, Frank van Harmelen and Valentina Presutti. Triplifying Equivalence Set Graphs. In: Proceedings of the ISWC 2019 Posters & Demonstrations, Industry, and Outrageous Ideas Tracks co-located with 18th International Semantic Web Conference (ISWC 2019) 


#### This framework is named in honor of the American astronomer [Edwin Powell Hubble](https://en.wikipedia.org/wiki/Edwin_Hubble)
