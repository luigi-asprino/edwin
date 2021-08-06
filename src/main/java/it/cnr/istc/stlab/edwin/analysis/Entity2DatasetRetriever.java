package it.cnr.istc.stlab.edwin.analysis;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;

import org.apache.commons.io.FilenameUtils;
import org.apache.jena.graph.Triple;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFParser;
import org.apache.jena.riot.RDFParserBuilder;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.system.StreamRDFBase;
import org.apache.jena.sparql.core.Quad;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.rdfhdt.hdt.hdt.HDT;
import org.rdfhdt.hdt.hdt.HDTManager;
import org.rdfhdt.hdt.triples.TripleID;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;

import com.google.common.collect.ImmutableSet;

import it.cnr.istc.stlab.edwin.Utils;

public class Entity2DatasetRetriever {

	private final String laundromatFolder;
	private AtomicLong processedFiles = new AtomicLong(0);
	private RocksDB outDB;
	private Set<CharSequence> processing;
	private static final Logger log = LogManager.getLogger(Entity2DatasetRetriever.class);
	private FileOutputStream fosProcessed, fosErrors;
	private RDFParserBuilder b = RDFParser.create().lang(Lang.NQUADS);

	public Entity2DatasetRetriever(RocksDB outDB, String laundromatFolder)
			throws RocksDBException, FileNotFoundException {
		super();

		this.outDB = outDB;
		this.laundromatFolder = laundromatFolder;
		this.processing = new ConcurrentSkipListSet<>();

		this.fosProcessed = new FileOutputStream("processed_" + System.currentTimeMillis() + ".txt");
		this.fosErrors = new FileOutputStream("errors" + System.currentTimeMillis() + ".txt");

	}

	public void close() throws IOException {
		this.fosErrors.flush();
		this.fosErrors.close();
		this.fosProcessed.flush();
		this.fosProcessed.close();
	}

