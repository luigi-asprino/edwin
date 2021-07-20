package it.cnr.istc.stlab.edwin.rocksdb;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.compress.compressors.CompressorException;
import org.apache.jena.ext.com.google.common.collect.Lists;
import org.rdfhdt.hdt.exceptions.NotFoundException;
import org.rdfhdt.hdt.triples.TripleString;
import org.rocksdb.RocksDBException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.cnr.istc.stlab.edwin.EquivalenceSetGraphAnalyser;
import it.cnr.istc.stlab.edwin.EquivalenceSetGraphBuilderParameters;
import it.cnr.istc.stlab.edwin.Utils;
import it.cnr.istc.stlab.lgu.commons.semanticweb.datasets.Dataset;

public class RocksDBEquivalenceSetGraphBuilderImpl {

	private static Logger logger = LoggerFactory.getLogger(RocksDBEquivalenceSetGraphBuilderImpl.class);
	private long lastIdentitySetId = 0;
	private long numberOfEquivalenceTriples = 0L, numberOfSpecializationTriples = 0L;
	protected Set<String> equivalencePropertiesToProcess = new HashSet<>(),
			equivalencePropertiesProcessed = new HashSet<>(), specializationPropertiesToProcess = new HashSet<>(),
			specializationPropertiesProcessed = new HashSet<>();
	protected RocksDBBackedEquivalenceSetGraph esg;
	protected Dataset dataset;
	protected EquivalenceSetGraphBuilderParameters parameters;
	private boolean updatePropertySetsUsingGraph;
	private RocksDBBackedEquivalenceSetGraph esgProperties;

	public RocksDBEquivalenceSetGraphBuilderImpl(String[] filelist) throws IOException {
		dataset = Dataset.getInstanceFromFileList(Lists.newArrayList(filelist));
	}

	public RocksDBEquivalenceSetGraphBuilderImpl(List<String> filelist) throws IOException {
		dataset = Dataset.getInstanceFromFileList(filelist);
	}

