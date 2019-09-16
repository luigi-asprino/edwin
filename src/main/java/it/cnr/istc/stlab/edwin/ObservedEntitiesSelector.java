package it.cnr.istc.stlab.edwin;

import org.rdfhdt.hdt.hdt.HDT;

public interface ObservedEntitiesSelector {

	public void addSpareEntitiesToEquivalenceSetGraph(EquivalenceSetGraph esg, HDT hdt);

	public void addSpareEntitiesToEquivalentSetGraphUsignESGForProperties(EquivalenceSetGraph esg,
			EquivalenceSetGraph esg_properties, HDT hdt);

	public void addSpareEntitiesToEquivalentSetGraphUsignESGForClasses(EquivalenceSetGraph esg,
			EquivalenceSetGraph esg_classes, HDT hdt);

}
