package it.cnr.istc.stlab.edwin.inmemory;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.compress.compressors.CompressorException;
import org.rdfhdt.hdt.exceptions.NotFoundException;
import org.rdfhdt.hdt.triples.TripleString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.cnr.istc.stlab.edwin.EquivalenceSetGraphBuilderParameters;
import it.cnr.istc.stlab.edwin.model.EquivalenceSetGraph;
import it.cnr.istc.stlab.edwin.model.EquivalenceSetGraphBuilder;
import it.cnr.istc.stlab.lgu.commons.rdf.Dataset;

public final class InMemoryEquivalenceSetGraphBuilder implements EquivalenceSetGraphBuilder {

	private static Logger logger = LoggerFactory.getLogger(EquivalenceSetGraphBuilder.class);

	private Dataset dataset;
	private Set<CharSequence> equivalencePropertiesToProcess = new HashSet<>(),
			equivalencePropertiesProcessed = new HashSet<>(), specializationPropertiesToProcess = new HashSet<>(),
			specializationPropertiesProcessed = new HashSet<>();
	private EquivalenceSetGraphBuilderParameters parameters;
	private InMemoryEquivalenceSetGraph esg;

	@Override
	public EquivalenceSetGraph build(EquivalenceSetGraphBuilderParameters parameters) {
		this.parameters = parameters;

		logger.trace("peq {}", parameters.getEquivalencePropertyToObserve());
		logger.trace("psub {}", parameters.getSpecializationPropertyToObserve());
		logger.trace("pe {}", parameters.getEquivalencePropertiesForProperties());
		logger.trace("ps {}", parameters.getSpecializationPropertyForProperties());

		// updatePropertyToObserve is true if do not update property sets by adding
		// possible specializations of properties to observe
		// TODO
		// boolean updatePropertySets =
		// parameters.getEquivalencePropertiesForProperties() != null
		// || parameters.getSpecializationPropertyForProperties() != null;

		// updatePropertySetsUsignGraph is true if the builder has to search possible
		// specializations of the properties
		// to observe in the equivalence set graph that is being built
		boolean updatePropertySetsUsingGraph = (parameters.getEquivalencePropertyToObserve() != null && parameters
				.getEquivalencePropertyToObserve().equals(parameters.getEquivalencePropertiesForProperties()))
				&& (parameters.getSpecializationPropertyToObserve() != null
						&& parameters.getSpecializationPropertyToObserve()
								.equals(parameters.getSpecializationPropertyForProperties()));

		// TODO
		// Loading possible specialization of properties to observe from esg properites

		this.esg = new InMemoryEquivalenceSetGraph();

		esg.setEquivalencePropertyToObserve(parameters.getEquivalencePropertyToObserve());
		esg.setSpecializationPropertyToObserve(parameters.getSpecializationPropertyToObserve());
		esg.setEquivalencePropertyForProperties(parameters.getEquivalencePropertiesForProperties());
		esg.setSpecializationPropertyForProperties(parameters.getSpecializationPropertyForProperties());

		// adding properties to observe
		if (parameters.getEquivalencePropertyToObserve() != null) {
			equivalencePropertiesToProcess.add(parameters.getEquivalencePropertyToObserve());
		}
		if (parameters.getSpecializationPropertyToObserve() != null) {
			specializationPropertiesToProcess.add(parameters.getSpecializationPropertyToObserve());
		}

		// adding additional properties to observe
		equivalencePropertiesToProcess.addAll(parameters.getAdditionalEquivalencePropertiesToObserve());
		specializationPropertiesToProcess.addAll(parameters.getAdditionalSpecializationPropertiesToObserve());

		int cycle = 0;
		while (!equivalencePropertiesToProcess.isEmpty() || !specializationPropertiesToProcess.isEmpty()) {
			logger.info("Cycle number: {}", cycle);
			computeEquivalentSets();
			computeSpecializations();
			if (updatePropertySetsUsingGraph)
				updatePropropertySets();
			logger.info("End cycle number: {}", cycle++);
		}

		return esg;
	}

