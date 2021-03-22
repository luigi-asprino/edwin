package it.cnr.istc.stlab.edwin;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.cnr.istc.stlab.lgu.commons.semanticweb.datasets.Dataset;


public interface ExtensionalSizeEstimator {

	public final static Logger logger = LoggerFactory.getLogger(PropertySizeEstimator.class);

	public void estimateObservedEntitiesSize(RocksDBBackedEquivalenceSetGraph esg, Dataset dataset);

	public void estimateObservedEntitiesSizeUsingESGForProperties(RocksDBBackedEquivalenceSetGraph esg,
			RocksDBBackedEquivalenceSetGraph esg_properties, Dataset hdt);

	public default void estimateEquivalenceSetDirectExtensionalSize(RocksDBBackedEquivalenceSetGraph esg) {

		logger.info("Computing Direct Extensional Size for Equivalence Set Graph");

		Iterator<Entry<Long, Collection<String>>> it = esg.IS.iterator();
		while (it.hasNext()) {
			Entry<Long, Collection<String>> entry = it.next();
			long size = 0;
			for (String u : entry.getValue()) {
				size += esg.oe_size.get(u);
			}
			esg.DES.put(entry.getKey(), size);
		}

		logger.info("Extensional Size for Equivalence Set Graph Computed");

	}

	public default void estimateEquivalenceSetIndirectExtensionalSize(RocksDBBackedEquivalenceSetGraph esg) {

		logger.info("Computing Indirect Extensional Size for Equivalence Set Graph");

		long processed = 0;
		long toProcess = esg.IS.keySet().size();

		Iterator<Entry<Long, Collection<String>>> it = esg.IS.iterator();
		while (it.hasNext()) {
			if (processed % 10000 == 0) {
				logger.info("Processed {}/{} equivalence sets", processed, toProcess);
			}

			Entry<Long, Collection<String>> entry = it.next();
			long size = esg.DES.get(entry.getKey());

			Collection<Long> subES = esg.C_inverse.get(entry.getKey());

			if (subES != null) {
				for (Long u : subES) {
					size += esg.DES.get(u);
				}
			}

			esg.IES.put(entry.getKey(), size);
			processed++;
		}

		logger.info("Indirect Extensional Size for Equivalence Set Graph Computed");

	}

}
