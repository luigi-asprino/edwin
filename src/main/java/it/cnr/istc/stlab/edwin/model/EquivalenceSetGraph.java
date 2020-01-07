package it.cnr.istc.stlab.edwin.model;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface EquivalenceSetGraph {

	static Logger logger = LoggerFactory.getLogger(EquivalenceSetGraph.class);

	public Collection<CharSequence> getEquivalenceSet(Long visitedSetId);

	public Long getIDOfEquivalenceSet(CharSequence iri);

	public Collection<Long> getEquivalenceSetSubsumedBy(Long equivalenceSetID);

	public boolean hasEquivalenceSet(CharSequence iri);

	public void setEquivalencePropertyToObserve(CharSequence iri);

	public void setSpecializationPropertyToObserve(CharSequence iri);

	public void setEquivalencePropertyForProperties(CharSequence iri);

	public void setSpecializationPropertyForProperties(CharSequence iri);

	public CharSequence getEquivalencePropertyToObserve();

	public CharSequence getSpecializationPropertyToObserve();

	public CharSequence getEquivalencePropertyForProperties();

	public CharSequence getSpecializationPropertyForProperties();

	public void addSpecialization(CharSequence s, CharSequence o);

	public default Set<CharSequence> getEquivalentOrSubsumedEntities(CharSequence entity) {
		if (entity == null) {
			return new HashSet<>();
		}
		Set<CharSequence> result = new HashSet<>();
		Long idRootEquivalent = getIDOfEquivalenceSet(entity);

		if (idRootEquivalent != null) {
			// get identity set of the entity
			logger.trace("Equivalence set of {}:{}", entity, idRootEquivalent);

			Set<Long> visited = new HashSet<>();
			visited.add(idRootEquivalent);

			Collection<Long> toVisit = getEquivalenceSetSubsumedBy(idRootEquivalent);

			while (toVisit != null && !toVisit.isEmpty()) {
				// Pick one identitySetToVisit
				long setIdToVisit = (Long) toVisit.iterator().next();
				visited.add(setIdToVisit);
				toVisit.remove(setIdToVisit);

				getEquivalenceSetSubsumedBy(setIdToVisit).forEach(nextToVisit -> {
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

}
