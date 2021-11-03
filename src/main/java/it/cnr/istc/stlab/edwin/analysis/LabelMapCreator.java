package it.cnr.istc.stlab.edwin.analysis;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.ParseException;
import org.rdfhdt.hdt.exceptions.NotFoundException;
import org.rdfhdt.hdt.hdt.HDT;
import org.rdfhdt.hdt.hdt.HDTManager;
import org.rdfhdt.hdt.hdt.impl.HDTImpl;
import org.rdfhdt.hdt.triples.IteratorTripleString;
import org.rocksdb.BlockBasedTableConfig;
import org.rocksdb.CompactionPriority;
import org.rocksdb.FlushOptions;
import org.rocksdb.LRUCache;
import org.rocksdb.Options;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;
import org.rocksdb.StringAppendOperator;
import org.rocksdb.util.SizeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.cnr.istc.stlab.edwin.Utils;

public class LabelMapCreator {

	private static Logger logger = LoggerFactory.getLogger(LabelMapCreator.class);

	private String pathLabelProperties;
	private RocksDB db;
	private HDTImpl hdt;
	private final String SEPARATOR = " ";
	private long CHECKPOINT = 1000000;
	private Strategy strategy = Strategy.MERGE_ALL_LABELS;

	private enum Strategy {
		MERGE_ALL_LABELS, KEEP_THE_LONGEST
	}

	public LabelMapCreator(HDT hdt, String pathDB, String pathLabelProperties, int gbCache) throws RocksDBException {
		super();

		Options options = new Options();
		BlockBasedTableConfig tableOptions = new BlockBasedTableConfig();
		// table_options.block_size = 16 * 1024;
		tableOptions.setBlockSize(16 * 1024);
		// table_options.cache_index_and_filter_blocks = true;
		tableOptions.setCacheIndexAndFilterBlocks(true);
		// table_options.pin_l0_filter_and_index_blocks_in_cache = true;
		tableOptions.setPinL0FilterAndIndexBlocksInCache(true);
		// https://github.com/facebook/rocksdb/wiki/Setup-Options-and-Basic-Tuning#block-cache-size
		tableOptions.setBlockCache(new LRUCache(gbCache * SizeUnit.GB));
		//@f:off
		options.setCreateIfMissing(true)
//			.setWriteBufferSize(512 * SizeUnit.MB)
//			.setMaxWriteBufferNumber(2)
			.setIncreaseParallelism(Runtime.getRuntime().availableProcessors())
			// table options
			.setTableFormatConfig(tableOptions)
			// cf_options.level_compaction_dynamic_level_bytes = true;
			.setLevelCompactionDynamicLevelBytes(true)
			// options.max_background_compactions = 4;
//			.setMaxBackgroundCompactions(4)
			// options.max_background_flushes = 2;
//			.setMaxBackgroundFlushes(2)
			// options.bytes_per_sync = 1048576;
			.setBytesPerSync(1048576)
			// options.compaction_pri = kMinOverlappingRatio;
			.setCompactionPriority(CompactionPriority.MinOverlappingRatio);
		//@f:on
		options.setMergeOperator(new StringAppendOperator());
		new File(pathDB).mkdirs();
		db = RocksDB.open(options, pathDB);

		this.pathLabelProperties = pathLabelProperties;
		this.hdt = (HDTImpl) hdt;
//		this.pathDB = pathDB;
	}

	public void setStrategy(Strategy s) {
		if (s != null) {
			this.strategy = s;
		}
	}

