package it.cnr.istc.stlab.edwin.analysis;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.rocksdb.FlushOptions;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;

import it.cnr.istc.stlab.edwin.EquivalenceSetGraphLoader;
import it.cnr.istc.stlab.edwin.Utils;
import it.cnr.istc.stlab.edwin.model.EquivalenceSetGraph;
import it.cnr.istc.stlab.lgu.commons.misc.ProgressCounter;
import it.cnr.istc.stlab.lgu.commons.semanticweb.utils.URIUtils;
import it.cnr.istc.stlab.rocksmap.transformer.LongRocksTransformer;

public class ESGVirtualDocumentGenerator {

	private EquivalenceSetGraph esg;
	private RocksDB labelMapDB, virtualDocDB;
	private LongRocksTransformer longTransformer = new LongRocksTransformer();
	private static final Logger logger = LogManager.getLogger(ESGVirtualDocumentGenerator.class);

	public ESGVirtualDocumentGenerator(String esgFilepath, String labelMapFilepath, String virtualDocDBFilepath)
			throws RocksDBException {
		esg = EquivalenceSetGraphLoader.loadEquivalenceSetGraphFromFolder(esgFilepath);
		labelMapDB = Utils.openDB(labelMapFilepath, 2);
		virtualDocDB = Utils.openDB(virtualDocDBFilepath, 2);

	}

	public void computeVirtualDocumentsForNodes(long limit, boolean excludeBlankNodes) throws RocksDBException {
		logger.info("Computing virtual documents for nodes");
		Set<Long> ids = esg.getEquivalenceSetIds();
		AtomicLong documentCounter = new AtomicLong(0L);
		ProgressCounter pc = new ProgressCounter(ids.size());
		Consumer<Long> consumer = esgNodeId -> {
			try {
				String vd = getVirtualDocumentEquivalenceSet(esgNodeId, excludeBlankNodes);

				if (vd.length() > 0) {
					logger.trace("Inserting document " + esgNodeId);
					virtualDocDB.put(longTransformer.transform(esgNodeId), vd.getBytes());
					documentCounter.incrementAndGet();
				}

			} catch (RocksDBException e) {
				e.printStackTrace();
			}
			pc.increase();
		};
		if (limit > 0) {
			ids.stream().limit(limit).parallel().forEach(consumer);
		} else {
			ids.stream().parallel().forEach(consumer);
		}
		virtualDocDB.flush(new FlushOptions());
		virtualDocDB.close();
		logger.info("Completed");
		logger.info("Number of documents " + documentCounter.get());
	}

	private Set<String> getVirtualDocumentForURI(String uri, boolean excludeBlankNode) throws RocksDBException {
		Set<String> result = new HashSet<>();
		final byte[] label = labelMapDB.get(uri.getBytes());
		if (label == null) {
			if (!excludeBlankNode || !uri.startsWith("http://lodlaundromat.org/.well-known/genid/")) {
				result.add(URIUtils.getID(uri).toLowerCase());
			}
		} else {
			result.addAll(getBoW(new String(label).toLowerCase()));
		}
		return result;
	}

	private String getVirtualDocumentEquivalenceSet(Long id, boolean excludeBlankNode) throws RocksDBException {
		Set<String> tokens = new HashSet<>();

		this.esg.getEquivalenceSet(id).forEach(entity -> {
			try {
				tokens.addAll(getVirtualDocumentForURI(entity, excludeBlankNode));
			} catch (RocksDBException e) {
				e.printStackTrace();
			}
		});

		StringBuilder sb = new StringBuilder();
		for (String t : tokens) {
			sb.append(t);
			sb.append(' ');
		}
		return sb.toString();
	}

	static Pattern pattern = Pattern.compile("[A-Za-z][A-Za-z]+");

	private static Set<String> getBoW(String s) {
		Set<String> result = new HashSet<>();
		Matcher m = pattern.matcher(s);
		while (m.find()) {
			result.add(s.substring(m.start(), m.end()));
		}
		return result;
	}

