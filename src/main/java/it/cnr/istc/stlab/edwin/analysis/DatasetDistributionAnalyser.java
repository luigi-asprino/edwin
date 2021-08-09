package it.cnr.istc.stlab.edwin.analysis;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map.Entry;

import org.rocksdb.RocksDBException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.cnr.istc.stlab.edwin.EquivalenceSetGraphLoader;
import it.cnr.istc.stlab.edwin.model.EquivalenceSetGraph;
import it.cnr.istc.stlab.lgu.commons.misc.ProgressCounter;

public class DatasetDistributionAnalyser {

	private EquivalenceSetGraph esg;
	private static final Logger logger = LoggerFactory.getLogger(DatasetDistributionAnalyser.class);

	public DatasetDistributionAnalyser(EquivalenceSetGraph esg) {
		this.esg = esg;
	}

	public int getNumberOfHeterogeneousNamespaces() {
		Iterator<Entry<Long, Collection<String>>> esIt = esg.equivalenceSetsIterator();
		int count = 0;
		logger.info("Number of equivalence sets {}", esg.getNumberOfEquivalenceSets());
		ProgressCounter pc = new ProgressCounter(esg.getNumberOfEquivalenceSets());
		pc.setSLF4jLogger(logger);
		while (esIt.hasNext()) {
			Entry<Long, Collection<String>> entry = esIt.next();
			if (isFromHeterogeneousNamespace(entry.getValue())) {
				count++;
			}
			pc.increase();
		}
		logger.info("Number of heterogeneous namespace {}", count);
		return count;
	}

	private boolean isFromHeterogeneousNamespace(Collection<String> value) {
		String lastns = null;
		String ns = null;

		for (String e : value) {
			try {
				ns = new URL(e).getHost();
				if (lastns == null) {
					lastns = ns;
				} else if (!lastns.equals(ns)) {
					return true;
				}
			} catch (MalformedURLException e1) {
//				e1.printStackTrace();
				logger.trace("Malformed URL {}", e);
			}
		}

		return false;
	}

	public static void main(String[] args) {
		String esgPath = "";
		try {
			EquivalenceSetGraph esg = EquivalenceSetGraphLoader.loadEquivalenceSetGraphFromFolder(esgPath);
			DatasetDistributionAnalyser dda = new DatasetDistributionAnalyser(esg);
			dda.getNumberOfHeterogeneousNamespaces();
		} catch (RocksDBException e) {
			e.printStackTrace();
		}

	}
}
