package it.cnr.istc.stlab.edwin;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.jena.riot.system.IRIResolver;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.rocksdb.RocksDBException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.cnr.istc.stlab.rocksmap.RocksMap;
import it.cnr.istc.stlab.rocksmap.RocksMultiMap;
import it.cnr.istc.stlab.rocksmap.transformer.LongRocksTransformer;
import it.cnr.istc.stlab.rocksmap.transformer.StringRocksTransformer;

public final class EquivalenceSetGraph {

	RocksMap<String, Long> ID;
	RocksMultiMap<Long, String> IS;
	RocksMultiMap<Long, Long> H, H_inverse, C, C_inverse;
	RocksMap<String, Long> oe_size;
	RocksMap<Long, Long> DES, IES;
	private String esgFolder, equivalencePropertyForProperties, equivalencePropertyToObserve,
			specializationPropertyToObserve, specializationPropertyForProperties;
	private static Logger logger = LoggerFactory.getLogger(EquivalenceSetGraph.class);
	private static final String equivalencePropertyToObserveFile = "equivalencePropertyObserved",
			specializationPropertyToObserveFile = "specializationPropertyObserved",
			equivalencePropertyForPropertiesFile = "equivalencePropertyForProperties",
			specializationPropertyForPropertiesFile = "specializationPropertyForProperties";

	private static final long PROGRESS_COUNT = 10000;

	private EquivalenceSetGraphStats stats = new EquivalenceSetGraphStats();

	EquivalenceSetGraph(String esgFolder) throws RocksDBException {
		this.esgFolder = esgFolder;

		ID = new RocksMap<>(esgFolder + "/ID", new StringRocksTransformer(), new LongRocksTransformer());
		IS = new RocksMultiMap<>(esgFolder + "/IS", new LongRocksTransformer(), new StringRocksTransformer());
		H = new RocksMultiMap<>(esgFolder + "/H", new LongRocksTransformer(), new LongRocksTransformer());
		H_inverse = new RocksMultiMap<>(esgFolder + "/H_inverse", new LongRocksTransformer(),
				new LongRocksTransformer());

		C = new RocksMultiMap<>(esgFolder + "/C", new LongRocksTransformer(), new LongRocksTransformer());
		C_inverse = new RocksMultiMap<>(esgFolder + "/C_inverse", new LongRocksTransformer(),
				new LongRocksTransformer());
		oe_size = new RocksMap<>(esgFolder + "/OE_size", new StringRocksTransformer(), new LongRocksTransformer());

		DES = new RocksMap<>(esgFolder + "/DES", new LongRocksTransformer(), new LongRocksTransformer());
		IES = new RocksMap<>(esgFolder + "/IES", new LongRocksTransformer(), new LongRocksTransformer());

	}

