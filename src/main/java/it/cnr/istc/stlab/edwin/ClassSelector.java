package it.cnr.istc.stlab.edwin;

import java.util.Set;

import org.rdfhdt.hdt.exceptions.NotFoundException;
import org.rdfhdt.hdt.hdt.HDT;
import org.rdfhdt.hdt.triples.IteratorTripleString;
import org.rdfhdt.hdt.triples.TripleString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClassSelector implements ObservedEntitiesSelector {

	private static Logger logger = LoggerFactory.getLogger(ClassSelector.class);

	public void addSpareEntitiesToEquivalentSetGraphUsignESGForProperties(EquivalenceSetGraph esg,
			EquivalenceSetGraph esg_properties, HDT hdt) {

		logger.info("Adding spare entities to ESG using ESG for properties.");

		long id = esg.getMaxId();

		// A class is an entity that belongs to rdfs:Class
		logger.info("Applying heuristics:  A class is the object of a type statement");

		Set<String> typePredicates = esg_properties
				.getEquivalentOrSubsumedEntities("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");

		logger.info("Number of properties equivalent to or subsumed by rdf:type: {}", typePredicates.size());

		int typePredicatesProcessed = 0;
		int spareEntitiesAdded = 0;

		for (String typePredicate : typePredicates) {
			logger.info("Type predicate procesed {}/{}: {}", typePredicatesProcessed, typePredicates.size(),
					typePredicate);
			try {

				IteratorTripleString its_type = hdt.search("", typePredicate, "");
				long toProcess = its_type.estimatedNumResults();
				logger.info("Number of triples {}", toProcess);
				int processedTriples = 0;
				String lastClass = null;
				while (its_type.hasNext()) {
					if (processedTriples > 0 && processedTriples % 1000000 == 0) {
						logger.info("Triples processed {}/{}", processedTriples, toProcess);
					}
					processedTriples++;
					TripleString ts = its_type.next();
					if (!ts.getObject().toString().equals(lastClass)) {
						if (!esg.ID.containsKey(ts.getObject().toString())) {
							esg.ID.put(ts.getObject().toString(), ++id);
							esg.IS.put(id, ts.getObject().toString());
							spareEntitiesAdded++;
						}
						lastClass = ts.getObject().toString();
					} else {
						continue;
					}
				}
			} catch (NotFoundException e) {
				e.printStackTrace();
			}

			typePredicatesProcessed++;
		}
		
		logger.info("Spare entities added {}", spareEntitiesAdded);
	}

	@Override
	public void addSpareEntitiesToEquivalenceSetGraph(EquivalenceSetGraph esg_classes, HDT hdt) {

	}

	@Override
	public void addSpareEntitiesToEquivalentSetGraphUsignESGForClasses(EquivalenceSetGraph esg,
			EquivalenceSetGraph esg_classes, HDT hdt) {

	}

}