	private void initParameters(EquivalenceSetGraphBuilderParameters p) throws RocksDBException, IOException {

		parameters = p.clone();
		logger.trace("Parameters before {}", parameters);

		new File(parameters.getEsgFolder()).mkdirs();

		logger.trace("peq {}", parameters.getEquivalencePropertyToObserve());
		logger.trace("psub {}", parameters.getSpecializationPropertyToObserve());
		logger.trace("pe {}", parameters.getEquivalencePropertiesForProperties());
		logger.trace("ps {}", parameters.getSpecializationPropertyForProperties());

		// updatePropertyToObserve is true if do not update property sets by adding
		// possible specializations of properties to observe
		boolean updatePropertySets = parameters.getEquivalencePropertiesForProperties() != null
				|| parameters.getSpecializationPropertyForProperties() != null;

		// updatePropertySetsUsignGraph is true if the builder has to search possible
		// specializations of the properties
		// to observe in the equivalence set graph that is being built
		updatePropertySetsUsingGraph = (parameters.getEquivalencePropertyToObserve() != null && parameters
				.getEquivalencePropertyToObserve().equals(parameters.getEquivalencePropertiesForProperties()))
				&& (parameters.getSpecializationPropertyToObserve() != null
						&& parameters.getSpecializationPropertyToObserve()
								.equals(parameters.getSpecializationPropertyForProperties()));

		esgProperties = null;

		if (parameters.getEsgPropertiesFolder() != null) {
			logger.trace("Load ESG for properties");
			esgProperties = new RocksDBBackedEquivalenceSetGraph(parameters.getEsgPropertiesFolder());
			updatePropertySetsUsingGraph = false;
		}

		if (esgProperties == null && parameters.getEsgProperties() != null) {
			logger.trace("Reuse already loaded ESG for properties");
			esgProperties = parameters.getEsgProperties();
			updatePropertySetsUsingGraph = false;
		}

		if (!updatePropertySetsUsingGraph && esgProperties == null && updatePropertySets) {
			logger.trace("Build ESG for properties");
			EquivalenceSetGraphBuilderParameters esgbp = new EquivalenceSetGraphBuilderParameters();
			esgbp.setEquivalencePropertyToObserve(parameters.getEquivalencePropertiesForProperties());
			esgbp.setSpecializationPropertyToObserve(parameters.getSpecializationPropertyForProperties());
			esgbp.setEsgFolder(parameters.getEsgFolder() + "/esgProperties");
			esgbp.setComputeEstimation(false);
			esgbp.setExportInRDFFormat(false);
			esgbp.setComputeStats(false);
			RocksDBEquivalenceSetGraphBuilderImpl builder = new RocksDBEquivalenceSetGraphBuilderImpl(this.dataset.getFiles());
			esgProperties = builder.build(esgbp);
			logger.trace("ESG for properties computed");
		}

		if (!updatePropertySetsUsingGraph && esgProperties != null && updatePropertySets) {

			// TODO check if semantics for building the graph is compliant with semantics of
			// SpecializationPropertyForProperties and EquivalencePropertiesForProperties

			// Getting properties that are implicitly equivalent to or subsumed by the
			// properties to observe
			logger.info("Adding properties equivalent or subsumed to equivalence property to observe ({})",
					parameters.getEquivalencePropertyToObserve());
			Set<String> equivalenceProperties = esgProperties
					.getEntitiesImplicityEquivalentToOrSubsumedBy(parameters.getEquivalencePropertyToObserve());
			equivalenceProperties.removeAll(parameters.getNotEquivalenceProperties());
			logger.info("Removing not equivalent properties ({} properties): {}",
					parameters.getNotEquivalenceProperties().size(), parameters.getNotEquivalenceProperties());
			equivalencePropertiesToProcess.addAll(equivalenceProperties);
			logger.info("Adding properties equivalent or subsumed to: ",
					parameters.getSpecializationPropertyToObserve());
			Set<String> specializationProperties = esgProperties
					.getEntitiesImplicityEquivalentToOrSubsumedBy(parameters.getSpecializationPropertyToObserve());
			logger.info("Retrieved {}", specializationProperties.size());
			specializationProperties.removeAll(parameters.getNotSpecializationProperties());
			logger.info("Removing ({} properties): {}", parameters.getNotSpecializationProperties().size(),
					parameters.getNotSpecializationProperties());
			specializationPropertiesToProcess.addAll(specializationProperties);

		}

		logger.trace("Parameters {}", parameters);

		esg = new RocksDBBackedEquivalenceSetGraph(p.getEsgFolder());
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
	}

	public RocksDBBackedEquivalenceSetGraph build(EquivalenceSetGraphBuilderParameters p)
			throws RocksDBException, IOException {

		initParameters(p);

		long cycle = 0;

		logger.info("\n\nStart Building Process\n\n");
		logger.info("Number of equivalence properties to process {} {}", equivalencePropertiesToProcess.size(),
				equivalencePropertiesToProcess);
		logger.info("Number of specialization properties to process {} {}", specializationPropertiesToProcess.size(),
				specializationPropertiesToProcess);
		logger.info("Parameters  {} ", this.parameters.toString());

		while (!equivalencePropertiesToProcess.isEmpty() || !specializationPropertiesToProcess.isEmpty()) {
			logger.info("Cycle number: {}", cycle);
			computeEquivalentSets();
			if (logger.isDebugEnabled())
				esg.print();
			computeSpecializations();
			if (logger.isDebugEnabled())
				esg.print();
			if (updatePropertySetsUsingGraph)
				updatePropropertySets();
			logger.info("End cycle number: {}", cycle++);
		}

		// compute specialization closure
		if (parameters.isComputeClosure()) {
			logger.info("Compute Specialization Closure");
			esg.computeSpecializationClosure();
		}

		// add spare entities (observed entities that are not equivalent to or subsumed
		// by other observed entities)
		if (parameters.getObservedEntitiesSelector() != null) {
			logger.info("Adding spare entities");
			parameters.getObservedEntitiesSelector().addSpareEntitiesToEquivalenceSetGraph(esg, dataset);
			if (updatePropertySetsUsingGraph) {
				parameters.getObservedEntitiesSelector().addSpareEntitiesToEquivalentSetGraphUsignESGForProperties(esg,
						esg, dataset);
			} else {
				if (parameters.getEsgPropertiesFolder() != null) {
					parameters.getObservedEntitiesSelector()
							.addSpareEntitiesToEquivalentSetGraphUsignESGForProperties(esg, esgProperties, dataset);
				}
			}

			if (parameters.getEsgClassesFolder() != null) {
				RocksDBBackedEquivalenceSetGraph esgClasses = new RocksDBBackedEquivalenceSetGraph(
						parameters.getEsgClassesFolder());
				parameters.getObservedEntitiesSelector().addSpareEntitiesToEquivalentSetGraphUsignESGForClasses(esg,
						esgClasses, dataset);
			}
		}

		// compute extensional size of the equivalence sets
		if (parameters.getExtensionalSizeEstimator() != null) {

			// Computing extensional size of the observed entities
			parameters.getExtensionalSizeEstimator().estimateObservedEntitiesSize(esg, dataset);

			if (updatePropertySetsUsingGraph) {
				parameters.getExtensionalSizeEstimator().estimateObservedEntitiesSizeUsingESGForProperties(esg, esg,
						dataset);
			} else {
				if (parameters.getEsgPropertiesFolder() != null) {
					parameters.getExtensionalSizeEstimator().estimateObservedEntitiesSizeUsingESGForProperties(esg,
							esgProperties, dataset);
				}
			}

			// Computing extensional direct size of the equivalence sets
			parameters.getExtensionalSizeEstimator().estimateEquivalenceSetDirectExtensionalSize(esg);

			// Computing extensional indirect size of the equivalence sets
			parameters.getExtensionalSizeEstimator().estimateEquivalenceSetIndirectExtensionalSize(esg);

		}

		if (parameters.isComputeStats()) {
			// compute stats
			if (updatePropertySetsUsingGraph) {
				computeStats(esg);
			}
		}

		return esg;
	}