	private void setPropertyFile(String property, String file) {
		if (!new File(esgFolder + "/" + file).exists()) {
			try {
				FileOutputStream fos = new FileOutputStream(new File(esgFolder + "/" + file));
				fos.write(property.getBytes());
				fos.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private String getPropertyFile(String file) throws IOException {
		if (new File(esgFolder + "/" + file).exists()) {
			BufferedReader br = new BufferedReader(new FileReader(new File(esgFolder + "/" + file)));
			String result = br.readLine();
			br.close();
			return result;
		}
		return null;
	}

	public void setEquivalencePropertyToObserve(String equivalencePropertyToObserve) {
		setPropertyFile(equivalencePropertyToObserve, equivalencePropertyToObserveFile);
		this.equivalencePropertyToObserve = equivalencePropertyToObserve;
	}

	public void setSpecializationPropertyToObserve(String specializationPropertyToObserve) {
		setPropertyFile(specializationPropertyToObserve, specializationPropertyToObserveFile);
		this.specializationPropertyToObserve = specializationPropertyToObserve;
	}

	public void setEquivalencePropertyForProperties(String equivalencePropertyForProperties) {
		setPropertyFile(equivalencePropertyForProperties, equivalencePropertyForPropertiesFile);
		this.equivalencePropertyForProperties = equivalencePropertyForProperties;
	}

	public void setSpecializationPropertyForProperties(String specializationPropertyToObserve) {
		setPropertyFile(specializationPropertyToObserve, specializationPropertyForPropertiesFile);
		this.specializationPropertyForProperties = specializationPropertyToObserve;
	}

	public String getEquivalencePropertyToObserve() {

		if (equivalencePropertyToObserve != null) {
			return equivalencePropertyToObserve;
		}

		String r = null;
		try {
			r = getPropertyFile(equivalencePropertyToObserveFile);
			this.equivalencePropertyToObserve = r;
		} catch (IOException e) {
			e.printStackTrace();
		}

		return r;
	}

	public String getSpecializationPropertyToObserve() {

		if (specializationPropertyToObserve != null) {
			return specializationPropertyToObserve;
		}

		String r = null;
		try {
			r = getPropertyFile(specializationPropertyToObserveFile);
			specializationPropertyToObserve = r;
		} catch (IOException e) {
			e.printStackTrace();
		}

		return r;
	}

	public String getEquivalencePropertyForProperties() {

		if (equivalencePropertyForProperties != null) {
			return equivalencePropertyForProperties;
		}

		String r = null;
		try {
			r = getPropertyFile(equivalencePropertyForPropertiesFile);
			equivalencePropertyForProperties = r;
		} catch (IOException e) {
			e.printStackTrace();
		}

		return r;

	}

	public String getSpecializationPropertyForProperties() {

		if (specializationPropertyForProperties != null) {
			return specializationPropertyForProperties;
		}

		String r = null;
		try {
			r = getPropertyFile(specializationPropertyForPropertiesFile);
			specializationPropertyForProperties = r;
		} catch (IOException e) {
			e.printStackTrace();
		}

		return r;

	}

	public void toFile() throws IOException {
		ID.toFile();
		IS.toFile();
		H.toFile();
		H_inverse.toFile();
		C.toFile();
		C_inverse.toFile();
		DES.toFile();
		IES.toFile();
	}

	public void printSimpleStats() {
		System.out.println("Equivalence Set Graph Folder: " + esgFolder);
		System.out.println("Number of observed entities: " + ID.keySet().size());
		System.out.println("Number of equivalence sets: " + IS.keySet().size());
	}

	public long getMaxId() {
		long max = 0;
		for (Long l : IS.keySet()) {
			if (l > max) {
				max = l;
			}
		}
		return max;
	}

	public long numberOfObservedEntities() {
		return ID.keySet().size();
	}

	public Set<String> getEquivalentOrSubsumedEntities(String entity) {
		Set<String> result = new HashSet<>();
		Long idRootEquivalent = ID.get(entity);

		if (idRootEquivalent != null) {
			// get identity set of the entity
			logger.trace("Equivalence set of {}:{}", entity, idRootEquivalent);

			Set<Long> visited = new HashSet<>();
			visited.add(idRootEquivalent);

			Collection<Long> toVisit = H_inverse.get(idRootEquivalent);

			while (toVisit != null && !toVisit.isEmpty()) {
				// Pick one identitySetToVisit
				long setIdToVisit = (Long) toVisit.iterator().next();
				visited.add(setIdToVisit);
				toVisit.remove(setIdToVisit);

				if (H_inverse.containsKey(setIdToVisit)) {
					// the current set is super of some set
					H_inverse.get(setIdToVisit).forEach(nextToVisit -> {
						if (!visited.contains(nextToVisit)) {
							toVisit.add(nextToVisit);
						}
					});
				}
			}

			visited.forEach(visitedSetId -> {
				result.addAll(IS.get(visitedSetId));
			});
		}

		return result;
	}

	void computeSpecializationClosure() {
		try {
			createClosure(H, C);
			createClosure(H_inverse, C_inverse);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private static void createClosure(RocksMultiMap<Long, Long> hierarchy, RocksMultiMap<Long, Long> closure)
			throws IOException {

		logger.info("Compute Closure");

		Set<Long> keys = hierarchy.keySet();
		int processed = 0;
		int size = keys.size();
		for (Long k : keys) {
			if (processed % 10000 == 0) {
				logger.info("Processed {}/{}", processed, size);
			}
			processed++;
			Collection<Long> visited = visitHierarchy(closure, hierarchy, k);
			if (!visited.isEmpty()) {
				closure.putAll(k, visited);
			}
		}
		closure.toFile();
		closure.close();

		logger.info("Closure Computed!");
	}

	private static Collection<Long> visitHierarchy(RocksMultiMap<Long, Long> closure,
			RocksMultiMap<Long, Long> hierarchy, long root) {

		Set<Long> visited = new HashSet<>();

		// initialize visited with direct descendants of root
		Collection<Long> toVisit = hierarchy.get(root);

		while (toVisit != null && !toVisit.isEmpty()) {
			long setIdToVisit = toVisit.iterator().next();
			visited.add(setIdToVisit);
			toVisit.remove(setIdToVisit);
			if (hierarchy.containsKey(setIdToVisit)) {
				hierarchy.get(setIdToVisit).forEach(nextToVisit -> {
					if (closure.containsKey(nextToVisit)) {
						// the closure for nextToVisit has been already computed
						// then we do not need to visit its children
						visited.add(nextToVisit);
						visited.addAll(closure.get(nextToVisit));
					} else if (!visited.contains(nextToVisit)) {
						toVisit.add(nextToVisit);
					}
				});
			}
		}

		if (!hierarchy.get(root).contains(root)) {
			visited.remove(root);
		}

		return visited;
	}

	public void toRDF(String file, String base, String esgName) throws IOException {
		toRDF(file, base, esgName, false);
	}

	public void toRDF(String file, String base, String esgName, boolean applyRules) throws IOException {
		FileOutputStream fos = new FileOutputStream(new File(file));

		logger.info("Triplifying ESG Graph");

		logger.info("Adding metadata to graph");
		String esgUri = base + esgName;

		fos.write(
				getTripleString(esgUri, RDF.type.getURI(), EquivalenceSetGraphOntology.EQUIVALENCESETGRAPH).getBytes());
		fos.write(getTripleString(esgUri, EquivalenceSetGraphOntology.observesEquivalenceProperty,
				this.getEquivalencePropertyToObserve()).getBytes());
		fos.write(getTripleString(esgUri, EquivalenceSetGraphOntology.observesSpecializationProperty,
				this.getSpecializationPropertyToObserve()).getBytes());
		fos.write(getTripleString(esgUri, EquivalenceSetGraphOntology.equivalencePropertyForPropertiesUsed,
				this.getEquivalencePropertyForProperties()).getBytes());
		fos.write(getTripleString(esgUri, EquivalenceSetGraphOntology.specializationPropertyForPropertiesUsed,
				this.getSpecializationPropertyForProperties()).getBytes());

		logger.info("Triplifying Observed Entities (esgs:contains)");
		Iterator<Entry<String, Long>> itID = ID.iterator();
		long toProcess = ID.keySet().size();
		long processed = 0;
		while (itID.hasNext()) {
			if (processed > 0 && processed % 10000 == 0) {
				logger.info("{}/{}", processed, toProcess);
			}
			processed++;
			Entry<String, Long> entry = itID.next();

			if (!IRIResolver.checkIRI(entry.getKey())) {
				fos.write(getTripleString(base + entry.getValue(), EquivalenceSetGraphOntology.CONTAINS, entry.getKey())
						.getBytes());
			} else {
				String newUri = base + URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8.toString());
				String malformedURITriple = "<" + newUri + "> <" + RDFS.comment.getURI()
						+ "> \"Percent encoding of the original malformed URI "
						+ entry.getKey().replaceAll("\"", "\\\\\"") + " \" .\n";
				fos.write(getTripleString(base + entry.getValue(), EquivalenceSetGraphOntology.CONTAINS, newUri)
						.getBytes());
				fos.write(malformedURITriple.getBytes());
			}

		}
		logger.info("Observed Entities Triplified");

		logger.info("Triplifying Equivalence Sets");
		Iterator<Entry<Long, Collection<String>>> itIS = IS.iterator();
		toProcess = IS.keySet().size();
		processed = 0;
		while (itIS.hasNext()) {
			if (processed > 0 && processed % 10000 == 0) {
				logger.info("{}/{}", processed, toProcess);
			}
			processed++;
			Entry<Long, Collection<String>> entry = itIS.next();

			fos.write(getTripleString(base + entry.getKey(), RDF.type.getURI(), EquivalenceSetGraphOntology.NODE)
					.getBytes());
			fos.write(getTripleString(esgUri, EquivalenceSetGraphOntology.HASNODE, base + entry.getKey()).getBytes());

			if (applyRules) {

				Collection<Long> superNodes = H.get(entry.getKey());
				Set<String> superEntities = new HashSet<>();

				if (superNodes != null) {
					for (Long superNode : superNodes) {

						for (String s2 : IS.get(superNode)) {
							if (IRIResolver.checkIRI(s2)) {
								s2 = base + URLEncoder.encode(s2, StandardCharsets.UTF_8.toString());
							}
							superEntities.add(s2);
						}
					}
				}

				for (String s1 : entry.getValue()) {

					if (IRIResolver.checkIRI(s1)) {
						s1 = base + URLEncoder.encode(s1, StandardCharsets.UTF_8.toString());
					}

					for (String s2 : entry.getValue()) {

						if (IRIResolver.checkIRI(s2)) {
							s2 = base + URLEncoder.encode(s2, StandardCharsets.UTF_8.toString());
						}

						fos.write(getTripleString(s1, this.equivalencePropertyToObserve, s2).getBytes());
						fos.write(getTripleString(s2, this.equivalencePropertyToObserve, s1).getBytes());

					}

					for (String superEntity : superEntities) {
						fos.write(getTripleString(s1, this.specializationPropertyToObserve, superEntity).getBytes());
					}
				}
			}
		}

		logger.info("Equivalence Sets Triplified!");

		logger.info("Triplifying specialization relation among equivalence sets");
		toProcess = H.keySet().size();
		processed = 0;
		Iterator<Entry<Long, Collection<Long>>> itH = H.iterator();
		while (itH.hasNext()) {
			if (processed > 0 && processed % 10000 == 0) {
				logger.info("{}/{}", processed, toProcess);
			}
			processed++;
			Entry<Long, Collection<Long>> entry = itH.next();
			for (Long l : entry.getValue()) {
				fos.write(getTripleString(base + entry.getKey(), EquivalenceSetGraphOntology.specializes, base + l)
						.getBytes());
				fos.write(getTripleString(base + l, EquivalenceSetGraphOntology.isSpecializedBy, base + entry.getKey())
						.getBytes());

			}

		}
		logger.info("Specialization relation among equivalence sets Triplified");
		logger.info("Triplification completed");

		fos.close();
	}

	private static String getTripleString(String subject, String predicate, String object) {
		StringBuilder sb = new StringBuilder();

		sb.append("<");
		sb.append(subject);
		sb.append("> <");
		sb.append(predicate);
		sb.append("> <");
		sb.append(object);
		sb.append("> .\n");

		return sb.toString();
	}

	public EquivalenceSetGraphStats getStats() {
		return stats;
	}

	public void setStats(EquivalenceSetGraphStats stats) {
		this.stats = stats;
	}

	public void saveStats() throws IOException {
		FileOutputStream fos = new FileOutputStream(new File(esgFolder + "/stats"));
		fos.write(stats.getTextualFileFormat().getBytes());
		fos.close();
	}

	public void toEdgeListNodeList(String folderOut) throws IOException {

		logger.info("Export ESG as Edge List and Node List");

		new File(folderOut).mkdirs();

		FileOutputStream fos_nodelist = new FileOutputStream(new File(folderOut + "/nodelist.tsv"));
		Iterator<Entry<Long, Collection<String>>> it_nodes = IS.iterator();
		long numberOfNodes = IS.keySet().size(), c = 0;

		while (it_nodes.hasNext()) {
			if (c > 0 && c % PROGRESS_COUNT == 0) {
				logger.info("Exported {}/{} nodes", c, numberOfNodes);
			}
			c++;
			Entry<Long, Collection<String>> entry = it_nodes.next();
			String line = String.format("%d\n", entry.getKey());
			fos_nodelist.write(line.getBytes());
		}
		fos_nodelist.flush();
		fos_nodelist.close();

		FileOutputStream fos_edgelist = new FileOutputStream(new File(folderOut + "/edgelist.tsv"));
		Iterator<Entry<Long, Collection<Long>>> it = H.iterator();
		c = 0;

		while (it.hasNext()) {
			if (c > 0 && c % PROGRESS_COUNT == 0) {
				logger.info("Exported {} edges", c);
			}
			c++;
			Entry<Long, Collection<Long>> entry = it.next();
			for (Long t : entry.getValue()) {
				String line = String.format("%d\t%d\n", entry.getKey(), t);
				fos_edgelist.write(line.getBytes());
			}
		}
		fos_edgelist.flush();
		fos_edgelist.close();

	}

}
