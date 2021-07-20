package it.cnr.istc.stlab.edwin;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.jena.ext.com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.cnr.istc.stlab.edwin.model.EquivalenceSetGraph;
import it.cnr.istc.stlab.edwin.rocksdb.RocksDBEquivalenceSetGraphBuilderImpl;

public class EquivalenceSetGraphAnalyser {

	private static Logger logger = LoggerFactory.getLogger(RocksDBEquivalenceSetGraphBuilderImpl.class);

	public static void countBlankNodes(EquivalenceSetGraph esg) throws IOException {

		logger.info("Counting number of BlankNodes");
		int processed = 0;

		int numberOfBlankNodes = 0;
//		int toProcess = esg.ID.keySet().size();
		int toProcess = esg.getNumberOfObservedEntities();

		Set<Long> es_with_bn = new HashSet<>();

//		Iterator<Entry<String, Long>> iterator = esg.ID.iterator();
		Iterator<Entry<String, Long>> iterator = esg.entityIterator();
		while (iterator.hasNext()) {
			if (processed % 10000 == 0) {
				logger.info("Processed {}/{}:{}", processed, toProcess, ((double) processed / (double) toProcess));
			}
			processed++;
			Entry<String, Long> entry = (Entry<String, Long>) iterator.next();
			if (isBlankNode(entry.getKey())) {
				numberOfBlankNodes++;
				es_with_bn.add(entry.getValue());
			}
		}
		esg.getStats().bns = numberOfBlankNodes;
		esg.getStats().es_with_bns = es_with_bn.size();

	}

	public static void countObservedEntitiesWithEmptyExtesion(EquivalenceSetGraph esg) {
		logger.info("Counting Number of Observed Entities With Empty Extension");
		long result = 0;
		long result_without_bn = 0;
//		Iterator<Entry<String, Long>> it = esg.oe_size.iterator();
		Iterator<Entry<String, Long>> it = esg.observedEntitySizeIterator();
		while (it.hasNext()) {
			Entry<String, Long> e = it.next();
			if (e.getValue() == 0) {
				result++;
				if (!isBlankNode(e.getKey())) {
					result_without_bn++;
				}
			}

		}
		esg.getStats().oe0 = result;
		esg.getStats().oe0_bns = result_without_bn;
	}

	public static void computeDistributionOfExtensionalSizeOfEquivalenceSets(EquivalenceSetGraph esg) {

		logger.info("Computing Statistics on Extensional Size of Equivalence Sets");
		Map<Long, Long> result = new HashMap<>();
		Map<Long, Long> resultPerThreshold = new HashMap<>();
		Set<Long> sizeThresholds = Sets.newHashSet(1L, 10L, 100L, 1000L, 1000000L, 1000000000L);
		for (Long l : sizeThresholds) {
			resultPerThreshold.put(l, 0L);
		}
//		Iterator<Entry<Long, Long>> it = esg.IES.iterator();
		Iterator<Entry<Long, Long>> it = esg.indirectESGSizeIterator();
		while (it.hasNext()) {
			Entry<Long, Long> e = it.next();
			Long s = result.get(e.getValue());
			if (s == null) {
				result.put(e.getValue(), 1L);
			} else {
				result.put(e.getValue(), result.get(e.getValue()) + 1);
			}

			for (Long l : sizeThresholds) {
				if (e.getValue() > l) {
					resultPerThreshold.put(l, resultPerThreshold.get(l) + 1);
				}
			}
		}
		esg.getStats().indirectExtensionalSizeDistribution = result;
		esg.getStats().iesPerThreshold = resultPerThreshold;

	}

	public static void countEquivalenceSetsWithEmptyExtension(EquivalenceSetGraph esg) {

		logger.info("Count Equivalence Sets With Empty Extension");
		long result = 0;
		long result_without_bn = 0;
//		Iterator<Entry<Long, Long>> it = esg.IES.iterator();
		Iterator<Entry<Long, Long>> it = esg.indirectESGSizeIterator();
		while (it.hasNext()) {
			Entry<Long, Long> e = it.next();
			if (e.getValue() == 0) {
				result++;
//				Collection<String> ES = esg.IS.get(e.getKey());
				Collection<String> ES = esg.getEquivalenceSet(e.getKey());
				if (ES.size() == 1 && isBlankNode(ES.iterator().next())) {
					result_without_bn++;
				}
			}
		}

		esg.getStats().es0 = result;
		esg.getStats().es0bns = result - result_without_bn;

	}

