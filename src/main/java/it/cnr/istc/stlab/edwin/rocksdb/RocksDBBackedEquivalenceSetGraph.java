package it.cnr.istc.stlab.edwin.rocksdb;

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
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
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

import it.cnr.istc.stlab.edwin.EquivalenceSetGraphOntology;
import it.cnr.istc.stlab.edwin.EquivalenceSetGraphStats;
import it.cnr.istc.stlab.edwin.model.EquivalenceSetGraph;
import it.cnr.istc.stlab.rocksmap.RocksMap;
import it.cnr.istc.stlab.rocksmap.RocksMultiMap;
import it.cnr.istc.stlab.rocksmap.transformer.LongRocksTransformer;
import it.cnr.istc.stlab.rocksmap.transformer.StringRocksTransformer;

public final class RocksDBBackedEquivalenceSetGraph implements EquivalenceSetGraph {

	RocksMap<String, Long> ID;
	RocksMultiMap<Long, String> IS;
	RocksMultiMap<Long, Long> H, H_inverse, C, C_inverse;
	RocksMap<String, Long> oe_size;
	RocksMap<Long, Long> DES, IES;
	private String esgFolder, equivalencePropertyForProperties, equivalencePropertyToObserve,
			specializationPropertyToObserve, specializationPropertyForProperties;
	private static Logger logger = LoggerFactory.getLogger(RocksDBBackedEquivalenceSetGraph.class);
	private static final String equivalencePropertyToObserveFile = "equivalencePropertyObserved",
			specializationPropertyToObserveFile = "specializationPropertyObserved",
			equivalencePropertyForPropertiesFile = "equivalencePropertyForProperties",
			specializationPropertyForPropertiesFile = "specializationPropertyForProperties";

	private String C_path, C_inversePath;

	private static final long PROGRESS_COUNT = 10000;

	private EquivalenceSetGraphStats stats = new EquivalenceSetGraphStats();

	public RocksDBBackedEquivalenceSetGraph(String esgFolder) throws RocksDBException {
		this.esgFolder = esgFolder;
		ID = new RocksMap<>(esgFolder + "/ID", new StringRocksTransformer(), new LongRocksTransformer());
		IS = new RocksMultiMap<>(esgFolder + "/IS", new LongRocksTransformer(), new StringRocksTransformer());
		H = new RocksMultiMap<>(esgFolder + "/H", new LongRocksTransformer(), new LongRocksTransformer());
		H_inverse = new RocksMultiMap<>(esgFolder + "/H_inverse", new LongRocksTransformer(),
				new LongRocksTransformer());
		C_path = esgFolder + "/C";
		C = new RocksMultiMap<>(C_path, new LongRocksTransformer(), new LongRocksTransformer());
		C_inversePath = esgFolder + "/C_inverse";
		C_inverse = new RocksMultiMap<>(C_inversePath, new LongRocksTransformer(), new LongRocksTransformer());
		oe_size = new RocksMap<>(esgFolder + "/OE_size", new StringRocksTransformer(), new LongRocksTransformer());
		DES = new RocksMap<>(esgFolder + "/DES", new LongRocksTransformer(), new LongRocksTransformer());
		IES = new RocksMap<>(esgFolder + "/IES", new LongRocksTransformer(), new LongRocksTransformer());
	}