	private void computeStats(RocksDBBackedEquivalenceSetGraph esgProperties) throws IOException {

		// save equivalence and specialization properties
		esg.getStats().equivalencePropertiesUsed = new HashSet<>(equivalencePropertiesProcessed);
		esg.getStats().equivalencePropertiesUsed.removeAll(parameters.getNotEquivalenceProperties());
		esg.getStats().specializationPropertiesUsed = new HashSet<>(specializationPropertiesProcessed);
		esg.getStats().specializationPropertiesUsed.removeAll(parameters.getNotSpecializationProperties());

		// compute stats
		esg.getStats().numberOfEquivalenceTriples = numberOfEquivalenceTriples;
		esg.getStats().numberOfSpecializationTriples = numberOfSpecializationTriples;

		esg.getStats().oe = esg.ID.keySet().size();
		esg.getStats().es = esg.IS.keySet().size();

		EquivalenceSetGraphAnalyser.countBlankNodes(esg);
		EquivalenceSetGraphAnalyser.countEdges(esg);
		EquivalenceSetGraphAnalyser.computeHeight(esg);
		EquivalenceSetGraphAnalyser.countIsoltatedEquivalenceSets(esg);
		EquivalenceSetGraphAnalyser.countTopLevelEquivalenceSetsAndAssessEmptyNodes(esg);
		EquivalenceSetGraphAnalyser.computeDistributionOfExtensionalSizeOfEquivalenceSets(esg);
		EquivalenceSetGraphAnalyser.countObservedEntitiesWithEmptyExtesion(esg);
		EquivalenceSetGraphAnalyser.countEquivalenceSetsWithEmptyExtension(esg);

		// save esg statistics
		esg.saveStats();

	}