	public void buildMap() throws IOException, NotFoundException, RocksDBException {

		BufferedReader br = new BufferedReader(new FileReader(new File(pathLabelProperties)));

		String property;
		long numberOfTriplesToProcess = 0;
		Set<String> predicatesToProcess = new HashSet<>();
		while ((property = br.readLine()) != null) {

			IteratorTripleString its = hdt.search("", property, "");
			long n = its.estimatedNumResults();
			if (n > 0) {
				predicatesToProcess.add(property);
				System.out.println(property + " " + n);
			}
			numberOfTriplesToProcess += n;
		}
		br.close();

		logger.info("Triples to process for building labelMap: {}", numberOfTriplesToProcess);

		long t0 = System.currentTimeMillis();
		AtomicLong numberOfTriplesProcessed = new AtomicLong(0);
		final long numOfTriplesToProcess = numberOfTriplesToProcess;
		final FlushOptions fo = new FlushOptions();
		fo.setWaitForFlush(false);

		predicatesToProcess.stream().parallel().forEach(predicate -> {

			try {
				hdt.search("", predicate, "").forEachRemaining(ts1 -> {
					if (numberOfTriplesProcessed.longValue() > 0
							&& numberOfTriplesProcessed.longValue() % CHECKPOINT == 0) {

						long t1 = System.currentTimeMillis();
						long elapsed = t1 - t0;
						double time_per_triple = ((double) elapsed) / ((double) numberOfTriplesProcessed.longValue());
						long triples_to_process = numOfTriplesToProcess - numberOfTriplesProcessed.longValue();
						long finish = t1 + ((long) (triples_to_process * time_per_triple));

						logger.info("Processing {} {}/{} :: {} ({})", predicate, numberOfTriplesProcessed,
								numOfTriplesToProcess, new Date(finish).toString(), time_per_triple);

						Runtime runtime = Runtime.getRuntime();
						try {
							db.flush(fo);
						} catch (RocksDBException e) {
							e.printStackTrace();
						}
						// Run the garbage collector
						runtime.gc();
						// Calculate the used memory
						long memory = runtime.totalMemory() - runtime.freeMemory();
						logger.info("Memory used:: {}", Utils.humanReadableByteCount(memory, true));

					}

					try {
						switch (strategy) {
						case KEEP_THE_LONGEST:
							byte[] newLabel = Utils.cleanDatatype(ts1.getObject(), SEPARATOR).toString().getBytes();
							byte[] v = db.get(ts1.getSubject().toString().getBytes());
							if (v == null || newLabel.length > v.length) {
								db.put(ts1.getSubject().toString().getBytes(), newLabel);
							}
							break;
						case MERGE_ALL_LABELS:
							db.merge(ts1.getSubject().toString().getBytes(),
									Utils.cleanDatatype(ts1.getObject(), SEPARATOR).toString().getBytes());
							break;

						}

					} catch (RocksDBException e) {
						e.printStackTrace();
					}

					numberOfTriplesProcessed.incrementAndGet();
				});

			} catch (Exception e) {
				e.printStackTrace();
			}

		});

	}

	private static final String HDT = "i";
	private static final String OUT = "o";
	private static final String PROPERTIES = "p";
	private static final String STRATEGY = "s";

	public static void main(String[] args) {

		org.apache.commons.cli.Options options = new org.apache.commons.cli.Options();

		options.addOption(org.apache.commons.cli.Option.builder(HDT).argName("input").hasArg().required(true)
				.desc("The path to the input dataset from which the label map is to be extracted.").longOpt("input")
				.build());

		options.addOption(org.apache.commons.cli.Option.builder(OUT).argName("output").hasArg().required(true)
				.desc("The path to the folder storing the RocksDB instance that will be created.").longOpt("output")
				.build());

		options.addOption(
				org.apache.commons.cli.Option.builder(PROPERTIES).argName("properties").hasArg().required(true)
						.desc("The path to the file containing a list of properties to be used to retrieve labels.")
						.longOpt("properties").build());

		options.addOption(org.apache.commons.cli.Option.builder(STRATEGY).argName("strategy").hasArg().required(false)
				.desc("The strategy of how to build the labels. Possible arguments: merge_all_labels (in this case multiple labels of an entity are merged together to form a unique label); "
						+ "keep_the_longest (in this case only the longest label will be keeped) [Default: merge_all_labels]")
				.longOpt("output").build());

		CommandLine commandLine = null;

		CommandLineParser cmdLineParser = new DefaultParser();

		try {
			commandLine = cmdLineParser.parse(options, args);

			String hdtPath = commandLine.getOptionValue(HDT);
			String labelMap = commandLine.getOptionValue(OUT);
			String properties = commandLine.getOptionValue(PROPERTIES);
			String strategy = commandLine.getOptionValue(STRATEGY);
			Strategy s = Strategy.MERGE_ALL_LABELS;
			if (strategy != null && strategy.equalsIgnoreCase("keep_the_longest")) {
				s = Strategy.KEEP_THE_LONGEST;
			}

			HDT hdt = HDTManager.mapIndexedHDT(hdtPath, null);

			LabelMapCreator lm = new LabelMapCreator(hdt, labelMap, properties, 2);
			lm.setStrategy(s);
			lm.buildMap();

		} catch (ParseException e) {
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("-i path -o path -p path [-s (keep_the_longest|merge_all_labels)]", options);
		} catch (RocksDBException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (NotFoundException e) {
			e.printStackTrace();
		}

	}

}