	public static void exportIESDistributionAsTSV(EquivalenceSetGraph esg, String filepathTSV, double untilPercentage)
			throws IOException {
		logger.info("Exporting IES distribution as TSV {}", filepathTSV);

		Map<Long, Long> result = new HashMap<>();
//		Iterator<Entry<Long, Long>> it = esg.IES.iterator();
		Iterator<Entry<Long, Long>> it = esg.indirectESGSizeIterator();
		while (it.hasNext()) {
			Entry<Long, Long> e = it.next();
			Long s = result.get(e.getValue());
			if (s == null) {
				result.put(e.getValue(), 1L);
			} else {
				result.put(e.getValue(), result.get(e.getValue()) + 1);
			}

		}

		long max = Collections.max(result.keySet());
		logger.info("IES(0): {}", result.get(0L));
		logger.info("Max IES: {} IES({}):{}", max, max, result.get(max));
//		long totalNumberOfES = esg.IS.keySet().size();
		long totalNumberOfES = esg.getNumberOfEquivalenceSets();
		long numberOfESsWithSmallerThanCurrentSize = 0;

		FileOutputStream fos = new FileOutputStream(new File(filepathTSV));
		for (long s = 0; s <= max; s++) {
			Long n = result.get(s);
			if (n != null) {
				fos.write((s + "\t" + n + "\n").getBytes());
				numberOfESsWithSmallerThanCurrentSize += n;
				if (((double) numberOfESsWithSmallerThanCurrentSize / (double) totalNumberOfES) > untilPercentage) {
					logger.info("Size at {}:{}", untilPercentage, s);
					break;
				}
			} else {
				fos.write((s + "\t0\n").getBytes());
			}
		}
		fos.close();
		logger.info("IES distribution exported");
	}

	public static void countEdges(EquivalenceSetGraph esg) throws IOException {

		logger.info("Counting number of edges");

		long numberOfEdges = 0L;
//		Iterator<Entry<Long, Collection<Long>>> it = esg.H.iterator();
		Iterator<Entry<Long, Collection<Long>>> it = esg.subOfRelationsIterator();
		while (it.hasNext()) {
			Entry<Long, Collection<Long>> entry = it.next();
			numberOfEdges += entry.getValue().size();
		}
		esg.getStats().e = numberOfEdges;

	}

	public static void computeHeight(EquivalenceSetGraph esg) throws IOException {
		logger.info("Computing maximum height");

		int processed = 0;
		long maxHeigh = 0;
		int cycleNumber = 0;

		int toProcess = esg.getNumberOfEquivalenceSets();

		Map<Long, Long> heightMap = new HashMap<>();

		boolean stop = false;
		long lastDiff = 0;
		while (!stop) {
			long heightMapKeySize = heightMap.keySet().size();
			long currentDiff = toProcess - heightMapKeySize;

			logger.info("Cycle number :: {} max height :: {} :: {} ::{}/{} ", cycleNumber++, maxHeigh,
					(lastDiff - currentDiff), heightMapKeySize, toProcess);

			if (!stop && (lastDiff - currentDiff) == 0) {
				break;
			}

			lastDiff = currentDiff;

			stop = true;
			processed = 0;

//			Iterator<Long> iterator = Sets.difference(esg.IS.keySet(), heightMap.keySet()).iterator();
			Iterator<Long> iterator = Sets.difference(esg.getEquivalenceSetIds(), heightMap.keySet()).iterator();
			while (iterator.hasNext()) {
				if (processed % 10000 == 0) {
					logger.info("Processed {}/{}:{}", processed, toProcess, ((double) processed / (double) toProcess));
				}
				processed++;
				Long key = iterator.next();

				if (!heightMap.containsKey(key) || !checkHeightMap(heightMap, esg.getHInverseKeys(), esg)) {
					stop = false;

//					Collection<Long> subIS = esg.H_inverse.get(key);
					Set<Long> subIS = esg.getEquivalenceSetsSubsumedBy(key);
					if (subIS == null || subIS.isEmpty()) {
						// the current node is a leaf
						heightMap.put(key, 0L);
					} else {
						Long max = 0L;
						boolean stopped = false;
						for (Long sub : subIS) {

							if (sub.equals(key)) {
								// continue for self-loops
								continue;
							}

							if (!heightMap.containsKey(sub)) {
								stopped = true;
								break;
							} else {
								long currentSubHeight = heightMap.get(sub);
								if (currentSubHeight > max) {
									max = currentSubHeight;
								}
							}
						}
						if (!stopped) {
							heightMap.put(key, max + 1);

							if (maxHeigh < max + 1) {
								maxHeigh = max + 1;
							}
						}
					}
				}
			}
		}

		processed = 0;

//		Set<Long> d = Sets.difference(esg.IS.keySet(), heightMap.keySet());
		Set<Long> d = Sets.difference(esg.getEquivalenceSetIds(), heightMap.keySet());
		Iterator<Long> iterator = d.iterator();
		while (iterator.hasNext()) {
			if (processed % 10000 == 0) {
				logger.info("Processed {}/{}:{}", processed, d.size(), ((double) processed / (double) toProcess));
			}
			processed++;
			long key = iterator.next();

//			Collection<Long> subs = esg.H_inverse.get(key);
			Set<Long> subs = esg.getDirectlySubsumedEquivalenceSets(key);
			long max = 0L;
			long without = 0;
			for (long sub : subs) {
				if (heightMap.containsKey(sub)) {
					long subHeight = heightMap.get(sub);
					if (subHeight > max) {
						max = subHeight;
					}
				} else {
					without++;
				}
			}
			long keyHeight = max + without;
			heightMap.put(key, max + without);
			if (keyHeight > maxHeigh) {
				maxHeigh = keyHeight;
			}
		}

		Iterator<Map.Entry<Long, Long>> hm_it = heightMap.entrySet().iterator();
		long max = 0;
		Map<Long, Long> height_distribution = new HashMap<>();
		while (hm_it.hasNext()) {
			Map.Entry<java.lang.Long, java.lang.Long> entry = (Map.Entry<java.lang.Long, java.lang.Long>) hm_it.next();
			if (entry.getValue() > max) {
				max = entry.getValue();
			}
			if (height_distribution.containsKey(entry.getValue())) {
				height_distribution.put(entry.getValue(), height_distribution.get(entry.getValue()) + 1);
			} else {
				height_distribution.put(entry.getValue(), 1L);
			}
		}

		esg.getStats().h_max = max;
		esg.getStats().heightDistribution = height_distribution;

	}