	private void setPropertyFile(String property, String file) {
		if (!new File(esgFolder + "/" + file).exists() && property != null) {
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

	public Set<String> getEquivalentEntities(String iri) {
		Set<String> result = new HashSet<>();
		Long id = ID.get(iri);
		if (id != null) {
			result.addAll(IS.get(id));
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
		oe_size.toFile();
	}

	public void close() {
		// FIXME
		ID.close();
		DES.close();
		IES.close();
		C.close();
		C_inverse.close();
		IS.close();
		oe_size.close();
		H.close();
		H_inverse.close();
	}

	public void printSimpleStats() {
		System.out.println("Equivalence Set Graph Folder: " + esgFolder);
		System.out.println("Number of observed entities: " + ID.keySet().size());
		System.out.println("Number of equivalence sets: " + IS.keySet().size());
	}

	public Long getNumberOfObservedEntities() {
		return (long) ID.keySet().size();
	}

	public Long getNumberOfEquivalenceSets() {
		return (long) IS.keySet().size();
	}

	@Override
	public Long getMaxId() {
		long max = 0;
		for (Long l : IS.keySet()) {
			if (l > max) {
				max = l;
			}
		}
		return max;
	}

	public Set<String> getEntitiesImplicityEquivalentToOrSubsumedBy(String entity) {
		return getEntitiesImplicityEquivalentToOrSubsumedBy(entity, false);
	}

	@Override
	public Set<String> getEntitiesImplicityEquivalentToOrSubsumedBy(String entity, boolean useClosure, int maxDepth) {
		Long idEntity;
		if (entity == null || (idEntity = ID.get(entity)) == null) {
			return new HashSet<>();
		}

		Set<String> result = new HashSet<>(IS.get(idEntity));

		// get identity set of the entity
		logger.trace("Equivalence set of {}:{}", entity, idEntity);

		if (maxDepth < 0) {
			if (useClosure) {
				Collection<Long> c = C_inverse.get(idEntity);
				logger.trace("{} -> {}", idEntity, c);
				if (c != null) {
					c.forEach(l -> {
						result.addAll(IS.get(l));
					});
				}
			} else {
				Set<Long> visited = new HashSet<>();
				visited.add(idEntity);

				Collection<Long> toVisit = H_inverse.get(idEntity);

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
		} else {

			Set<Long> visited = new HashSet<>();
			visited.add(idEntity);
			Set<Long> hierarchyRetrieved = new HashSet<>();
			for (int i = 0; i < maxDepth; i++) {
				Set<Long> toAdd = new HashSet<>();
				Sets.difference(visited, hierarchyRetrieved).forEach(idEquivalentSet -> {
					if (!hierarchyRetrieved.contains(idEquivalentSet)) {
						Collection<Long> subSets = H_inverse.get(idEquivalentSet);
						if (subSets != null) {
							toAdd.addAll(subSets);
						}
						hierarchyRetrieved.add(idEquivalentSet);
					}
				});
				visited.addAll(toAdd);
			}

			visited.forEach(visitedSetId -> {
				result.addAll(IS.get(visitedSetId));
			});

		}

		return result;
	}

	public Set<String> getEntitiesImplicityEquivalentToOrSubsumedBy(String entity, boolean useClosure) {
		return getEntitiesImplicityEquivalentToOrSubsumedBy(entity, useClosure, -1);
	}

	void computeSpecializationClosure() {
		try {
			logger.trace("Compute closure");
			createClosure(H, C);
			logger.trace("Compute closure inverse");
			createClosure(H_inverse, C_inverse);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private void createClosure(RocksMultiMap<Long, Long> hierarchy, RocksMultiMap<Long, Long> closure)
			throws IOException {

		Set<Long> keys = hierarchy.keySet();

		AtomicLong processed = new AtomicLong(0L);
		final int size = keys.size();
		logger.trace("Entries to process {}", size);
		keys.parallelStream().forEach(k -> {
			if (processed.get() % 10000 == 0) {
				logger.info("Processed {}/{}", processed, size);
			}
			processed.incrementAndGet();

			if (!closure.containsKey(k)) {
				Collection<Long> visited = visitHierarchy(closure, hierarchy, k);
				if (!visited.isEmpty()) {
					closure.putAll(k, visited);
				}
			}
		});

//		closure.toFile();
//		closure.close();

		logger.info("Closure Computed!");
	}

	private Collection<Long> visitHierarchy(RocksMultiMap<Long, Long> closure, RocksMultiMap<Long, Long> hierarchy,
			long root) {

		Set<Long> visited = new HashSet<>();

		// initialize visited with direct descendants of root
		Collection<Long> toVisit = hierarchy.get(root);

		while (toVisit != null && !toVisit.isEmpty()) {
			long setIdToVisit = toVisit.iterator().next();
			visited.add(setIdToVisit);
			toVisit.remove(setIdToVisit);
			if (hierarchy.containsKey(setIdToVisit)) {
				hierarchy.get(setIdToVisit).parallelStream().forEach(nextToVisit -> {
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
		logger.trace("Exporting node list");
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
			fos_nodelist.write(String.format("%d\n", entry.getKey()).getBytes());
		}
		logger.trace("Closing");
		fos_nodelist.flush();
		fos_nodelist.close();

		logger.trace("Node list exported");
		logger.trace("Exporting edge list");
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

		logger.trace("Edge list exported");

		logger.info("Exported");

	}

	@Override
	public boolean hasEquivalenceSet(String iri) {
		return ID.containsKey(iri);
	}

	@Override
	public Set<String> getEquivalenceSet(Long visitedSetId) {
		return new HashSet<>(IS.get(visitedSetId));
	}

	@Override
	public Long getEquivalenceSetIdOfIRI(String iri) {
		return ID.get(iri);
	}

	@Override
	public Set<Long> getEquivalenceSetsSubsumedBy(Long equivalenceSetID) {
		Collection<Long> subIds = H_inverse.get(equivalenceSetID);
		if (subIds != null)
			return new HashSet<>(subIds);
		return new HashSet<>();

	}

	@Override
	public Set<Long> getSuperEquivalenceSets(Long equivalenceSetID) {
		Collection<Long> superIds = H.get(equivalenceSetID);
		if (superIds != null)
			return new HashSet<>(superIds);
		else
			return new HashSet<>();
	}

	@Override
	public void addSpecialization(String s, String o) {

		Long sId = ID.get(s);
		Long oId = ID.get(o);

		if (sId != null && oId != null) {
			H.put(sId, oId);
			H_inverse.put(oId, sId);
		}

	}

	@Override
	public Set<Long> getEquivalenceSetIds() {
		return IS.keySet();
	}

	@Override
	public Set<Long> getTopLevelEquivalenceSets() {
		Set<Long> result = new HashSet<Long>(IS.keySet());
		result.removeAll(H.keySet());
		return result;
	}

	@Override
	public Set<String> getEquivalenceSet(String iri) {
		Long equivalenceSetId = ID.get(iri);
		if (equivalenceSetId == null) {
			return null;
		}
		return new HashSet<>(IS.get(equivalenceSetId));
	}

	@Override
	public Set<String> getSuperEquivalenceSets(String iri) {
		Long equivalenceSetId = ID.get(iri);
		if (equivalenceSetId == null) {
			return null;
		}
		Set<Long> superEquivalenceSets = Sets.newHashSet(H.get(equivalenceSetId));
		Set<String> result = new HashSet<>();

		for (Long superEquivalenceSet : superEquivalenceSets) {
			result.addAll(IS.get(superEquivalenceSet));
		}

		return result;

	}

	@Override
	public Set<String> getEntities() {
		return ID.keySet();
	}

	@Override
	public Long getEntityDirectExtensionalSize(String entityURI) {
		return oe_size.get(entityURI);
	}

	@Override
	public Long getEntityIndirectExtensionalSize(String entityURI) {
		return IES.get(getEquivalenceSetIdOfIRI(entityURI));
	}

	@Override
	public Long getEquivalenceSetDirectSize(Long equivalenceSetId) {
		return DES.get(equivalenceSetId);
	}

	@Override
	public Long getEquivalenceSetIndirectSize(Long equivalenceSetId) {
		return IES.get(equivalenceSetId);
	}

	@Override
	public Set<Long> getEmptyEquivalenceSets() {
		Set<Long> result = new HashSet<>();
		Iterator<Entry<Long, Long>> it = IES.iterator();
		while (it.hasNext()) {
			Entry<Long, Long> entry = it.next();
			if (entry.getValue() == 0) {
				result.add(entry.getKey());
			}
		}
		return result;
	}

	@Override
	public boolean containsEntity(String uri) {
		return ID.containsKey(uri);
	}

	@Override
	public void createSingleEntityEquivalenceSet(String uri, Long id) {
		ID.put(uri, id);
		IS.put(id, uri);
	}

	@Override
	public Set<Long> getIndirectlySubsumedEquivalenceSets(Long es) {
		Collection<Long> r = C_inverse.get(es);
		if (r != null)
			return new HashSet<>(r);
		return new HashSet<>();
	}

	@Override
	public void setEquivalenceSetIndirectSize(Long esId, Long size) {
		IES.put(esId, size);
	}

	@Override
	public void setEquivalenceSetDirectSize(Long esId, Long size) {
		DES.put(esId, size);
	}

	@Override
	public Long getOESize(String entityURI) {
		return oe_size.get(entityURI);
	}

	@Override
	public Iterator<Entry<Long, Collection<String>>> equivalenceSetsIterator() {
		return IS.iterator();
	}

	@Override
	public Iterator<Entry<String, Long>> entityIterator() {
		return ID.entryIterator();
	}

	@Override
	public void setObservedEntitySize(String key, long size) {
		this.oe_size.put(key, size);
	}

	@Override
	public Iterator<Entry<String, Long>> observedEntitySizeIterator() {
		return oe_size.iterator();
	}

	@Override
	public Iterator<Entry<Long, Long>> indirectESGSizeIterator() {
		return IES.iterator();
	}

	@Override
	public Iterator<Entry<Long, Collection<Long>>> subOfRelationsIterator() {
		return H.iterator();
	}

	@Override
	public Set<Long> getHInverseKeys() {
		return H_inverse.keySet();
	}

	@Override
	public boolean hasSuperEquivalenceSets(Long key) {
		return H.containsKey(key);
	}

	@Override
	public boolean hasSubEquivalenceSets(Long key) {
		return H_inverse.containsKey(key);
	}

	@Override
	public boolean hasIndirectEquivalenceSetSize(Long key) {
		return IES.containsKey(key);
	}

	@Override
	public boolean hasObservedEntitySize(String uri) {
		return oe_size.containsKey(uri);
	}

	@Override
	public Long getObservedEntitySize(String uri) {
		return oe_size.get(uri);
	}

	@Override
	public synchronized void mergeEquivalenceSets(Long... idsToMerge) {

		if (idsToMerge.length < 2)
			return;

		Long newId = getMaxId() + 1;

		Set<String> newEquivalenceSet = new HashSet<>();
		Set<Long> superEquivanceSets = new HashSet<>();
		Set<Long> subEquivanceSets = new HashSet<>();

		for (Long id : idsToMerge) {
			newEquivalenceSet.addAll(this.getEquivalenceSet(id));
			IS.removeAll(id);
			Collection<Long> superIds = H.get(id);
			if (superIds != null) {
				superEquivanceSets.addAll(superIds);
				superIds.forEach(h -> {
					Collection<Long> hSubIds = H_inverse.get(h);
					if (hSubIds != null) {
						hSubIds.add(newId);
						hSubIds.remove(id);
						H_inverse.putAll(h, hSubIds);
					}
				});
				H.removeAll(id);
			}

			Collection<Long> subIds = H_inverse.get(id);
			if (subIds != null) {
				subEquivanceSets.addAll(subIds);
				subIds.forEach(h_1 -> {
					Collection<Long> hSuperIds = H.get(h_1);
					if (hSuperIds != null) {
						hSuperIds.add(newId);
						hSuperIds.remove(id);
						H.putAll(h_1, hSuperIds);
					}
				});
				H_inverse.removeAll(id);
			}

			if (superEquivanceSets.contains(id)) {
				superEquivanceSets.remove(id);
				superEquivanceSets.add(newId);
			}

			if (subEquivanceSets.contains(id)) {
				subEquivanceSets.remove(id);
				subEquivanceSets.add(id);
			}
		}

		for (String e : newEquivalenceSet) {
			ID.put(e, newId);
		}

		IS.putAll(newId, newEquivalenceSet);

		H.putAll(newId, superEquivanceSets);
		H_inverse.putAll(newId, subEquivanceSets);

	}

	@Override
	public EquivalenceSetGraph cloneInto(String path) {
		try {
			RocksDBBackedEquivalenceSetGraph result = new RocksDBBackedEquivalenceSetGraph(path);
			logger.info("Copying ID map");
			this.ID.iterator().forEachRemaining(e -> result.ID.put(e.getKey(), e.getValue()));
			logger.info("Copying ES map");
			this.IS.iterator().forEachRemaining(e -> result.IS.putAll(e.getKey(), e.getValue()));
			logger.info("Copying H map");
			this.H.iterator().forEachRemaining(e -> result.H.putAll(e.getKey(), e.getValue()));
			logger.info("Copying H_inverse map");
			this.H_inverse.iterator().forEachRemaining(e -> result.H_inverse.putAll(e.getKey(), e.getValue()));
			logger.info("Copying C map");
			this.C.iterator().forEachRemaining(e -> result.C.putAll(e.getKey(), e.getValue()));
			logger.info("Copying C_inverse map");
			this.C_inverse.iterator().forEachRemaining(e -> result.C_inverse.putAll(e.getKey(), e.getValue()));
			logger.info("Copying OE Size map");
			this.oe_size.iterator().forEachRemaining(e -> result.oe_size.put(e.getKey(), e.getValue()));
			logger.info("Copying DES map");
			this.DES.iterator().forEachRemaining(e -> result.DES.put(e.getKey(), e.getValue()));
			logger.info("Copying IES map");
			this.DES.iterator().forEachRemaining(e -> result.DES.put(e.getKey(), e.getValue()));

			result.setEquivalencePropertyForProperties(this.getEquivalencePropertyForProperties());
			result.setSpecializationPropertyForProperties(this.getSpecializationPropertyForProperties());
			result.setEquivalencePropertyToObserve(this.getEquivalencePropertyToObserve());
			result.setSpecializationPropertyToObserve(result.getSpecializationPropertyToObserve());

			logger.info("Cloned");

		} catch (RocksDBException e) {
			e.printStackTrace();
		}

		return null;

	}

	@Override
	public void recomputeSpecializationClosure() {
		logger.info("Recompute specialization closure");
		try {
			org.apache.commons.io.FileUtils.deleteDirectory(new File(C_path));
			org.apache.commons.io.FileUtils.deleteDirectory(new File(C_inversePath));
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		computeSpecializationClosure();

	}

}
