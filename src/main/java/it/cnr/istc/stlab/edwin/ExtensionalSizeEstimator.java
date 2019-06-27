package it.cnr.istc.stlab.edwin;

import org.rdfhdt.hdt.hdt.HDT;

public interface ExtensionalSizeEstimator {

	public void estimateObservedEntitiesSize(EquivalenceSetGraph esg, HDT hdt);
	
	public default void estimateEquivalenceSetDirectSize() {
		
	}

}
