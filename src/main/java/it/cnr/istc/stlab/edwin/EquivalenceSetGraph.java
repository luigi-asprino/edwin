package it.cnr.istc.stlab.edwin;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

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
	private String esgFolder;
	private static Logger logger = LoggerFactory.getLogger(EquivalenceSetGraph.class);

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

}
