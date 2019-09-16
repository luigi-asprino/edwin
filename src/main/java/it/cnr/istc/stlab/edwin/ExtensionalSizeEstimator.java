package it.cnr.istc.stlab.edwin;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map.Entry;

import org.rdfhdt.hdt.hdt.HDT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface ExtensionalSizeEstimator {

	public final static Logger logger = LoggerFactory.getLogger(PropertySizeEstimator.class);

	public void estimateObservedEntitiesSize(EquivalenceSetGraph esg, HDT hdt);

	public void estimateObservedEntitiesSizeUsingESGForProperties(EquivalenceSetGraph esg,
			EquivalenceSetGraph esg_properties, HDT hdt);

	public default void estimateEquivalenceSetDirectExtensionalSize(EquivalenceSetGraph esg) {

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

	public default void estimateEquivalenceSetIndirectExtensionalSize(EquivalenceSetGraph esg) {

		logger.info("Computing Indirect Extensional Size for Equivalence Set Graph");

		Iterator<Entry<Long, Collection<String>>> it = esg.IS.iterator();
		while (it.hasNext()) {
			Entry<Long, Collection<String>> entry = it.next();
			long size = esg.DES.get(entry.getKey());

			Collection<Long> subES = esg.C_inverse.get(entry.getKey());

			if (subES != null) {
				for (Long u : subES) {
					size += esg.DES.get(u);
				}
			}

			esg.IES.put(entry.getKey(), size);
		}

		logger.info("Indirect Extensional Size for Equivalence Set Graph Computed");

	}

}