	private static boolean checkHeightMap(Map<Long, Long> m, Set<Long> H_inverseKeys, EquivalenceSetGraph esg) {
//		for (Long l : H_inverse.keySet()) {
		for (Long l : H_inverseKeys) {

			if (m.get(l) == null)
				return false;

//			Collection<Long> subs = H_inverse.get(l);
			Set<Long> subs = esg.getEquivalenceSetsSubsumedBy(l);
			long max = 0;
			for (Long ll : subs) {
				if (ll == null) {
					return false;
				} else {
					max = Math.max(max, m.get(ll));
				}
			}

			if (m.get(l) == max + 1) {
				continue;
			}
		}
		return true;
	}

	public static void countIsoltatedEquivalenceSets(EquivalenceSetGraph esg) throws IOException {
		logger.info("Counting isolated Equivalence Sets");

		int processed = 0;
		int isolatedIS = 0;
//		int toProcess = esg.IS.keySet().size();
		int toProcess = esg.getNumberOfEquivalenceSets();

		Iterator<Entry<Long, Collection<String>>> iterator = esg.equivalenceSetsIterator();

		while (iterator.hasNext()) {
			if (processed % 10000 == 0) {
				logger.info("Processed {}/{}:{}", processed, toProcess, ((double) processed / (double) toProcess));
			}
			processed++;
			Entry<Long, Collection<String>> entry = iterator.next();

			Collection<Long> subSup = Sets.newHashSet();
//			if (esg.H.containsKey(entry.getKey())) {
			if (esg.hasSuperEquivalenceSets(entry.getKey())) {

//				subSup.addAll(esg.H.get(entry.getKey()));
				subSup.addAll(esg.getSuperEquivalenceSets(entry.getKey()));

			}
//			if (esg.H_inverse.containsKey(entry.getKey())) {
			if (esg.hasSubEquivalenceSets(entry.getKey())) {
//				subSup.addAll(esg.H_inverse.get(entry.getKey()));
				subSup.addAll(esg.getDirectlySubsumedEquivalenceSets(entry.getKey()));
			}

			subSup.remove(entry.getKey());

			if (subSup.isEmpty()) {
				isolatedIS++;
			}

		}
		esg.getStats().in = isolatedIS;

	}