	public void build() {
		log.info("Start");

		try {
			BufferedReader br = new BufferedReader(new FileReader("entities.txt"));
			List<String> l = br.lines().collect(Collectors.toList());
			br.close();
			ImmutableSet<String> entities = ImmutableSet.<String>builder().addAll(l).build();
			log.info("Entities loaded " + entities.size());

			ScheduledExecutorService ses = Executors.newScheduledThreadPool(1);
			ses.scheduleAtFixedRate(new Runnable() {
				@Override
				public void run() {
					log.info("Files processed " + processedFiles.get());
					log.info("Processing ");
					for (CharSequence s : processing) {
						log.info("\t" + s);
					}
				}
			}, 1, 1, TimeUnit.MINUTES);

			Files.walk(Paths.get(laundromatFolder))
					.filter(f -> FilenameUtils.isExtension(f.getFileName().toAbsolutePath().toString(), "hdt")
							|| FilenameUtils.getName(f.getFileName().toString()).endsWith("data.nq.gz"))
					.parallel().forEach(new DatasetConsumer(entities));

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	class DatasetConsumer implements Consumer<Path> {
		private ImmutableSet<String> esgEntities;

		public DatasetConsumer(ImmutableSet<String> entities) {
			this.esgEntities = entities;
		}

		@Override
		public void accept(Path t) {
			try {
				File processed = new File(t.getParent().toFile().getAbsolutePath() + "/processed11");
				log.info("Processing " + t.getFileName().toString() + " " + t.getParent().getFileName().toString() + " "
						+ processed.getName() + " " + processedFiles.get());
				if (processed.exists()) {
					log.info("Skipping " + t.getFileName().toString() + " " + t.getParent().getFileName().toString());
					return;
				}
				processing.add(t.getParent().getFileName().toString());
				Set<CharSequence> entities = new HashSet<>();
				log.trace("Filename: " + t.getFileName() + " " + t.getFileName().endsWith("hdt"));
				if (FilenameUtils.isExtension(t.toFile().getAbsolutePath(), "hdt")) {
					log.trace("Processing hdt");
					HDT hdt;
					hdt = HDTManager.mapIndexedHDT(t.toFile().getAbsolutePath(), null);
					log.trace("Size HDT " + hdt.size());

					hdt.getDictionary().getSubjects().getSortedEntries()
							.forEachRemaining(e -> collectURI(e, esgEntities, entities));
					hdt.getDictionary().getPredicates().getSortedEntries()
							.forEachRemaining(e -> collectURI(e, esgEntities, entities));
					hdt.getDictionary().getObjects().getSortedEntries()
							.forEachRemaining(e -> collectURI(e, esgEntities, entities));

					hdt.close();
				}

				if (t.getFileName().endsWith("data.nq.gz")) {
					log.trace("Processing data.nq.gz");
					StreamRDF s = new StreamRDFBase() {
						public void triple(Triple q) {
							collectTriple(q, esgEntities, entities);
						}

						public void quad(Quad q) {
							collectTriple(q.asTriple(), esgEntities, entities);
						}
					};

					try {

						InputStream is = new GZIPInputStream(new FileInputStream(t.toFile()), 20 * 1024);
						BufferedReader br = new BufferedReader(new InputStreamReader(is), 20 * 1024);
						AtomicLong al = new AtomicLong(0L);
						String l;
						StringBuilder sb = new StringBuilder();
						while ((l = br.readLine()) != null) {

							sb.append(l);
							sb.append('\n');
							al.incrementAndGet();

							if (al.get() % 10000 == 0) {
								String toParse = sb.toString();
								try {
									b.fromString(toParse).parse(s);
									log.info("Progress " + t.getParent().toFile().getAbsolutePath() + " " + al.get()
											+ " " + entities.size());
								} catch (Exception e) {
									log.error("Error line " + t.getParent().toFile().getAbsolutePath());
									for (String ll : toParse.split("\n")) {
										b.fromString(ll).parse(s);
									}
								}
								sb = new StringBuilder();
							}

						}

					} catch (Exception e) {
						log.error("Parse exception");
						e.printStackTrace();
						fosErrors.write(("Error parsing with" + t.toFile().getAbsolutePath() + "\n").getBytes());
					}

				}

				log.trace("Processed " + t.getFileName().toString() + " size entities: " + entities.size());

				for (CharSequence e : entities) {
					outDB.merge(t.getParent().getFileName().toString().getBytes(), e.toString().getBytes());
				}

				FileOutputStream fos = new FileOutputStream(processed);
				fos.write(new Date(System.currentTimeMillis()).toString().getBytes());
				fos.flush();
				fos.close();

				processing.remove(t.getParent().getFileName().toString());
				processedFiles.incrementAndGet();
				fosProcessed.write(t.toFile().getAbsolutePath().getBytes());
				fosProcessed.write('\n');

				log.info("Processed " + t.getFileName().toString() + " " + t.getParent().getFileName().toString()
						+ " num of entities " + entities.size());

			} catch (Exception e) {
				log.trace("Error while processing " + t.getParent().getFileName().toString());
				log.error(e.getMessage());
				e.printStackTrace();
				try {
					fosErrors.write(("Error processing with" + t.toFile().getAbsolutePath() + "\n").getBytes());
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		}

	}

	private void collectTriple(Triple q, ImmutableSet<String> esgEntities, Set<CharSequence> entities) {
		if (q.getSubject().isURI() && esgEntities.contains(q.getSubject().getURI())) {
			entities.add(q.getSubject().getURI());
		}

		if (q.getPredicate().isURI() && esgEntities.contains(q.getPredicate().getURI())) {
			entities.add(q.getPredicate().getURI());
		}

		if (q.getObject().isURI() && esgEntities.contains(q.getObject().getURI())) {
			entities.add(q.getObject().getURI());
		}
	}

	private void collectURI(CharSequence e, ImmutableSet<String> esgEntities, Set<CharSequence> entities) {
		log.trace("e: " + e.toString());
		if (esgEntities.contains(e.toString())) {
			entities.add(e);
		}
	}

	public static void main(String[] args) {

		log.info("Entity Retriever");
		log.info("Triple ID size " + TripleID.size());

		RocksDB dataset2entity;
		try {
			dataset2entity = Utils.openDB(args[1], 10);

			Entity2DatasetRetriever e2dr = new Entity2DatasetRetriever(dataset2entity, args[0]);

			Thread t = new Thread(() -> {
				System.out.println("Shutting down, closing db");
				dataset2entity.close();
				try {
					e2dr.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			});
			Runtime.getRuntime().addShutdownHook(t);
			e2dr.build();
			dataset2entity.close();

		} catch (RocksDBException e1) {
			e1.printStackTrace();
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
	}

}
