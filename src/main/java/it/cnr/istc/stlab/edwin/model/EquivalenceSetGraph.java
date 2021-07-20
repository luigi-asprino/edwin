package it.cnr.istc.stlab.edwin.model;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.cnr.istc.stlab.edwin.EquivalenceSetGraphStats;

public interface EquivalenceSetGraph {

	static Logger logger = LoggerFactory.getLogger(EquivalenceSetGraph.class);
	
	public Long getMaxId();
	
	public boolean containsEntity(String uri);
	
	public void createSingleEntityEquivalenceSet(String uri, Long id);
	

	public Set<Long> getIndirectlySubsumedEquivalenceSets(Long es);
	
	public void setEquivalenceSetIndirectSize(Long esId, Long size);

	public void setEquivalenceSetDirectSize(Long esId, Long size);
	
	public Long getOESize(String entityURI);
	
	public Iterator<Entry<Long, Collection<String>>> equivalenceSetsIterator();

	public Long getEntityDirectExtensionalSize(String entityURI);
	
	public Long getEntityIndirectExtensionalSize(String entityURI);

	public Long getEquivalenceSetDirectSize(Long equivalenceSetId);

	public Long getEquivalenceSetIndirectSize(Long equivalenceSetId);

	public Set<String> getEquivalenceSet(Long visitedSetId);

	public Set<String> getEquivalenceSet(String iri);

	public Long getEquivalenceSetIdOfIRI(CharSequence iri);

	public Set<Long> getEquivalenceSetsSubsumedBy(Long equivalenceSetID);

	public Set<Long> getSuperEquivalenceSets(Long equivalenceSetID);

	public Set<String> getEquivalentEntities(String iri);

	public boolean hasEquivalenceSet(String iri);

	public void setEquivalencePropertyToObserve(String iri);

	public void setSpecializationPropertyToObserve(String iri);

	public void setEquivalencePropertyForProperties(String iri);

	public void setSpecializationPropertyForProperties(String iri);

	public Set<Long> getTopLevelEquivalenceSets();

	public String getEquivalencePropertyToObserve();

	public Set<String> getEntities();

	public String getSpecializationPropertyToObserve();

	public String getEquivalencePropertyForProperties();

	public String getSpecializationPropertyForProperties();

	public void addSpecialization(String s, String o);

	public Set<String> getEntitiesImplicityEquivalentToOrSubsumedBy(String entity, boolean useClosure);

	public Set<String> getEntitiesImplicityEquivalentToOrSubsumedBy(String entity, boolean useClosure, int maxDepth);

	public Set<String> getEntitiesImplicityEquivalentToOrSubsumedBy(String entity);

	public Set<Long> getEquivalenceSetIds();
	
	public Set<Long> getEmptyEquivalenceSets();

	public Long getNumberOfObservedEntities();

	public Long getNumberOfEquivalenceSets();

	public default Set<CharSequence> getEquivalentOrSubsumedEntities(String entity) {
		if (entity == null) {
			return new HashSet<>();
		}
		Set<CharSequence> result = new HashSet<>();
		Long idRootEquivalent = getEquivalenceSetIdOfIRI(entity);

		if (idRootEquivalent != null) {
			// get identity set of the entity
			logger.trace("Equivalence set of {}:{}", entity, idRootEquivalent);

			Set<Long> visited = new HashSet<>();
			visited.add(idRootEquivalent);

			Collection<Long> toVisit = getEquivalenceSetsSubsumedBy(idRootEquivalent);

			while (toVisit != null && !toVisit.isEmpty()) {
				// Pick one identitySetToVisit
				long setIdToVisit = (Long) toVisit.iterator().next();
				visited.add(setIdToVisit);
				toVisit.remove(setIdToVisit);

				getEquivalenceSetsSubsumedBy(setIdToVisit).forEach(nextToVisit -> {
					if (!visited.contains(nextToVisit)) {
						toVisit.add(nextToVisit);
					}
				});

			}

			visited.forEach(visitedSetId -> {
				result.addAll(getEquivalenceSet(visitedSetId));
			});
		}

		return result;
	}

	public default void print() {
		Collection<Long> esKeys = this.getEquivalenceSetIds();
		System.out.println("Equivalence Sets\n---------");
		for (Long k : esKeys) {
			System.out.println(k + " -> " + this.getEquivalenceSet(k));
		}
		System.out.println("---------\nHierarchy\n---------");
		for (Long k : esKeys) {
			System.out.println(k + " sub Of " + this.getSuperEquivalenceSets(k));
		}
		System.out.println("---------\nHierarchy Inverse\n---------");
		for (Long k : esKeys) {
			System.out.println(k + " super Of " + this.getEquivalenceSetsSubsumedBy(k));
		}
		System.out.println("---------");
	}

	public void close();

	public Set<String> getSuperEquivalenceSets(String subCat);

	public Iterator<Entry<String, Long>> entityIterator();

	public void setObservedEntitySize(String key, long size);

	public EquivalenceSetGraphStats getStats();

	public Iterator<Entry<String, Long>> observedEntitySizeIterator();

	public Iterator<Entry<Long, Long>> indirectESGSizeIterator();

	public Iterator<Entry<Long, Collection<Long>>> subOfRelationsIterator();

	public Set<Long> getHInverseKeys();

	public Set<Long> getDirectlySubsumedEquivalenceSets(Long key);

	public boolean hasSuperEquivalenceSets(Long key);

	public boolean hasSubEquivalenceSets(Long key);

	public boolean hasIndirectEquivalenceSetSize(Long key);

	public boolean hasObservedEntitySize(String uri);

	public Long getObservedEntitySize(String uri);

}