	private void updatePropropertySets() {
		{
			Set<CharSequence> subsumedByEquivalencePropertiesToObserve = esg
					.getEquivalentOrSubsumedEntities(parameters.getEquivalencePropertyToObserve());
			for (String additionEquivalencePropertyToObserve : parameters
					.getAdditionalEquivalencePropertiesToObserve()) {
				subsumedByEquivalencePropertiesToObserve
						.addAll(esg.getEquivalentOrSubsumedEntities(additionEquivalencePropertyToObserve));
			}
			subsumedByEquivalencePropertiesToObserve.removeAll(equivalencePropertiesProcessed);
			equivalencePropertiesToProcess.addAll(subsumedByEquivalencePropertiesToObserve);
		}

		{

			Set<CharSequence> subsumedBySpecializationPropertyToObserve = esg
					.getEquivalentOrSubsumedEntities(parameters.getSpecializationPropertyToObserve());
			for (String additionalSpecializationPropertyToObserve : parameters
					.getAdditionalSpecializationPropertiesToObserve()) {
				subsumedBySpecializationPropertyToObserve
						.addAll(esg.getEquivalentOrSubsumedEntities(additionalSpecializationPropertyToObserve));
			}
			subsumedBySpecializationPropertyToObserve.removeAll(specializationPropertiesProcessed);
			specializationPropertiesToProcess.addAll(subsumedBySpecializationPropertyToObserve);

		}

	}

	private void computeSpecializations() {
		while (!this.specializationPropertiesToProcess.isEmpty()) {

			CharSequence propertyToProcess = specializationPropertiesToProcess.iterator().next();
			this.specializationPropertiesToProcess.remove(propertyToProcess);
			this.specializationPropertiesProcessed.add(propertyToProcess);
// TODO
//			esg.getStats().specializationPropertiesUsed.add(propertyToProcess);

			try {
				logger.info("Computing Specialization Relations using {}", propertyToProcess);
				long numOfResults = 0L;
				if (parameters.isComputeEstimation()) {
					numOfResults = dataset.estimateSearch("", propertyToProcess, "");
				}
				logger.info("Number of explicit statements {}", numOfResults);
				// TODO
//				long numberOfStatementsProcessed = 0, numberOfStatementsToProcess = numOfResults;
				dataset.searchStream("", propertyToProcess, "").parallel()
						.forEach(ts -> esg.addSpecialization(ts.getSubject().toString(), ts.getObject().toString()));

			} catch (NotFoundException e) {
				e.printStackTrace();
			} catch (CompressorException e) {
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	private void computeEquivalentSets() {
		while (!equivalencePropertiesToProcess.isEmpty()) {

			CharSequence p_eq = equivalencePropertiesToProcess.iterator().next();
			equivalencePropertiesToProcess.remove(p_eq);
			equivalencePropertiesProcessed.add(p_eq);
			// TODO
//			esg.getStats().equivalencePropertiesUsed.add(p_eq);

			try {
				logger.info("Computing Equivalence Sets using {}", p_eq);
				long numOfResults = 0L;
				if (parameters.isComputeEstimation()) {
					numOfResults = dataset.estimateSearch("", p_eq, "");
				}
				logger.info("Number of explicit statements {}", numOfResults);
				// TODO
//				long numberOfStatementsProcessed = 0, numberOfStatementsToProcess = numOfResults;

				dataset.searchStream("", p_eq, "").parallel().map(ts -> getInitMap(ts)).reduce(esg.es, (m1, m2) -> {

					Map<Long, Long> m2RemappedValues = new HashMap<>();

					m2.forEach((k, v) -> {
						if (m1.containsKey(k)) {
							m2RemappedValues.put(v, m1.get(k));
						} else {
							m1.put(k, v);
						}
					});

					m2.forEach((k, v) -> {
						Long remappedValue = m2RemappedValues.get(v);
						if (remappedValue != null) {
							m1.put(k, remappedValue);
						}
					});

					return m1;
				});

			} catch (NotFoundException e) {
				e.printStackTrace();
			} catch (CompressorException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
// TODO
//		logger.info("Number of Equivalence Triples {}", numberOfEquivalenceTriples);
	}

	public Map<Long, Long> getInitMap(TripleString ts) {
		HashMap<Long, Long> result = new HashMap<>();
		Long newESID = esg.getNewEquivalenceSetID();
		result.put(esg.getOrCreateEntityID(ts.getSubject()), newESID);
		result.put(esg.getOrCreateEntityID(ts.getObject()), newESID);
		return result;
	}

}