	private static final String ESG_FOLDER = "i";
	private static final String LABEL_MAP_FOLDER = "l";
	private static final String OUTPUT_FOLDER = "o";
	private static final String MAX = "m";
	private static final String BN = "b";

	public static void main(String[] args) throws RocksDBException {

		Options options = new Options();

		options.addOption(Option.builder(ESG_FOLDER).argName("esg-input-folder").hasArg().required(true)
				.desc("The path of the folder storing the Equivalence Set Graph to verbalise.")
				.longOpt("esg-input-folder").build());

		options.addOption(Option.builder(LABEL_MAP_FOLDER).argName("label-map-folder").hasArg().required(true)
				.desc("The path of the folder storing the labels of the entities.").longOpt("label-map-folder")
				.build());

		options.addOption(Option.builder(OUTPUT_FOLDER).argName("output-folder").hasArg().required(true)
				.desc("The path of the folder where the virtual documents will be stored.").longOpt("output-folder")
				.build());

		options.addOption(Option.builder(MAX).argName("max").hasArg().required(false)
				.desc("The maximum number of virtual documents to generate.").longOpt("max").build());

		options.addOption(Option.builder(BN).argName("blank-nodes").required(false)
				.desc("Include skolemization of the blank nodes in the verbalization.").longOpt("blank-nodes").build());

		CommandLine commandLine = null;

		CommandLineParser cmdLineParser = new DefaultParser();

		try {
			commandLine = cmdLineParser.parse(options, args);

			String esgClasses = commandLine.getOptionValue(ESG_FOLDER);
			String labelMap = commandLine.getOptionValue(LABEL_MAP_FOLDER);
			String output = commandLine.getOptionValue(OUTPUT_FOLDER);

			boolean bn = commandLine.hasOption(BN);

			long limit = -1;

			if (commandLine.hasOption(MAX)) {
				limit = Long.parseLong(commandLine.getOptionValue(MAX));
			}

			ESGVirtualDocumentGenerator esgVDocGenerator = new ESGVirtualDocumentGenerator(esgClasses, labelMap,
					output);

			esgVDocGenerator.computeVirtualDocumentsForNodes(limit, bn);

		} catch (ParseException e) {
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("-i path -o path -l path [-m int] [-b]", options);
		}

//		String esgClasses = "/Users/lgu/Desktop/NOTime/EKR/ESGs/classes";
//		String labelMap = "/Users/lgu/Desktop/NOTime/EKR/labelMap";
//		String vDocClasses = "/Users/lgu/Desktop/NOTime/EKR/vDocClassesDB_noBN";
//		ESGVirtualDocumentGenerator esgVDocGenerator = new ESGVirtualDocumentGenerator(esgClasses, labelMap,
//				vDocClasses);
//
//		System.out.println(esgVDocGenerator.esg.getEquivalenceSet(1L));
//		System.out.println(esgVDocGenerator.esg.getEquivalenceSet(1011635L));
//		System.out.println(esgVDocGenerator.esg.getEquivalenceSet(1013251L));
//		System.out.println(esgVDocGenerator.esg.getEquivalenceSet(1080111L));
//		System.out.println(esgVDocGenerator.esg.getEquivalenceSet(1098598L));
//		System.out.println(esgVDocGenerator.esg.getEquivalenceSet(951063L));

//		System.out.println(esgVDocGenerator.esg.getEquivalenceSet(1000004L));
//		System.out.println(esgVDocGenerator.esg.getEquivalenceSet(2477842L));

//		System.out.println(esgVDocGenerator.getVirtualDocumentForURI("http://xmlns.com/foaf/0.1/Person"));

//		System.out.println(esgVDocGenerator.getVirtualDocumentEquivalenceSet(451974L));

//		System.out.println(esgVDocGenerator.getVirtualDocumentEquivalenceSet(1L, true));

//		esgVDocGenerator.computeVirtualDocumentsForNodes(-1, true);

	}

}