	private void computeEquivalentSets() throws IOException {

		while (!equivalencePropertiesToProcess.isEmpty()) {

			String p_eq = equivalencePropertiesToProcess.iterator().next();
			equivalencePropertiesToProcess.remove(p_eq);
			equivalencePropertiesProcessed.add(p_eq);
			esg.getStats().equivalencePropertiesUsed.add(p_eq);

			try {
				logger.info("Computing Equivalence Sets using {}", p_eq);
				long numOfResults = 0L;
				if (parameters.isComputeEstimation()) {
					numOfResults = dataset.estimateSearch("", p_eq, "");
					logger.info("Number of explicit statements {}", numOfResults);
				}
				Iterator<TripleString> it = dataset.search("", p_eq, "");
				long numberOfStatementsProcessed = 0, numberOfStatementsToProcess = numOfResults;

				while (it.hasNext()) {

					if (numberOfStatementsProcessed % 10000 == 0) {

						Runtime runtime = Runtime.getRuntime();
						runtime.gc();
						long memory = runtime.totalMemory() - runtime.freeMemory();

						logger.info("Number of statements processed {}/{}", numberOfStatementsProcessed,
								numberOfStatementsToProcess);
						logger.info("Number of statements processed {}", numberOfStatementsProcessed);
						logger.info("Memory used:: {}", Utils.humanReadableByteCount(memory, true));
					}

					numberOfStatementsProcessed++;

					TripleString tripleString = (TripleString) it.next();
					String subject = tripleString.getSubject().toString();
					String object = tripleString.getObject().toString();
					addEquivalence(subject, object);
					numberOfEquivalenceTriples++;

				}
			} catch (NotFoundException e) {
				e.printStackTrace();
			} catch (CompressorException e) {
				e.printStackTrace();
			}
		}

		logger.info("Number of Equivalence Triples {}", numberOfEquivalenceTriples);
	}

	private synchronized long getIdentityNewSetId() {
		return ++lastIdentitySetId;
	}

	private void addEquivalence(String subject, String object) {

		logger.debug("Subject {} Object {}", subject, object);
		Long subjectIdentitySetId = esg.ID.get(subject), objectIdentitySetId = esg.ID.get(object);
		boolean subjectHasID = subjectIdentitySetId != null;
		boolean objectHasID = objectIdentitySetId != null;

		if (!subjectHasID && !objectHasID) {
			logger.debug("not subj not obj");

			// The subject and the object are not contained in any identity set
			// {subject,object} is the new identity set

			long c = getIdentityNewSetId();
			esg.ID.put(subject, c);
			esg.ID.put(object, c);
			esg.IS.put(c, subject);
			esg.IS.put(c, object);

		} else if (subjectHasID && !objectHasID) {

			logger.debug(" subj not obj");

			// the subject is contained in one identity set (at least) but the object is not
			// contained in any identity set
			// put object in the identity set of the subject
//			subjectIdentitySetId = esg.ID.get(subject);
			esg.ID.put(object, subjectIdentitySetId);
			esg.IS.put(subjectIdentitySetId, object);

		} else if (!subjectHasID && objectHasID) {

			// the object is contained in one identity set (at least) but the subject is not
//			objectIdentitySetId = esg.ID.get(object);
			esg.ID.put(subject, objectIdentitySetId);
			esg.IS.put(objectIdentitySetId, subject);

		} else if (subjectHasID && objectHasID && !subjectIdentitySetId.equals(objectIdentitySetId)) {

			// The subject and the object are contained in to two different identity sets
			// The two identity sets must be merged

//			subjectIdentitySetId = esg.ID.get(subject);
//			objectIdentitySetId = esg.ID.get(object);
			long newId = getIdentityNewSetId();

			logger.trace("Meging {} and {} into {}", subjectIdentitySetId, objectIdentitySetId, newId);

			{
				Collection<String> identitySetOfTheSubject = esg.IS.get(subjectIdentitySetId);
				for (String res : identitySetOfTheSubject) {
					esg.ID.put(res, newId);
					esg.IS.put(newId, res);
				}
				esg.IS.removeAll(subjectIdentitySetId);
				if (esg.IS.containsKey(subjectIdentitySetId)) {
					throw new RuntimeException(subjectIdentitySetId + " removed but it is still in the DB!");
				}
			}
			{
				Collection<String> identitySetOfTheObject = esg.IS.get(objectIdentitySetId);
				for (String res : identitySetOfTheObject) {
					esg.ID.put(res, newId);
					esg.IS.put(newId, res);
				}
				esg.IS.removeAll(objectIdentitySetId);
				if (esg.IS.containsKey(objectIdentitySetId)) {
					throw new RuntimeException(subjectIdentitySetId + " removed but it is still in the DB!");
				}
			}

			// Adjust hierarchy
			adjsutHierarchies(subjectIdentitySetId, objectIdentitySetId, newId);

		}
	}

