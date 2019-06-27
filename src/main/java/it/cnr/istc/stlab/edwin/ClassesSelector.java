package it.cnr.istc.stlab.edwin;

import java.util.HashSet;
import java.util.Set;

import org.rdfhdt.hdt.exceptions.NotFoundException;
import org.rdfhdt.hdt.hdt.HDT;
import org.rdfhdt.hdt.triples.IteratorTripleString;
import org.rdfhdt.hdt.triples.TripleString;

public class ClassesSelector implements ObservedEntitiesSelector {

	public void addSpareEntitiesToEquivalentSetGraph(EquivalenceSetGraph esg_classes,
			EquivalenceSetGraph esg_properties, HDT hdt) {

		long id = esg_properties.getMaxId();

		// A class is an entity that belongs to rdfs:Class
		Set<String> typePredicates = esg_properties
				.getEquivalentOrSubsumedEntities("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
		Set<String> classClasses = new HashSet<>();
		classClasses.add("http://www.w3.org/2000/01/rdf-schema#Class");
		if (esg_classes != null) {
			classClasses
					.addAll(esg_classes.getEquivalentOrSubsumedEntities("http://www.w3.org/2000/01/rdf-schema#Class"));
		}
		for (String typePredicate : typePredicates) {
			for (String classClass : classClasses) {
				try {
					IteratorTripleString its = hdt.search("", typePredicate, classClass);
					while (its.hasNext()) {
						TripleString ts = its.next();
						if (!esg_classes.ID.containsKey(ts.getSubject().toString())) {
							esg_classes.ID.put(ts.getSubject().toString(), ++id);
							esg_classes.IS.put(id, ts.getSubject().toString());
						}
					}
				} catch (NotFoundException e) {
					e.printStackTrace();
				}

			}
		}

		// A class is the subject of a triple where the property has
		// rdfs:Class as domain.
		Set<String> domainredicates = esg_properties
				.getEquivalentOrSubsumedEntities("http://www.w3.org/2000/01/rdf-schema#domain");
		for (String domainPredicate : domainredicates) {
			for (String classClass : classClasses) {
				try {
					IteratorTripleString its = hdt.search("", domainPredicate, classClass);
					while (its.hasNext()) {
						TripleString ts = its.next();
						IteratorTripleString its2 = hdt.search("", ts.getSubject(), "");
						while (its2.hasNext()) {
							TripleString ts2 = its2.next();
							if (!esg_classes.ID.containsKey(ts2.getSubject().toString())) {
								esg_classes.ID.put(ts2.getSubject().toString(), ++id);
								esg_classes.IS.put(id, ts2.getSubject().toString());
							}
						}
					}
				} catch (NotFoundException e) {
					e.printStackTrace();
				}
			}
		}

		// A class is the object of a triple where the property has
		// rdfs:Class as range.
		Set<String> rangePredicates = esg_properties
				.getEquivalentOrSubsumedEntities("http://www.w3.org/2000/01/rdf-schema#range");
		for (String rangePredicate : rangePredicates) {
			for (String classClass : classClasses) {
				try {
					IteratorTripleString its = hdt.search("", rangePredicate, classClass);
					while (its.hasNext()) {
						TripleString ts = its.next();
						IteratorTripleString its2 = hdt.search("", ts.getSubject(), "");
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

}
