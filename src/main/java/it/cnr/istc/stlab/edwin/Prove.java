package it.cnr.istc.stlab.edwin;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BinaryOperator;

import org.apache.commons.compress.compressors.CompressorException;
import org.apache.jena.ext.com.google.common.collect.Sets;
import org.rdfhdt.hdt.exceptions.NotFoundException;
import org.rdfhdt.hdt.triples.TripleString;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import it.cnr.istc.stlab.lgu.commons.rdf.Dataset;

public class Prove {

	private static BiMap<CharSequence, Long> entityID = HashBiMap.create();
	private static Map<Long, Long> entityIDESID = new ConcurrentHashMap<>();
	private static AtomicLong nextIDToAssign = new AtomicLong(0L);
	private static AtomicLong ESIDtoAssign = new AtomicLong(0L);

	public static void main(String[] args) throws IOException, NotFoundException, CompressorException {
		Dataset d = Dataset.getInstanceFromFileList(
				new String[] { new File("src/main/resources/testResources/testData.nt").getAbsolutePath() });
		Map<Long, Long> es = new HashMap<>();
		BinaryOperator<Map<Long, Long>> op = (m1, m2) -> {

			Map<Long, Long> m2RemappedValues = new ConcurrentHashMap<>();

			m2.forEach((k, v) -> {
				if (m1.containsKey(k)) {
					m2RemappedValues.put(v, m1.get(k));
				} else {
					m1.put(k, v);
				}
			});

			m2.forEach((k, v) -> {
				Long remappedValue = m2RemappedValues.get(v);
				if (remappedValue != null) {
					m1.put(k, remappedValue);
				}
			});

			return m1;
		};
		d.searchStream("", "http://example.org/equal", "").parallel().map(ts -> getInitMapC(ts)).reduce(es, op);
		es.forEach((k, v) -> {
			System.out.println(getURI(k));
			System.out.println(v);
		});

		System.out.println("----");

		d.searchStream("", "http://example.org/equal2", "").parallel().map(ts -> getInitMapC(ts)).reduce(es, op);

		es.forEach((k, v) -> {
			System.out.println(getURI(k));
			System.out.println(v);
		});

//		d.searchStream("", "http://example.org/equal", "")
//				.map(ts -> Sets.newHashSet(getID(ts.getSubject()), getID(ts.getObject()))).parallel()
//				.collect(Collectors.toConcurrentMap(s -> al.getAndIncrement(), s -> s)).entrySet().parallelStream()
//				.reduce(new HashMap<Long, Set<Long>>(),
//						(e1, e2) -> !Collections.disjoint(e1.getValue(), e2.getValue())
//								? Sets.union(e1.getValue(), e2.getValue())
//								: e2);

	}

	public static Map<Long, Set<Long>> getInitMap(TripleString ts) {
		HashMap<Long, Set<Long>> result = new HashMap<>();
		Long newIS = ESIDtoAssign.getAndIncrement();
		Long idSubj = getID(ts.getSubject());
		Long idObject = getID(ts.getObject());
		entityIDESID.put(idSubj, newIS);
		entityIDESID.put(idObject, newIS);
		result.put(newIS, Sets.newHashSet(idSubj, idObject));
		return result;
	}

	public static Map<Long, Long> getInitMapC(TripleString ts) {
		HashMap<Long, Long> result = new HashMap<>();
		Long newIS = ESIDtoAssign.getAndIncrement();
		result.put(getID(ts.getSubject()), newIS);
		result.put(getID(ts.getObject()), newIS);
		return result;
	}

	public static Long getID(CharSequence uri) {
		Long result = entityID.get(uri);
		if (result != null) {
			return result;
		}
		synchronized (entityID) {
			result = nextIDToAssign.getAndIncrement();
			entityID.put(uri, result);
		}
		return result;
	}

	public static CharSequence getURI(Long uriID) {
		return entityID.inverse().get(uriID);
	}

}
