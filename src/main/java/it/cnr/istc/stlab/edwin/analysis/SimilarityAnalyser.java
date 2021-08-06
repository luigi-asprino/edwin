package it.cnr.istc.stlab.edwin.analysis;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.jena.ext.com.google.common.collect.Sets;
import org.rocksdb.RocksDBException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.cnr.istc.stlab.edwin.EquivalenceSetGraphLoader;
import it.cnr.istc.stlab.edwin.model.EquivalenceSetGraph;
import it.cnr.istc.stlab.lgu.commons.misc.ProgressCounter;

public class SimilarityAnalyser {

	private String esSimilarities;
	private EquivalenceSetGraph esg;
	private double threshold;

	private static final Logger logger = LoggerFactory.getLogger(SimilarityAnalyser.class);

	public SimilarityAnalyser(String esSimilarities, EquivalenceSetGraph esg, double threshold) {
		this.esSimilarities = esSimilarities;
		this.esg = esg;
		this.threshold = threshold;
	}

	public void analyse() throws IOException {
		Map<Long, Long> esIdToId = new HashMap<>();
		Map<Long, Set<Long>> esOfEss = new HashMap<>();

		BufferedReader br = new BufferedReader(new FileReader(esSimilarities));
		String line;

		ProgressCounter pc = new ProgressCounter();
		pc.setSLF4jLogger(logger);
		long esOfEsId = 0;

		while ((line = br.readLine()) != null) {
			String[] row = line.split("\t");

			Double score = Double.parseDouble(row[2]);
			pc.increase();
			if (score < threshold)
				continue;

			Long es1 = Long.parseLong(row[0]);
			Long es2 = Long.parseLong(row[1]);

			if (!esIdToId.containsKey(es1) && !esIdToId.containsKey(es2)) {
				logger.trace(" {} add new {}", line, (esOfEsId + 1));
				long newEsOfEsId = esOfEsId++;
				esIdToId.put(es1, newEsOfEsId);
				esIdToId.put(es2, newEsOfEsId);
				esOfEss.put(newEsOfEsId, Sets.newHashSet(es1, es2));
			} else if (esIdToId.containsKey(es1) && !esIdToId.containsKey(es2)) {
				logger.trace(" {} add 2  -> 1 {}", line, esIdToId.get(es1));
				long ides1 = esIdToId.get(es1);
				esIdToId.put(es2, ides1);
				esOfEss.get(ides1).add(es2);
			} else if (!esIdToId.containsKey(es1) && esIdToId.containsKey(es2)) {
				logger.trace(" {} add 1 -> 2 {}", line, esIdToId.get(es2));
				long ides2 = esIdToId.get(es2);
				esIdToId.put(es1, ides2);
				esOfEss.get(ides2).add(es1);
			} else if (esIdToId.containsKey(es1) && esIdToId.containsKey(es2)) {
				long ides2 = esIdToId.get(es2);
				long ides1 = esIdToId.get(es1);

				if (ides1 != ides2) {
					logger.trace(" {} merge 1 {} 2 {}", line, esIdToId.get(es1), esIdToId.get(es2));
					long newEsOfEsId = esOfEsId++;
					Set<Long> es1es = esOfEss.get(ides1);
					Set<Long> es2es = esOfEss.get(ides2);
					Set<Long> newEsOfEs = new HashSet<>();
					newEsOfEs.addAll(es1es);
					newEsOfEs.addAll(es2es);
					esOfEss.remove(ides1);
					esOfEss.remove(ides2);
					esIdToId.put(es1, newEsOfEsId);
					esIdToId.put(es2, newEsOfEsId);
					esOfEss.put(newEsOfEsId, newEsOfEs);
					newEsOfEs.forEach(e -> {
						esIdToId.put(e, newEsOfEsId);
					});

				} else {
					logger.trace(" {} merge skip", line, esIdToId.get(es1), esIdToId.get(es2));
				}
			}

		}

		br.close();

		logger.info("Number of ES of ESs {}", esOfEss.values().size());

		logger.info("Number of ES before merge {}", esg.getNumberOfEquivalenceSets());

		Set<Long> ESsInvolved = new HashSet<>();

		int c = 0;
		for (Set<Long> esOfEs : esOfEss.values()) {
			esg.mergeEquivalenceSets(esOfEs.toArray(new Long[esOfEs.size()]));
			ESsInvolved.addAll(esOfEs);
			logger.info("Processed {} ES of ESs", c);
		}

		logger.info("Number of ES involved {}", ESsInvolved.size());
		logger.info("Number of ES before merge {}", esg.getNumberOfEquivalenceSets());

		esg.recomputeSpecializationClosure();

	}

	public static void main(String[] args) throws RocksDBException {
		String sim = "/Users/lgu/Desktop/out_7/similarities_esg";
		String esgFilepath = "/Users/lgu/Desktop/NOTime/EKR/ESGs/classes_1";
		EquivalenceSetGraph esg = EquivalenceSetGraphLoader.loadEquivalenceSetGraphFromFolder(esgFilepath);

//		EquivalenceSetGraph esg1 = esg.cloneInto(esgFilepath + "_1");

		SimilarityAnalyser sa = new SimilarityAnalyser(sim, esg, 0.0);
		try {
			sa.analyse();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
