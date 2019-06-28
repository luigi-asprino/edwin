package it.cnr.istc.stlab.edwin;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.jena.vocabulary.RDF;
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
	private String esgFolder, equivalencePropertyForProperties, equivalencePropertyToObserve,
			specializationPropertyToObserve, specializationPropertyForProperties;
	private static Logger logger = LoggerFactory.getLogger(EquivalenceSetGraph.class);
	private static final String equivalencePropertyToObserveFile = "equivalencePropertyObserved",
			specializationPropertyToObserveFile = "specializationPropertyObserved",
			equivalencePropertyForPropertiesFile = "equivalencePropertyForProperties",
			specializationPropertyForPropertiesFile = "specializationPropertyForProperties";

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

	}

	private void setPropertyFile(String property, String file) {
		if (!new File(esgFolder + file).exists()) {
			try {
				FileOutputStream fos = new FileOutputStream(new File(esgFolder + file));
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
		if (new File(esgFolder + file).exists()) {
			BufferedReader br = new BufferedReader(new FileReader(new File(file)));
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
		FileOutputStream fos = new FileOutputStream(new File(file));

		String esgUri = base + esgName;

		fos.write(
				getTripleString(esgUri, RDF.type.getURI(), EquivalenceSetGraphOntology.EQUIVALENCESETGRAPH).getBytes());
		fos.write(getTripleString(esgUri, EquivalenceSetGraphOntology.observesEquivalenceProperty,
				this.equivalencePropertyToObserve).getBytes());
		fos.write(getTripleString(esgUri, EquivalenceSetGraphOntology.observesSpecializationProperty,
				this.specializationPropertyToObserve).getBytes());
		fos.write(getTripleString(esgUri, EquivalenceSetGraphOntology.equivalencePropertyForPropertiesUsed,
				this.equivalencePropertyForProperties).getBytes());
		fos.write(getTripleString(esgUri, EquivalenceSetGraphOntology.specializationPropertyForPropertiesUsed,
				this.specializationPropertyForProperties).getBytes());

		Iterator<Entry<String, Long>> itID = ID.iterator();
		while (itID.hasNext()) {
			Entry<String, Long> entry = itID.next();
			fos.write(getTripleString(base + entry.getKey(), RDF.type.getURI(), EquivalenceSetGraphOntology.NODE)
					.getBytes());
			fos.write(getTripleString(esgUri, EquivalenceSetGraphOntology.HASNODE, base + entry.getValue()).getBytes());
			fos.write(getTripleString(base + entry.getValue(), EquivalenceSetGraphOntology.CONTAINS, entry.getKey())
					.getBytes());
		}

		Iterator<Entry<Long, Collection<String>>> itIS = IS.iterator();
		while (itIS.hasNext()) {
			Entry<Long, Collection<String>> entry = itIS.next();
			for (String s1 : entry.getValue()) {
				for (String s2 : entry.getValue()) {
					fos.write(getTripleString(s1, this.equivalencePropertyToObserve, s2).getBytes());
					fos.write(getTripleString(s2, this.equivalencePropertyToObserve, s1).getBytes());
				}
			}
		}

		Iterator<Entry<Long, Collection<Long>>> itH = H.iterator();
		while (itH.hasNext()) {
			Entry<Long, Collection<Long>> entry = itH.next();
			for (Long l : entry.getValue()) {
				fos.write(getTripleString(base + entry.getKey(), EquivalenceSetGraphOntology.specializes, base + l)
						.getBytes());
				fos.write(getTripleString(base + l, EquivalenceSetGraphOntology.isSpecializedBy, base + entry.getKey())
						.getBytes());
			}

		}

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

}