	private void adjsutHierarchies(long i1, long i2, long newId) {

		Collection<Long> S1 = esg.H.get(i1);
		if (S1 == null) {
			S1 = new HashSet<>();
		}
		Collection<Long> S2 = esg.H.get(i2);
		if (S2 == null) {
			S2 = new HashSet<>();
		}
		Set<Long> S3 = new HashSet<>();
		S3.addAll(S1);
		S3.addAll(S2);

		Collection<Long> S1_inverse = esg.H_inverse.get(i1);
		if (S1_inverse == null) {
			S1_inverse = new HashSet<>();
		}
		Collection<Long> S2_inverse = esg.H_inverse.get(i2);
		if (S2_inverse == null) {
			S2_inverse = new HashSet<>();
		}
		Set<Long> S3_inverse = new HashSet<>();
		S3_inverse.addAll(S1_inverse);
		S3_inverse.addAll(S2_inverse);

		esg.H.removeAll(i1);
		esg.H.removeAll(i2);

		esg.H_inverse.removeAll(i1);
		esg.H_inverse.removeAll(i2);

		esg.H.putAll(newId, S3);
		esg.H_inverse.putAll(newId, S3_inverse);

		// fixing direct hierarchy replacing i1 with newId
		for (long i11 : S1_inverse) {
			Collection<Long> i11h = esg.H.get(i11);
			i11h.remove(i1);
			i11h.add(newId);
			esg.H.putAll(i11, i11h);
		}

		// fixing inverse hierarchy replacing i1 with newId
		for (long i11 : S1) {
			Collection<Long> i11h_inverse = esg.H_inverse.get(i11);
			i11h_inverse.remove(i1);
			i11h_inverse.add(newId);
			esg.H_inverse.putAll(i11, i11h_inverse);
		}

		// fixing direct hierarchy replacing i2 with newId
		for (long i21 : S2_inverse) {
			Collection<Long> i21h = esg.H.get(i21);
			i21h.remove(i2);
			i21h.add(newId);
			esg.H.putAll(i21, i21h);
		}

		// fixing inverse hierarchy replacing i1 with newId
		for (long i21 : S2) {
			Collection<Long> i21h_inverse = esg.H_inverse.get(i21);
			i21h_inverse.remove(i2);
			i21h_inverse.add(newId);
			esg.H_inverse.putAll(i21, i21h_inverse);
		}

	}

	private void computeSpecializations() throws IOException {

		while (!this.specializationPropertiesToProcess.isEmpty()) {

			String propertyToProcess = specializationPropertiesToProcess.iterator().next();
			this.specializationPropertiesToProcess.remove(propertyToProcess);
			this.specializationPropertiesProcessed.add(propertyToProcess);

			esg.getStats().specializationPropertiesUsed.add(propertyToProcess);

			try {
				logger.info("Computing Specialization Relations using {}", propertyToProcess);
				Iterator<TripleString> it = dataset.search("", propertyToProcess, "");
				long numOfResults = 0L;
				logger.info("Compute estimations {}", parameters.isComputeEstimation());
				if (parameters.isComputeEstimation()) {
					logger.info("Computing estimations");
					numOfResults = dataset.estimateSearch("", propertyToProcess, "");
					logger.info("Number of explicit statements {}", numOfResults);
				}
				long numberOfStatementsProcessed = 0, numberOfStatementsToProcess = numOfResults;
				while (it.hasNext()) {
					if (numberOfStatementsProcessed % 10000 == 0) {

						Runtime runtime = Runtime.getRuntime();
						// Run the garbage collector
						runtime.gc();
						// Calculate the used memory
						long memory = runtime.totalMemory() - runtime.freeMemory();

						logger.info("Statements processed {}/{}", numberOfStatementsProcessed,
								numberOfStatementsToProcess);
						logger.info("Statements processed {}", numberOfStatementsProcessed);
						logger.info("Memory used:: {}", Utils.humanReadableByteCount(memory, true));
					}
					numberOfStatementsProcessed++;
					TripleString tripleString = (TripleString) it.next();
					String subject = tripleString.getSubject().toString();
					String object = tripleString.getObject().toString();
					addSubsumption(subject, object);
					numberOfSpecializationTriples++;
				}
			} catch (NotFoundException e) {
				e.printStackTrace();
			} catch (CompressorException e) {
				e.printStackTrace();
			}
		}
	}

