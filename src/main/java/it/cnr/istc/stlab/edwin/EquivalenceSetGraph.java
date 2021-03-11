package it.cnr.istc.stlab.edwin;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.jena.ext.com.google.common.collect.Sets;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.riot.system.IRIResolver;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.system.StreamRDFLib;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFWriter;
import org.eclipse.rdf4j.rio.Rio;
import org.rdfhdt.hdt.hdt.HDTManager;
import org.rdfhdt.hdt.options.HDTSpecification;
import org.rdfhdt.hdt.rdf.TripleWriter;
import org.rdfhdt.hdt.triples.TripleString;
import org.rocksdb.RocksDBException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.cnr.istc.stlab.rocksmap.RocksMap;
import it.cnr.istc.stlab.rocksmap.RocksMultiMap;
import it.cnr.istc.stlab.rocksmap.transformer.LongRocksTransformer;
import it.cnr.istc.stlab.rocksmap.transformer.StringRocksTransformer;

public final class EquivalenceSetGraph {

	RocksMap<String, Long> ID;
	RocksMap<Long, String> rootsToLeavesPaths;
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
	private AtomicLong pathId = new AtomicLong(0L);

	private EquivalenceSetGraphStats stats = new EquivalenceSetGraphStats();

	EquivalenceSetGraph(String esgFolder) throws RocksDBException {
		this.esgFolder = esgFolder;

		ID = new RocksMap<>(esgFolder + "/ID", new StringRocksTransformer(), new LongRocksTransformer());
		IS = new RocksMultiMap<>(esgFolder + "/IS", new LongRocksTransformer(), new StringRocksTransformer());
		rootsToLeavesPaths = new RocksMap<>(esgFolder + "/rootsToLeavesPaths", new LongRocksTransformer(),
				new StringRocksTransformer());
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

	public void computeRootsToLeavesPaths() {
		// check if paths have already been computed
		logger.info("Computing roots to leaves paths");
		if (rootsToLeavesPaths.containsKey(0L)) {
			logger.info("Already computed!");
			return;
		}
		this.pathId = new AtomicLong(0L);
		Set<Long> roots = getRoots();
		AtomicLong rootsComputed = new AtomicLong(0L);
		roots.parallelStream().forEach(root -> {
			if (rootsComputed.get() % 1000 == 0) {
				logger.trace("Computed paths for " + rootsComputed.get() + " roots");
			}
			dfs(root);
			rootsComputed.incrementAndGet();
		});
	}
	

	public Iterator<Long[]> listPaths() {
		Iterator<Map.Entry<Long, String>> it = rootsToLeavesPaths.iterator();
		return new Iterator<Long[]>() {

			@Override
			public boolean hasNext() {
				return it.hasNext();
			}

			@Override
			public Long[] next() {
				String[] pathString = it.next().getValue().split(" ");
				Long[] result = new Long[pathString.length];
				for (int i = 0; i < result.length; i++) {
					result[i] = Long.parseLong(pathString[i]);
				}
				return result;
			}
		};
	}

	private void dfs(Long root) {
		Set<Long> visited = Sets.newHashSet(root);
		dfsRecursive(root, visited, new ArrayList<>());
	}

	private void dfsRecursive(Long current, Set<Long> visited, List<Long> currentPath) {
		visited.add(current);
		currentPath.add(current);
		Set<Long> childs = this.getDirectSubNodes(current);
		if (childs == null || childs.isEmpty()) {

			StringBuilder sb = new StringBuilder();
			for (Long node : currentPath) {
				sb.append(node);
				sb.append(' ');
			}
			rootsToLeavesPaths.put(pathId.incrementAndGet(), sb.toString().trim());

		} else {
			for (Long child : childs) {
				if (!visited.contains(child)) {
					dfsRecursive(child, visited, new ArrayList<>(currentPath));
				}
			}
		}
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

	public Set<String> getEquivalenceSetFromNodeId(Long nodeId) {
		Collection<String> node = IS.get(nodeId);
		if (node == null) {
			return null;
		}
		return new HashSet<>(node);
	}

	public Long getNodeId(String uri) {
		return ID.get(uri);
	}

	public Collection<Long> getEquivalenceSetIds() {
		return IS.keySet();
	}

	public Set<Long> getDirectSuperNodes(Long nodeId) {
		Collection<Long> result = H.get(nodeId);
		if (result == null) {
			return new HashSet<>();
		}
		return new HashSet<>(result);
	}

	public Set<Long> getDirectSubNodes(Long nodeId) {
		Collection<Long> result = H_inverse.get(nodeId);
		if (result == null) {
			return null;
		}
		return new HashSet<>(result);
	}

	public Set<Long> getRoots() {
		return Sets.difference(IS.keySet(), H.keySet());
	}

	public Set<Long> getLeaves() {
		return Sets.difference(IS.keySet(), H_inverse.keySet());
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

	public Collection<String> getEquivalentEntities(String iri) {
		Collection<String> result = new HashSet<>();
		Long id = ID.get(iri);
		if (id != null) {
			return IS.get(id);
		}
		return result;
	}

	public long getIndirectSizeOfEntity(String iri) {
		Long id = ID.get(iri);
		if (id != null) {
			return IES.get(id);
		}
		return 0;
	}

	public long getSizeOfEntity(String iri) {
		if (oe_size.containsKey(iri)) {
			return oe_size.get(iri);
		}
		return 0;
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
		oe_size.close();
	}

	public void close() {
		// FIXME When RocksDB fix its issue
//		ID.close();
//		DES.close();
//		IES.close();
//		
//		C.close();
//		C_inverse.close();
//		IS.close();
//		oe_size.close();
//		H.close();
//		H_inverse.close();
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
		StreamRDF s = StreamRDFLib.writer(new FileOutputStream(new File(file)));

		logger.info("Triplifying ESG Graph");

		logger.info("Adding metadata to graph");
		String esgUri = base + esgName;

		s.triple(getTriple(esgUri, RDF.type.getURI(), EquivalenceSetGraphOntology.EQUIVALENCESETGRAPH));
		s.triple(getTriple(esgUri, EquivalenceSetGraphOntology.observesEquivalenceProperty,
				this.getEquivalencePropertyToObserve()));
		s.triple(getTriple(esgUri, EquivalenceSetGraphOntology.observesSpecializationProperty,
				this.getSpecializationPropertyToObserve()));
		s.triple(getTriple(esgUri, EquivalenceSetGraphOntology.equivalencePropertyForPropertiesUsed,
				this.getEquivalencePropertyForProperties()));
		s.triple(getTriple(esgUri, EquivalenceSetGraphOntology.specializationPropertyForPropertiesUsed,
				this.getSpecializationPropertyForProperties()));

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

			if (!checkIRI(entry.getKey())) {
				s.triple(getTriple(base + entry.getValue(), EquivalenceSetGraphOntology.CONTAINS, entry.getKey()));
			} else {

				String newUri = base + DigestUtils.md5Hex(entry.getKey());

				s.triple(getTriple(base + entry.getValue(), EquivalenceSetGraphOntology.CONTAINS, newUri));
				s.triple(new Triple(NodeFactory.createURI(newUri), RDFS.comment.asNode(),
						NodeFactory.createLiteral("Percent encoding of the original malformed URI " + entry.getKey())));
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

			s.triple(getTriple(base + entry.getKey(), RDF.type.getURI(), EquivalenceSetGraphOntology.NODE));
			s.triple(getTriple(esgUri, EquivalenceSetGraphOntology.HASNODE, base + entry.getKey()));

			if (applyRules) {

				Collection<Long> superNodes = H.get(entry.getKey());
				Set<String> superEntities = new HashSet<>();

				if (superNodes != null) {
					for (Long superNode : superNodes) {

						for (String s2 : IS.get(superNode)) {
							if (checkIRI(s2)) {
								s2 = base + DigestUtils.md5Hex(s2);
								;
							}
							superEntities.add(s2);
						}
					}
				}

				for (String s1 : entry.getValue()) {

					if (checkIRI(s1)) {
						s1 = base + DigestUtils.md5Hex(s1);
						;
					}

					for (String s2 : entry.getValue()) {

						if (checkIRI(s2)) {
							s2 = base + DigestUtils.md5Hex(s2);
							;
						}

						s.triple(getTriple(s1, this.equivalencePropertyToObserve, s2));
						s.triple(getTriple(s2, this.equivalencePropertyToObserve, s1));

					}

					for (String superEntity : superEntities) {
						s.triple(getTriple(s1, this.specializationPropertyToObserve, superEntity));
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
				s.triple(getTriple(base + entry.getKey(), EquivalenceSetGraphOntology.specializes, base + l));
				s.triple(getTriple(base + l, EquivalenceSetGraphOntology.isSpecializedBy, base + entry.getKey()));

			}

		}
		s.finish();
		logger.info("Specialization relation among equivalence sets Triplified");
		logger.info("Triplification completed");

	}

	public void toRDF4J(String file, String base, String esgName, boolean applyRules) throws IOException {

		RDFWriter rw = Rio.createWriter(RDFFormat.RDFXML, new FileOutputStream(new File(file)));
		ValueFactory factory = SimpleValueFactory.getInstance();
		rw.startRDF();
		logger.info("Triplifying ESG Graph");
		logger.info("Adding metadata to graph");
		String esgUri = base + esgName;

		rw.handleStatement(
				getStatement(factory, esgUri, RDF.type.getURI(), EquivalenceSetGraphOntology.EQUIVALENCESETGRAPH));
		rw.handleStatement(getStatement(factory, esgUri, EquivalenceSetGraphOntology.observesEquivalenceProperty,
				this.getEquivalencePropertyToObserve()));
		rw.handleStatement(getStatement(factory, esgUri, EquivalenceSetGraphOntology.observesSpecializationProperty,
				this.getSpecializationPropertyToObserve()));
		rw.handleStatement(
				getStatement(factory, esgUri, EquivalenceSetGraphOntology.equivalencePropertyForPropertiesUsed,
						this.getEquivalencePropertyForProperties()));
		rw.handleStatement(
				getStatement(factory, esgUri, EquivalenceSetGraphOntology.specializationPropertyForPropertiesUsed,
						this.getSpecializationPropertyForProperties()));

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

			if (!checkIRI(entry.getKey())) {
				rw.handleStatement(getStatement(factory, base + entry.getValue(), EquivalenceSetGraphOntology.CONTAINS,
						entry.getKey()));
			} else {

				String newUri = base + URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8.toString());

				rw.handleStatement(
						getStatement(factory, base + entry.getValue(), EquivalenceSetGraphOntology.CONTAINS, newUri));
				rw.handleStatement(factory.createStatement(factory.createIRI(newUri),
						factory.createIRI(RDFS.comment.getURI()),
						factory.createLiteral("Percent encoding of the original malformed URI " + entry.getKey())));
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

			rw.handleStatement(
					getStatement(factory, base + entry.getKey(), RDF.type.getURI(), EquivalenceSetGraphOntology.NODE));
			rw.handleStatement(
					getStatement(factory, esgUri, EquivalenceSetGraphOntology.HASNODE, base + entry.getKey()));

			if (applyRules) {

				Collection<Long> superNodes = H.get(entry.getKey());
				Set<String> superEntities = new HashSet<>();

				if (superNodes != null) {
					for (Long superNode : superNodes) {

						for (String s2 : IS.get(superNode)) {
							if (checkIRI(s2)) {
								s2 = base + URLEncoder.encode(s2, StandardCharsets.UTF_8.toString());
							}
							superEntities.add(s2);
						}
					}
				}

				for (String s1 : entry.getValue()) {

					if (checkIRI(s1)) {
						s1 = base + URLEncoder.encode(s1, StandardCharsets.UTF_8.toString());
					}

					for (String s2 : entry.getValue()) {

						if (checkIRI(s2)) {
							s2 = base + URLEncoder.encode(s2, StandardCharsets.UTF_8.toString());
						}

						rw.handleStatement(getStatement(factory, s1, this.equivalencePropertyToObserve, s2));
						rw.handleStatement(getStatement(factory, s2, this.equivalencePropertyToObserve, s1));

					}

					for (String superEntity : superEntities) {
						rw.handleStatement(
								getStatement(factory, s1, this.specializationPropertyToObserve, superEntity));
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
				rw.handleStatement(getStatement(factory, base + entry.getKey(), EquivalenceSetGraphOntology.specializes,
						base + l));
				rw.handleStatement(getStatement(factory, base + l, EquivalenceSetGraphOntology.isSpecializedBy,
						base + entry.getKey()));

			}

		}
		rw.endRDF();
		logger.info("Specialization relation among equivalence sets Triplified");
		logger.info("Triplification completed");

	}

	public static boolean isValidASCII(final byte[] bytes) {

		try {
			Charset.availableCharsets().get("US-ASCII").newDecoder().decode(ByteBuffer.wrap(bytes));

		} catch (CharacterCodingException e) {

			return false;
		}

		return true;
	}

	static boolean checkIRI(String iri) {

		return IRIResolver.checkIRI(iri) || iri.indexOf(':') < 0 || !iri.startsWith("http")
				|| !isValidASCII(iri.getBytes());
	}

	public void toHDT(String file, String base, String esgName, boolean applyRules) throws Exception {
		logger.info("Triplifying ESG Graph");
		logger.info("Adding metadata to graph");
		String esgUri = base + esgName;
		try (TripleWriter writer = HDTManager.getHDTWriter(file, base, new HDTSpecification())) {

			writer.addTriple(
					new TripleString(esgUri, RDF.type.getURI(), EquivalenceSetGraphOntology.EQUIVALENCESETGRAPH));
			writer.addTriple(new TripleString(esgUri, EquivalenceSetGraphOntology.observesEquivalenceProperty,
					this.getEquivalencePropertyToObserve()));
			writer.addTriple(new TripleString(esgUri, EquivalenceSetGraphOntology.observesSpecializationProperty,
					this.getSpecializationPropertyToObserve()));
			writer.addTriple(new TripleString(esgUri, EquivalenceSetGraphOntology.equivalencePropertyForPropertiesUsed,
					this.getEquivalencePropertyForProperties()));
			writer.addTriple(
					new TripleString(esgUri, EquivalenceSetGraphOntology.specializationPropertyForPropertiesUsed,
							this.getSpecializationPropertyForProperties()));
		}
		long toProcess = ID.keySet().size();
		long processed = 0;
		try (TripleWriter writer = HDTManager.getHDTWriter(file, base, new HDTSpecification())) {
			logger.info("Triplifying Observed Entities (esgs:contains)");
			Iterator<Entry<String, Long>> itID = ID.iterator();
			while (itID.hasNext()) {
				if (processed > 0 && processed % 10000 == 0) {
					logger.info("{}/{}", processed, toProcess);
				}
				processed++;
				Entry<String, Long> entry = itID.next();

				writer.addTriple(new TripleString(base + entry.getValue(), EquivalenceSetGraphOntology.CONTAINS,
						entry.getKey()));

			}
			logger.info("Observed Entities Triplified");

		}

		logger.info("Triplifying Equivalence Sets");
		Iterator<Entry<Long, Collection<String>>> itIS = IS.iterator();
		toProcess = IS.keySet().size();
		processed = 0;
		while (itIS.hasNext()) {
			if (processed > 0 && processed % 10000 == 0) {
				logger.info("{}/{}", processed, toProcess);
			}

			try (TripleWriter writer = HDTManager.getHDTWriter(file, base, new HDTSpecification())) {
				processed++;
				Entry<Long, Collection<String>> entry = itIS.next();

				writer.addTriple(
						new TripleString(base + entry.getKey(), RDF.type.getURI(), EquivalenceSetGraphOntology.NODE));
				writer.addTriple(new TripleString(esgUri, EquivalenceSetGraphOntology.HASNODE, base + entry.getKey()));

				if (applyRules) {

					Collection<Long> superNodes = H.get(entry.getKey());
					Set<String> superEntities = new HashSet<>();

					if (superNodes != null) {
						for (Long superNode : superNodes) {

							for (String s2 : IS.get(superNode)) {
								superEntities.add(s2);
							}
						}
					}

					for (String s1 : entry.getValue()) {

						for (String s2 : entry.getValue()) {

							if (IRIResolver.checkIRI(s2)) {
								s2 = base + URLEncoder.encode(s2, StandardCharsets.UTF_8.toString());
							}

							writer.addTriple(new TripleString(s1, this.equivalencePropertyToObserve, s2));
							writer.addTriple(new TripleString(s2, this.equivalencePropertyToObserve, s1));

						}

						for (String superEntity : superEntities) {
							writer.addTriple(new TripleString(s1, this.specializationPropertyToObserve, superEntity));
						}
					}
				}
			}
		}

		try (TripleWriter writer = HDTManager.getHDTWriter(file, base, new HDTSpecification())) {

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
					writer.addTriple(
							new TripleString(base + entry.getKey(), EquivalenceSetGraphOntology.specializes, base + l));

					writer.addTriple(new TripleString(base + l, EquivalenceSetGraphOntology.isSpecializedBy,
							base + entry.getKey()));

				}

			}
			logger.info("Specialization relation among equivalence sets Triplified");
			logger.info("Triplification completed");

			logger.info("Equivalence Sets Triplified!");

		}

	}

//	private static String getTripleString(String subject, String predicate, String object) {
//		StringBuilder sb = new StringBuilder();
//
//		sb.append("<");
//		sb.append(subject);
//		sb.append("> <");
//		sb.append(predicate);
//		sb.append("> <");
//		sb.append(object);
//		sb.append("> .\n");
//
//		return sb.toString();
//	}

	private static Triple getTriple(String subject, String predicate, String object) {

		return new Triple(NodeFactory.createURI(subject), NodeFactory.createURI(predicate),
				NodeFactory.createURI(object));
	}

	private static Statement getStatement(ValueFactory factory, String s, String p, String o) {
		if (checkIRI(s) || checkIRI(p) || checkIRI(o)) {
			throw new RuntimeException("Invalid");
		}
		return factory.createStatement(factory.createIRI(s), factory.createIRI(p), factory.createIRI(o));
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
