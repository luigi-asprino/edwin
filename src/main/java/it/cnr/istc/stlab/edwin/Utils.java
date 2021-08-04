package it.cnr.istc.stlab.edwin;

import java.io.File;

import org.rocksdb.BlockBasedTableConfig;
import org.rocksdb.CompactionPriority;
import org.rocksdb.LRUCache;
import org.rocksdb.Options;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;
import org.rocksdb.StringAppendOperator;
import org.rocksdb.util.SizeUnit;

public class Utils {

	public static String humanReadableByteCount(long bytes, boolean si) {
		int unit = si ? 1000 : 1024;
		if (bytes < unit)
			return bytes + " B";
		int exp = (int) (Math.log(bytes) / Math.log(unit));
		String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? "" : "i");
		return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
	}

	public static RocksDB openDB(String pathDB, int gbCache) throws RocksDBException {

		if (!new File(pathDB).exists()) {
			new File(pathDB).mkdir();
		}

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
		RocksDB db = RocksDB.open(options, pathDB);
		return db;

	}

	public static int lastIndexOf(CharSequence cs, int ch) {
		for (int i = cs.length() - 1; i >= 0; i--) {
			if (cs.charAt(i) == ch) {
				return i;
			}
		}
		return 0;
	}

	public static String cleanDatatype(final CharSequence obj, final String separator) {
		final StringBuilder sb = new StringBuilder(obj.length());
		sb.append(separator);
		sb.append('"');

		final int li = Utils.lastIndexOf(obj, '"');
		for (int i = 1; i < li; i++) {
			final char c = obj.charAt(i);
			if (c == '@' || c == '"') {
				sb.append(' ');
			} else {
				sb.append(c);
			}
		}
		sb.append('"');

		int at = 0;
		for (int i = li; i < obj.length(); i++) {
			if (obj.charAt(i) == '@') {
				at = i;
				break;
			}
		}

		if (at > 0) {
			int ap = 0;
			for (int i = li; i < obj.length(); i++) {
				if (obj.charAt(i) == '^') {
					ap = i;
					break;
				}
			}
			if (ap > 0) {
				if (at < ap) {
					sb.append(obj.subSequence(at, ap));
				} else {
					sb.append(obj.subSequence(at, obj.length()));
				}
			} else {
				sb.append(obj.subSequence(at, obj.length()));
			}
		}
		return sb.toString();

	}

}
