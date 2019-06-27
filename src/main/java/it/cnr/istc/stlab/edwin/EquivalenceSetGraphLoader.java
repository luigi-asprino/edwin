package it.cnr.istc.stlab.edwin;

import org.rocksdb.RocksDBException;

public final class EquivalenceSetGraphLoader {

	private EquivalenceSetGraphLoader() {

	}

	public static EquivalenceSetGraph loadEquivalenceSetGraphFromFolder(String folder) throws RocksDBException {
		EquivalenceSetGraph esg = new EquivalenceSetGraph(folder);
		return esg;
	}

}
