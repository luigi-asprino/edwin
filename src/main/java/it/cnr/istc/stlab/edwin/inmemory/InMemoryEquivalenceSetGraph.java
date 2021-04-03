package it.cnr.istc.stlab.edwin.inmemory;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;

import it.cnr.istc.stlab.edwin.model.EquivalenceSetGraph;

public class InMemoryEquivalenceSetGraph implements EquivalenceSetGraph {

	private AtomicLong nextESIDToAssing = new AtomicLong(0L);
	private AtomicLong nextIRIIDToAssing = new AtomicLong(0L);
	private BiMap<CharSequence, Long> entityID = HashBiMap.create();
	Map<Long, Long> es = new HashMap<>();
	private Multimap<Long, Long> h = MultimapBuilder.treeKeys().treeSetValues().build();
	private Multimap<Long, Long> h_inverse = MultimapBuilder.treeKeys().treeSetValues().build();
	private CharSequence equivalencePropertyToObserve;
	private CharSequence specializationPropertyToObserve;
	private CharSequence equivalencePropertyForProperties;
	private CharSequence specializationPropertyForProperties;

	InMemoryEquivalenceSetGraph() {

	}

	@Override
	public boolean hasEquivalenceSet(CharSequence iri) {
		return es.containsKey(getOrCreateEntityID(iri));
	}

	@Override
	public void setEquivalencePropertyToObserve(CharSequence iri) {
		this.equivalencePropertyToObserve = iri;
	}

	@Override
	public void setSpecializationPropertyToObserve(CharSequence iri) {
		this.specializationPropertyToObserve = iri;

	}

	@Override
	public void setEquivalencePropertyForProperties(CharSequence iri) {
		this.equivalencePropertyForProperties = iri;
	}

	@Override
	public void setSpecializationPropertyForProperties(CharSequence iri) {
		this.specializationPropertyForProperties = iri;
	}

	@Override
	public CharSequence getEquivalencePropertyToObserve() {
		return this.equivalencePropertyToObserve;
	}

	@Override
	public CharSequence getSpecializationPropertyToObserve() {
		return this.specializationPropertyToObserve;
	}

	@Override
	public CharSequence getEquivalencePropertyForProperties() {
		return this.equivalencePropertyForProperties;
	}

	@Override
	public CharSequence getSpecializationPropertyForProperties() {
		return this.specializationPropertyForProperties;
	}

	@Override
	public void addSpecialization(CharSequence s, CharSequence o) {
		h.put(getOrCreateEquivalenceSetOfURI(s), getOrCreateEquivalenceSetOfURI(o));
		h_inverse.put(getOrCreateEquivalenceSetOfURI(o), getOrCreateEquivalenceSetOfURI(s));
	}

	Long getOrCreateEntityID(CharSequence iri) {
		Long result = entityID.get(iri);
		if (result != null) {
			return result;
		}
		synchronized (entityID) {
			result = nextIRIIDToAssing.getAndIncrement();
			entityID.put(iri, result);
		}
		return result;
	}

	CharSequence getEntityIRIFromEntityID(Long uriID) {
		return entityID.inverse().get(uriID);
	}

	Long getOrCreateEquivalenceSetOfURI(CharSequence URI) {
		Long uriID = entityID.get(URI);
		if (uriID != null) {
			return es.get(uriID);
		} else {
			uriID = nextIRIIDToAssing.getAndIncrement();
			synchronized (es) {
				Long result = nextESIDToAssing.getAndIncrement();
				es.put(uriID, result);
				return result;
			}
		}
	}

	@Override
	public Collection<String> getEquivalenceSet(Long equivalenceSetID) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Long getEquivalenceSetIdOfIRI(CharSequence iri) {
		return null;
	}

	@Override
	public Collection<Long> getEquivalenceSetsSubsumedBy(Long equivalenceSetID) {
		// TODO Auto-generated method stub
		return null;
	}

	Long getNewEquivalenceSetID() {
		return nextESIDToAssing.getAndIncrement();
	}

	@Override
	public Collection<Long> getSuperEquivalenceSets(Long equivalenceSetID) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<Long> getEquivalenceSetIds() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<Long> getTopLevelEquivalenceSets() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<String> getEntitiesImplicityEquivalentToOrSubsumedBy(String entity, boolean useClosure) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<String> getEntitiesImplicityEquivalentToOrSubsumedBy(String entity) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getNumberOfObservedEntities() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getNumberOfEquivalenceSets() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Collection<String> getEquivalenceSet(String iri) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<String> getEntitiesImplicityEquivalentToOrSubsumedBy(String entity, boolean useClosure, int maxDepth) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<String> getEquivalentEntities(String iri) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void close() {
		// TODO Auto-generated method stub
		
	}
}
