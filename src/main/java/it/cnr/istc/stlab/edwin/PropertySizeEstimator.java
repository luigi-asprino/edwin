package it.cnr.istc.stlab.edwin;

import java.util.Date;
import java.util.Iterator;
import java.util.Map.Entry;

import org.rdfhdt.hdt.exceptions.NotFoundException;
import org.rdfhdt.hdt.hdt.HDT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PropertySizeEstimator implements ExtensionalSizeEstimator {

	private static Logger logger = LoggerFactory.getLogger(PropertySizeEstimator.class);

	@Override
	public void estimateObservedEntitiesSize(EquivalenceSetGraph esg, HDT hdt) {
		logger.info("Computing extensional size of observed properties");

		long processed = 0;
		long toProcess = esg.numberOfObservedEntities();
		Iterator<Entry<String, Long>> it = esg.ID.iterator();
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
			processed++;
			Entry<String, Long> entry = it.next();
			long size = 0L;
			try {
				size = hdt.search("", entry.getKey(), "").estimatedNumResults();
			} catch (NotFoundException e) {
				e.printStackTrace();
			}
			esg.oe_size.put(entry.getKey(), size);
		}

		logger.info("Extensional size of observed properties computed!");

		
	}

	public void estimateObservedEntitiesSizeUsingESGForProperties(EquivalenceSetGraph esg,
			EquivalenceSetGraph esg_properties, HDT hdt) {

	}

}
