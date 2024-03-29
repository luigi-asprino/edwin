package it.cnr.istc.stlab.edwin.model;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface EquivalenceSetGraph {

	static Logger logger = LoggerFactory.getLogger(EquivalenceSetGraph.class);

	public Long getEntityDirectExtensionalSize(String entityURI);
	
	public Long getEntityIndirectExtensionalSize(String entityURI);

	public Long getEquivalenceSetDirectSize(Long equivalenceSetId);

	public Long getEquivalenceSetIndirectSize(Long equivalenceSetId);

	public Collection<String> getEquivalenceSet(Long visitedSetId);

	public Collection<String> getEquivalenceSet(String iri);

	public Long getEquivalenceSetIdOfIRI(CharSequence iri);

	public Collection<Long> getEquivalenceSetsSubsumedBy(Long equivalenceSetID);

	public Collection<Long> getSuperEquivalenceSets(Long equivalenceSetID);

	public Set<String> getEquivalentEntities(String iri);

	public boolean hasEquivalenceSet(CharSequence iri);

	public void setEquivalencePropertyToObserve(CharSequence iri);

	public void setSpecializationPropertyToObserve(CharSequence iri);

	public void setEquivalencePropertyForProperties(CharSequence iri);

	public void setSpecializationPropertyForProperties(CharSequence iri);

	public Collection<Long> getTopLevelEquivalenceSets();

	public CharSequence getEquivalencePropertyToObserve();

	public Set<String> getEntities();

	public CharSequence getSpecializationPropertyToObserve();

	public CharSequence getEquivalencePropertyForProperties();

	public CharSequence getSpecializationPropertyForProperties();

	public void addSpecialization(CharSequence s, CharSequence o);

	public Set<String> getEntitiesImplicityEquivalentToOrSubsumedBy(String entity, boolean useClosure);

	public Set<String> getEntitiesImplicityEquivalentToOrSubsumedBy(String entity, boolean useClosure, int maxDepth);

	public Set<String> getEntitiesImplicityEquivalentToOrSubsumedBy(String entity);

	public Collection<Long> getEquivalenceSetIds();
	
	public Set<Long> getEmptyEquivalenceSets();

	public int getNumberOfObservedEntities();

	public int getNumberOfEquivalenceSets();

	public default Set<CharSequence> getEquivalentOrSubsumedEntities(CharSequence entity) {
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

}
