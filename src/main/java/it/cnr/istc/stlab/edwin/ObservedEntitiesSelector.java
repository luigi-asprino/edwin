package it.cnr.istc.stlab.edwin;

import it.cnr.istc.stlab.edwin.model.EquivalenceSetGraph;
import it.cnr.istc.stlab.lgu.commons.semanticweb.datasets.Dataset;

public interface ObservedEntitiesSelector {

	public void addSpareEntitiesToEquivalenceSetGraph(EquivalenceSetGraph esg, Dataset hdt);

	public void addSpareEntitiesToEquivalentSetGraphUsignESGForProperties(EquivalenceSetGraph esg,
			EquivalenceSetGraph esg_properties, Dataset hdt);

	public void addSpareEntitiesToEquivalentSetGraphUsignESGForClasses(EquivalenceSetGraph esg,
			EquivalenceSetGraph esg_classes, Dataset hdt);

}
