# Computed Equivalence Set Graphs

A number of Computed Equivalence Set Graphs are available for download. 
These ESGs are provided in RDF format (NTriples serialization) and triplified following the [Edwin Ontology](https://w3id.org/edwin/ontology/).


* The Equivalence Set Graph for properties in LOD-a-lot [download](http://etna.istc.cnr.it/edwin/datasets/esg_properties.tar.gz). For computing this graph Edwin has been configured as follows:

```
equivalencePropertyToObserve=http://www.w3.org/2002/07/owl#equivalentProperty
notEquivalenceProperties=http://www.w3.org/1999/02/22-rdf-syntax-ns#type,http://www.w3.org/2000/01/rdf-schema#subClassOf,http://www.w3.org/2002/07/owl#equivalentClass
equivalencePropertyForProperties=http://www.w3.org/2002/07/owl#equivalentProperty

specializationPropertyToObserve=http://www.w3.org/2000/01/rdf-schema#subPropertyOf
specializationPropertyForProperties=http://www.w3.org/2000/01/rdf-schema#subPropertyOf
notSpecializationProperties=http://www.w3.org/1999/02/22-rdf-syntax-ns#type,http://www.w3.org/2000/01/rdf-schema#subClassOf,http://www.w3.org/2002/07/owl#equivalentClass

observedEntitiesSelector=it.cnr.istc.stlab.edwin.PropertiesSelector
extensionalSizeEstimator=it.cnr.istc.stlab.edwin.PropertySizeEstimator
```

* The Equivalence Set Graph for classes in LOD-a-lot [download](http://etna.istc.cnr.it/edwin/datasets/esg_classes.tar.gz). For computing this graph Edwin has been configured as follows:

```
equivalencePropertyToObserve=http://www.w3.org/2002/07/owl#equivalentClass
equivalencePropertyForProperties=http://www.w3.org/2002/07/owl#equivalentProperty

specializationPropertyToObserve=http://www.w3.org/2000/01/rdf-schema#subClassOf
specializationPropertyForProperties=http://www.w3.org/2000/01/rdf-schema#subPropertyOf
notSpecializationProperties=http://www.w3.org/1999/02/22-rdf-syntax-ns#type

observedEntitiesSelector=it.cnr.istc.stlab.edwin.ClassSelector
extensionalSizeEstimator=it.cnr.istc.stlab.edwin.ClassSizeEstimator

```