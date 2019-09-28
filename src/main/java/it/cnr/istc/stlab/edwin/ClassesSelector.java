package it.cnr.istc.stlab.edwin;

import java.util.HashSet;
import java.util.Set;

import org.rdfhdt.hdt.exceptions.NotFoundException;
import org.rdfhdt.hdt.hdt.HDT;
import org.rdfhdt.hdt.triples.IteratorTripleString;
import org.rdfhdt.hdt.triples.TripleString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClassesSelector implements ObservedEntitiesSelector {

	private static Logger logger = LoggerFactory.getLogger(ClassesSelector.class);

	public void addSpareEntitiesToEquivalentSetGraphUsignESGForProperties(EquivalenceSetGraph esg,
			EquivalenceSetGraph esg_properties, HDT hdt) {

		logger.info("Adding spare entities to ESG using ESG for properties.");

		long id = esg_properties.getMaxId();

		// A class is an entity that belongs to rdfs:Class
		logger.info("Applying heuristics: A class is an entity that belongs to rdfs:Class");
		logger.info("Applying heuristics:  A class is the object of a type statement");

		Set<String> typePredicates = esg_properties
				.getEquivalentOrSubsumedEntities("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
		Set<String> classClasses = new HashSet<>();
		classClasses.add("http://www.w3.org/2000/01/rdf-schema#Class");
		if (esg != null) {
			classClasses.addAll(esg.getEquivalentOrSubsumedEntities("http://www.w3.org/2000/01/rdf-schema#Class"));
		}

		logger.info("Number of properties equivalent to or subsumed by rdf:type: {}", typePredicates.size());
		logger.info("Number of classes equivalent to or subsumed by rdfs:Class: {}", classClasses.size());

		int typePredicatesProcessed = 0;
		int classClassesProcessed = 0;

		for (String typePredicate : typePredicates) {
			classClassesProcessed = 0;
			for (String classClass : classClasses) {
				try {

					{
						logger.info("Type predicate processed {}/{}", typePredicatesProcessed, typePredicates.size());
						logger.info(
								"Classes equivalent to or subsumed by rdfs:Class processed for current predicate {}/{}",
								classClassesProcessed, classClasses.size());
						IteratorTripleString its = hdt.search("", typePredicate, classClass);
						logger.info("Retrieving {} ?class {} {}:", its.estimatedNumResults(), typePredicate,
								classClass);

						while (its.hasNext()) {
							TripleString ts = its.next();
							if (!esg.ID.containsKey(ts.getSubject().toString())) {
								esg.ID.put(ts.getSubject().toString(), ++id);
								esg.IS.put(id, ts.getSubject().toString());
							}
						}
					}

					{
						IteratorTripleString its_type = hdt.search("", typePredicate, "");
						while (its_type.hasNext()) {
							TripleString ts = its_type.next();
							if (!esg.ID.containsKey(ts.getObject().toString())) {
								esg.ID.put(ts.getObject().toString(), ++id);
								esg.IS.put(id, ts.getObject().toString());
							}
						}
					}
				} catch (NotFoundException e) {
					e.printStackTrace();
				}

				classClassesProcessed++;
			}
			typePredicatesProcessed++;
		}

		// A class is the subject of a triple where the property has
		// rdfs:Class as domain.
		logger.info(
				"Applying heuristics: A class is the subject of a triple where the property has rdfs:Class as domain.");
		Set<String> domainredicates = esg_properties
				.getEquivalentOrSubsumedEntities("http://www.w3.org/2000/01/rdf-schema#domain");
		logger.info("Number of properties equivalent to or subsumed by rdfs:domain: {}", domainredicates.size());
		int domainPredicatesProcessed = 0;
		for (String domainPredicate : domainredicates) {
			classClassesProcessed = 0;
			for (String classClass : classClasses) {
				try {
					logger.info("Domain predicate processed {}/{}", domainPredicatesProcessed, domainredicates.size());
					logger.info("Classes equivalent to or subsumed by rdfs:Class processed for current predicate {}/{}",
							classClassesProcessed, classClasses.size());
					IteratorTripleString its = hdt.search("", domainPredicate, classClass);
					logger.info("Current domain property: {}", domainPredicate);
					logger.info("Current class Class: {}", classClass);
					logger.info("Number of properties: {}", its.estimatedNumResults());
					while (its.hasNext()) {
						TripleString ts = its.next();
						IteratorTripleString its2 = hdt.search("", ts.getSubject(), "");
						logger.info("Current property: {}", ts.getSubject());
						logger.info("Number of classes retrieved: {}", its2.estimatedNumResults());
						while (its2.hasNext()) {
							TripleString ts2 = its2.next();
							if (!esg.ID.containsKey(ts2.getSubject().toString())) {
								esg.ID.put(ts2.getSubject().toString(), ++id);
								esg.IS.put(id, ts2.getSubject().toString());
							}
						}
					}
				} catch (NotFoundException e) {
					e.printStackTrace();
				}
				classClassesProcessed++;
			}
			domainPredicatesProcessed++;
		}

		// A class is the object of a triple where the property has
		// rdfs:Class as range.
		logger.info(
				"Applying heuristics: A class is the object of a triple where the property has rdfs:Class as range.");
		Set<String> rangePredicates = esg_properties
				.getEquivalentOrSubsumedEntities("http://www.w3.org/2000/01/rdf-schema#range");
		logger.info("Number of properties equivalent to or subsumed by rdfs:range: {}", rangePredicates.size());
		int rangePredicatesProcessed = 0;
		for (String rangePredicate : rangePredicates) {
			classClassesProcessed = 0;
			for (String classClass : classClasses) {
				try {
					logger.info("Range predicate processed {}/{}", rangePredicatesProcessed++, rangePredicates.size());
					logger.info("Classes equivalent to or subsumed by rdfs:Class processed for current predicate {}/{}",
							classClassesProcessed++, classClasses.size());
					IteratorTripleString its = hdt.search("", rangePredicate, classClass);
					logger.info("Current range property: {}", rangePredicate);
					logger.info("Current class Class: {}", classClass);
					logger.info("Number of properties: {}", its.estimatedNumResults());
					while (its.hasNext()) {
						TripleString ts = its.next();
						IteratorTripleString its2 = hdt.search("", ts.getSubject(), "");
						logger.info("Current property: {}", ts.getSubject());
						logger.info("Number of classes retrieved: {}", its2.estimatedNumResults());
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
				}
			}
		}

	}

	@Override
	public void addSpareEntitiesToEquivalenceSetGraph(EquivalenceSetGraph esg_classes, HDT hdt) {

	}

	@Override
	public void addSpareEntitiesToEquivalentSetGraphUsignESGForClasses(EquivalenceSetGraph esg,
			EquivalenceSetGraph esg_classes, HDT hdt) {

	}

}
