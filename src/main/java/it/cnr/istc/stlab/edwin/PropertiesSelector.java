package it.cnr.istc.stlab.edwin;

import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.compress.compressors.CompressorException;
import org.apache.jena.graph.Triple;
import org.rdfhdt.hdt.dictionary.DictionarySection;
import org.rdfhdt.hdt.exceptions.NotFoundException;
import org.rdfhdt.hdt.hdt.HDT;
import org.rdfhdt.hdt.triples.TripleString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.cnr.istc.stlab.lgu.commons.semanticweb.datasets.Dataset;
import it.cnr.istc.stlab.lgu.commons.semanticweb.streams.StreamRDFUtils;


public class PropertiesSelector implements ObservedEntitiesSelector {

	private static Logger logger = LoggerFactory.getLogger(PropertiesSelector.class);

	public void addSpareEntitiesToEquivalenceSetGraph(RocksDBBackedEquivalenceSetGraph esg_properties, Dataset dataset) {

		logger.info("Adding spare properties");

		// A property is the predicate of a triple
		long id = esg_properties.getMaxId();

		for (HDT hdt : dataset.getHdts()) {
			DictionarySection predicates = hdt.getDictionary().getPredicates();
			long numberOfPredicates = predicates.getNumberOfElements();
			long predicatesProcessed = 0;
			logger.info("Number of predicates found in Dictionary: {}", predicates.size());
			Iterator<? extends CharSequence> it = predicates.getSortedEntries();
			while (it.hasNext()) {
				String predicate = it.next().toString();
				if (predicatesProcessed > 0 && predicatesProcessed % 10000 == 0) {
					logger.info("Number of predicates processed: {}/{}", predicatesProcessed, numberOfPredicates);
				}

				if (!esg_properties.ID.containsKey(predicate)) {
					esg_properties.ID.put(predicate.toString(), ++id);
					esg_properties.IS.put(id, predicate.toString());
				}
				predicatesProcessed++;
			}

			for (String f : dataset.getFiles()) {
				logger.info("Processing file {}", f);
				try {
					Iterator<Triple> itsw = StreamRDFUtils.createIteratorTripleFromFile(f);
					while (it.hasNext()) {
						String predicate = itsw.next().toString();
						if (!esg_properties.ID.containsKey(predicate)) {
							esg_properties.ID.put(predicate.toString(), ++id);
							esg_properties.IS.put(id, predicate.toString());
						}
						predicatesProcessed++;

					}
				} catch (CompressorException | IOException e) {
					e.printStackTrace();
				}
			}

		}

	}

	public void addSpareEntitiesToEquivalentSetGraphUsignESGForClasses(RocksDBBackedEquivalenceSetGraph esg_properties,
			RocksDBBackedEquivalenceSetGraph esg_classes, Dataset dataset) {

		long id = esg_properties.getMaxId();

		// A property is an entity that belongs to rdfs:Property
		Set<String> typePredicates = esg_properties
				.getEntitiesImplicityEquivalentToOrSubsumedBy("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
		Set<String> propertyClasses = new HashSet<>();
		propertyClasses.add("http://www.w3.org/1999/02/22-rdf-syntax-ns#Property");
		if (esg_classes != null) {
			propertyClasses.addAll(
					esg_classes.getEntitiesImplicityEquivalentToOrSubsumedBy("http://www.w3.org/1999/02/22-rdf-syntax-ns#Property"));
		}
		for (String typePredicate : typePredicates) {
			for (String property : propertyClasses) {
				try {
					Iterator<TripleString> its = dataset.search("", typePredicate, property);
					while (its.hasNext()) {
						TripleString ts = its.next();
						if (!esg_properties.ID.containsKey(ts.getSubject().toString())) {
							esg_properties.ID.put(ts.getSubject().toString(), ++id);
							esg_properties.IS.put(id, ts.getSubject().toString());
						}
					}
				} catch (NotFoundException e) {
					e.printStackTrace();
				} catch (CompressorException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}

			}
		}

		// A property is the subject of a triple where the property has
		// rdf:Property as domain.
		// A property is the subject of a triple having rdfs:domain as predicate
		Set<String> domainredicates = esg_properties
				.getEntitiesImplicityEquivalentToOrSubsumedBy("http://www.w3.org/2000/01/rdf-schema#domain");
		for (String domainPredicate : domainredicates) {
			for (String property : propertyClasses) {
				try {
					Iterator<TripleString> its = dataset.search("", domainPredicate, property);
					while (its.hasNext()) {
						TripleString ts = its.next();
						if (!esg_properties.ID.containsKey(ts.getSubject().toString())) {
							esg_properties.ID.put(ts.getSubject().toString(), ++id);
							esg_properties.IS.put(id, ts.getSubject().toString());
						}

						Iterator<TripleString> its2 = dataset.search("", ts.getSubject(), "");
						while (its2.hasNext()) {
							TripleString ts2 = its2.next();
							if (!esg_properties.ID.containsKey(ts2.getSubject().toString())) {
								esg_properties.ID.put(ts2.getSubject().toString(), ++id);
								esg_properties.IS.put(id, ts2.getSubject().toString());
							}
						}
					}
				} catch (NotFoundException e) {
					e.printStackTrace();
				} catch (CompressorException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		// A property is the object of a triple where the property has
		// rdf:Property as range.
		// A property is the subject of a triple having rdfs:range as predicate.
		Set<String> rangePredicates = esg_properties
				.getEntitiesImplicityEquivalentToOrSubsumedBy("http://www.w3.org/2000/01/rdf-schema#range");
		for (String rangePredicate : rangePredicates) {
			for (String property : propertyClasses) {
				try {
					Iterator<TripleString> its = dataset.search("", rangePredicate, property);
					while (its.hasNext()) {
						TripleString ts = its.next();
						if (!esg_properties.ID.containsKey(ts.getSubject().toString())) {
							esg_properties.ID.put(ts.getSubject().toString(), ++id);
							esg_properties.IS.put(id, ts.getSubject().toString());
						}

						Iterator<TripleString> its2 = dataset.search("", ts.getSubject(), "");
						while (its2.hasNext()) {
							TripleString ts2 = its2.next();
							if (!esg_properties.ID.containsKey(ts2.getObject().toString())) {
								esg_properties.ID.put(ts2.getObject().toString(), ++id);
								esg_properties.IS.put(id, ts2.getObject().toString());
							}
						}
					}
				} catch (NotFoundException e) {
					e.printStackTrace();
				} catch (CompressorException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	@Override
	public void addSpareEntitiesToEquivalentSetGraphUsignESGForProperties(RocksDBBackedEquivalenceSetGraph esg,
			RocksDBBackedEquivalenceSetGraph esg_properties, Dataset hdt) {
		// TODO Auto-generated method stub

	}

}