	private void addSubsumption(String subject, String object) {

		logger.trace("Processing {} subsumedBy {}", subject, object);

		Long subjectIdentitySetId = esg.ID.get(subject), objectIdentitySetId = esg.ID.get(object);
		boolean subjectHasID = subjectIdentitySetId != null;
		boolean objectHasID = objectIdentitySetId != null;

		if (!objectHasID) {
			// create new identity set for the object
			objectIdentitySetId = getIdentityNewSetId();
			esg.ID.put(object, objectIdentitySetId);
			esg.IS.put(objectIdentitySetId, object);
		}

		if (!subjectHasID) {
			// create new identity set for the subject
			subjectIdentitySetId = getIdentityNewSetId();
			esg.ID.put(subject, subjectIdentitySetId);
			esg.IS.put(subjectIdentitySetId, subject);

		}

		logger.trace("Processing {} subsumedBy {}", subjectIdentitySetId, objectIdentitySetId);

		// subjectIdentitySetId subOf objectIdentitySetId
		esg.H.put(subjectIdentitySetId, objectIdentitySetId);

		// objectIdentitySetId superOf subjectIdentitySetId
		esg.H_inverse.put(objectIdentitySetId, subjectIdentitySetId);

	}

	private void updatePropropertySets() {

		{
			Long idRootEquivalent = esg.ID.get(parameters.getEquivalencePropertyToObserve());

			if (idRootEquivalent != null) {
				// get identity set of the entity
				logger.info("Equivalence set of {}:{}", parameters.getEquivalencePropertyToObserve(), idRootEquivalent);

				Set<Long> visited = new HashSet<>();
				visited.add(idRootEquivalent);

				Collection<Long> toVisit = esg.H_inverse.get(idRootEquivalent);

				while (toVisit != null && !toVisit.isEmpty()) {
					// Pick one identitySetToVisit
					long setIdToVisit = (Long) toVisit.iterator().next();
					visited.add(setIdToVisit);
					toVisit.remove(setIdToVisit);

					if (esg.H_inverse.containsKey(setIdToVisit)) {
						// the current set is super of some set
						esg.H_inverse.get(setIdToVisit).forEach(nextToVisit -> {
							if (!visited.contains(nextToVisit)) {
								toVisit.add(nextToVisit);
							}
						});
					}
				}

				visited.forEach(visitedSetId -> {
					esg.IS.get(visitedSetId).forEach(p -> {
						if (!equivalencePropertiesProcessed.contains(p)
								&& !parameters.getNotEquivalenceProperties().contains(p)) {
							equivalencePropertiesToProcess.add(p);
						}
					});
				});
			}
		}

		{
			Long idRootSubsumption = esg.ID.get(parameters.getSpecializationPropertyToObserve());

			if (idRootSubsumption != null) {

				// get identity set of the entity
				logger.info("Equivalence set of {}:{}", parameters.getSpecializationPropertyToObserve(),
						idRootSubsumption);

				Set<Long> visited = new HashSet<>();
				visited.add(idRootSubsumption);

				Collection<Long> toVisit = esg.H_inverse.get(idRootSubsumption);

				while (toVisit != null && !toVisit.isEmpty()) {

					// Pick one identitySetToVisit
					long setIdToVisit = toVisit.iterator().next();
					visited.add(setIdToVisit);
					toVisit.remove(setIdToVisit);

					if (esg.H_inverse.containsKey(setIdToVisit)) {
						// the current set is super of some set
						esg.H_inverse.get(setIdToVisit).forEach(nextToVisit -> {
							if (!visited.contains(nextToVisit)) {
								toVisit.add(nextToVisit);
							}
						});
					}
				}

				visited.forEach(visitedSetId -> {
					esg.IS.get(visitedSetId).forEach(p -> {
						if (!specializationPropertiesProcessed.contains(p)
								&& !parameters.getNotSpecializationProperties().contains(p)) {
							specializationPropertiesToProcess.add(p);
						}
					});
				});
			}
		}

	}

}