	public static void countTopLevelEquivalenceSetsAndAssessEmptyNodes(EquivalenceSetGraph esg) throws IOException {
		logger.info("Counting Top Level Equivalence Sets And Assess Empty Nodes");

		int processed = 0;
		long tl = 0;
		long tlWithBN = 0;
		long oeInTL = 0;
		long bnInTL = 0;
		long tl0 = 0;
		long tl0WithBNs = 0L;
		long oeInTL0 = 0;
		long oeInTL0WithoutBNs = 0;
//		int toProcess = esg.IS.keySet().size();
		int toProcess = esg.getNumberOfEquivalenceSets();

//		Iterator<Entry<Long, Collection<String>>> iterator = esg.IS.iterator();
		Iterator<Entry<Long, Collection<String>>> iterator = esg.equivalenceSetsIterator();

		while (iterator.hasNext()) {
			if (processed % 10000 == 0) {
				logger.info("Processed {}/{}:{}", processed, toProcess, ((double) processed / (double) toProcess));
			}
			processed++;
			Entry<Long, Collection<String>> entry = iterator.next();

//			Collection<Long> superES = esg.H.get(entry.getKey());
			Collection<Long> superES = esg.getSuperEquivalenceSets(entry.getKey());

			if (superES != null)
				superES.remove(entry.getKey());

			if (superES == null || superES.isEmpty()) {

				tl++;

//				if (esg.IES.containsKey(entry.getKey()) && esg.IES.get(entry.getKey()) == 0) {

				if (esg.hasIndirectEquivalenceSetSize(entry.getKey())
						&& esg.getEquivalenceSetIndirectSize(entry.getKey()) == 0) {
					tl0++;

					for (String uri : entry.getValue()) {
						if (isBlankNode(uri)) {
							tl0WithBNs++;
							break;
						}
					}

				}

				oeInTL += entry.getValue().size();

				for (String uri : entry.getValue()) {
					if (isBlankNode(uri)) {
						tlWithBN++;
						break;
					}
				}
				for (String uri : entry.getValue()) {
					if (isBlankNode(uri)) {
						bnInTL++;
					}

//					if (esg.oe_size.containsKey(uri) && esg.oe_size.get(uri) == 0) {

					if (esg.hasObservedEntitySize(uri) && esg.getObservedEntitySize(uri) == 0) {
						oeInTL0++;
						if (!isBlankNode(uri)) {
							oeInTL0WithoutBNs++;
						}
					}
				}
			}

		}
		esg.getStats().tl = tl;
		esg.getStats().tlWithoutBNs = tl - tlWithBN;
		esg.getStats().oeInTL = oeInTL;
		esg.getStats().oeInTLWithoutBNs = oeInTL - bnInTL;
		esg.getStats().tl0 = tl0;
		esg.getStats().tl0WithoutBNs = tl0 - tl0WithBNs;
		esg.getStats().oeInTL0 = oeInTL0;
		esg.getStats().oeInTl0WithoutBN = oeInTL0WithoutBNs;

	}

	public static void countObservedEntitiesInTopLevelEquivalenceSetsWithEmptyExtesion(EquivalenceSetGraph esg)
			throws IOException {

		logger.info("Counting Observed Entities In Top Level Equivalence Sets With Empty Extesion");

		int processed = 0;
		long tl = 0;
		long tlWithBN = 0;
		long oeInTL = 0;
		long bnInTL = 0;
//		int toProcess = esg.IS.keySet().size();
		int toProcess = esg.getNumberOfEquivalenceSets();

//		Iterator<Entry<Long, Collection<String>>> iterator = esg.IS.iterator();
		Iterator<Entry<Long, Collection<String>>> iterator = esg.equivalenceSetsIterator();

		while (iterator.hasNext()) {
			if (processed % 10000 == 0) {
				logger.info("Processed {}/{}:{}", processed, toProcess, ((double) processed / (double) toProcess));
			}
			processed++;
			Entry<Long, Collection<String>> entry = iterator.next();

//			if (!esg.H.containsKey(entry.getValue())) { FIXME CHECK
			if (!esg.hasSuperEquivalenceSets(entry.getKey())) {
				tl++;
			}

			for (String uri : entry.getValue()) {
				if (isBlankNode(uri)) {
					tlWithBN++;
					break;
				}
			}

			oeInTL += entry.getValue().size();

			for (String uri : entry.getValue()) {
				if (isBlankNode(uri)) {
					bnInTL++;
				}
			}
		}
		esg.getStats().tl = tl;
		esg.getStats().tlWithoutBNs = tl - tlWithBN;
		esg.getStats().oeInTL = oeInTL;
		esg.getStats().oeInTLWithoutBNs = oeInTL - bnInTL;

	}

	private static boolean isBlankNode(String uri) {
		// Exploit skolemization of the blank nodes carried out by LOD Laundromat
		return uri.startsWith("http://lodlaundromat.org/.well-known/genid/");
	}

}
