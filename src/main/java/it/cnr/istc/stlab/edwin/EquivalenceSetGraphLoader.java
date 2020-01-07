package it.cnr.istc.stlab.edwin;

import org.rocksdb.RocksDBException;

public final class EquivalenceSetGraphLoader {

	private EquivalenceSetGraphLoader() {

	}

	public static RocksDBBackedEquivalenceSetGraph loadEquivalenceSetGraphFromFolder(String folder) throws RocksDBException {
		RocksDBBackedEquivalenceSetGraph esg = new RocksDBBackedEquivalenceSetGraph(folder);
		return esg;
	}

}
