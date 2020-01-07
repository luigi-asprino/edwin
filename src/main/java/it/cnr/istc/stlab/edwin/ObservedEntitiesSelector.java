package it.cnr.istc.stlab.edwin;

import it.cnr.istc.stlab.lgu.commons.rdf.Dataset;

public interface ObservedEntitiesSelector {

	public void addSpareEntitiesToEquivalenceSetGraph(RocksDBBackedEquivalenceSetGraph esg, Dataset hdt);

	public void addSpareEntitiesToEquivalentSetGraphUsignESGForProperties(RocksDBBackedEquivalenceSetGraph esg,
			RocksDBBackedEquivalenceSetGraph esg_properties, Dataset hdt);

	public void addSpareEntitiesToEquivalentSetGraphUsignESGForClasses(RocksDBBackedEquivalenceSetGraph esg,
			RocksDBBackedEquivalenceSetGraph esg_classes, Dataset hdt);

}
