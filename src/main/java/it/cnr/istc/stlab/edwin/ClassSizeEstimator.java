package it.cnr.istc.stlab.edwin;

import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.rdfhdt.hdt.exceptions.NotFoundException;
import org.rdfhdt.hdt.hdt.HDT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClassSizeEstimator implements ExtensionalSizeEstimator {

	private static Logger logger = LoggerFactory.getLogger(ClassSizeEstimator.class);

	@Override
	public void estimateObservedEntitiesSize(EquivalenceSetGraph esg, HDT hdt) {

	}

	public void estimateObservedEntitiesSizeUsingESGForProperties(EquivalenceSetGraph esg_classes, EquivalenceSetGraph esg_properties,
			HDT hdt) {

		logger.info("Computing extensional size of observed for properties");

		Set<String> typePredicates = esg_properties
				.getEquivalentOrSubsumedEntities("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");

		Iterator<Map.Entry<String, Long>> it = esg_classes.ID.iterator();

		long processed = 0;
		long toProcess = esg_classes.numberOfObservedEntities();
		long start = System.currentTimeMillis();
		long current = System.currentTimeMillis();
		long elapsed = 0L;
		while (it.hasNext()) {
			if (processed % 10000 == 0) {
				current = System.currentTimeMillis();
				elapsed = current - start;
				double timePerProperty = (double) elapsed / (double) processed;
				long end = System.currentTimeMillis() + ((long) (timePerProperty * (toProcess - processed)));
				logger.info("Processed {}/{} end: {}", processed, toProcess, new Date(end));
			}
			Map.Entry<String, Long> entry = it.next();

			long size = 0L;
			for (String typePredicate : typePredicates) {
				try {
					size += hdt.search("", typePredicate, entry.getKey()).estimatedNumResults();
				} catch (NotFoundException e) {
					e.printStackTrace();
				}
			}

			esg_classes.oe_size.put(entry.getKey(), size);
		}

	}

}
