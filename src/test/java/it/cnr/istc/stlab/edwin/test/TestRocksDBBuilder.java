package it.cnr.istc.stlab.edwin.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import org.junit.Test;

import com.google.common.collect.Sets;

import it.cnr.istc.stlab.edwin.Edwin;
import it.cnr.istc.stlab.edwin.EquivalenceSetGraphAnalyser;
import it.cnr.istc.stlab.edwin.model.EquivalenceSetGraph;

public class TestRocksDBBuilder {

	private static String p(String id) {
		return "http://example.org/" + id;
	}

	@Test
	public void t1() {
		File f = new File("src/main/resources/testResources/t1.properties");

		TestUtils.clean();
		EquivalenceSetGraph esg = Edwin.computeESG(f.getAbsolutePath());

//		System.out.println(esg.getEquivalentEntities("http://example.org/a"));

		assertEquals(esg.getEquivalentEntities("http://example.org/a"), Sets.newHashSet("http://example.org/a",
				"http://example.org/c", "http://example.org/d", "http://example.org/e"));
		assertEquals(esg.getEquivalentEntities("http://example.org/c"), Sets.newHashSet("http://example.org/a",
				"http://example.org/c", "http://example.org/d", "http://example.org/e"));
		assertEquals(esg.getEquivalentEntities("http://example.org/d"), Sets.newHashSet("http://example.org/a",
				"http://example.org/c", "http://example.org/d", "http://example.org/e"));
		assertEquals(esg.getEquivalentEntities("http://example.org/e"), Sets.newHashSet("http://example.org/a",
				"http://example.org/c", "http://example.org/d", "http://example.org/e"));

		assertEquals(Sets.newHashSet("http://example.org/f", "http://example.org/f1"),
				esg.getEntitiesImplicityEquivalentToOrSubsumedBy("http://example.org/f"));
		assertEquals(Sets.newHashSet("http://example.org/f", "http://example.org/f1"),
				esg.getEntitiesImplicityEquivalentToOrSubsumedBy("http://example.org/f1"));

		assertFalse(esg.getEntitiesImplicityEquivalentToOrSubsumedBy("http://example.org/a")
				.contains("http://example.org/f"));

		assertEquals(esg.getEquivalentEntities("http://example.org/a").size(), 4);
		assertEquals(esg.getEquivalentEntities("http://example.org/f").size(), 2);
		Long es = 2L;
		Long oe = 6L;
		assertEquals(esg.getNumberOfEquivalenceSets(), es);
		assertEquals(esg.getNumberOfObservedEntities(), oe);

		esg.close();

		TestUtils.clean();
	}

	@Test
	public void t2() {
		File f = new File("src/main/resources/testResources/t2.properties");

		TestUtils.clean();
		EquivalenceSetGraph esgProperties = Edwin.computeESG(f.getAbsolutePath());

		assertEquals(esgProperties.getNumberOfEquivalenceSets(), Long.valueOf(2L));
		assertEquals(esgProperties.getEquivalentEntities("http://example.org/equal"),
				Sets.newHashSet("http://example.org/equal"));
		assertEquals(esgProperties.getEquivalentEntities("http://example.org/equal2"),
				Sets.newHashSet("http://example.org/equal2"));
		assertEquals(Sets.newHashSet("http://example.org/equal2", "http://example.org/equal"),
				esgProperties.getEntitiesImplicityEquivalentToOrSubsumedBy("http://example.org/equal"));

		esgProperties.close();
		TestUtils.clean();

	}

	@Test
	public void t3() {
		File f = new File("src/main/resources/testResources/t3.properties");

		TestUtils.clean();
		EquivalenceSetGraph esg = Edwin.computeESG(f.getAbsolutePath());
		assertEquals(Long.valueOf(3L), esg.getNumberOfEquivalenceSets());
		assertEquals(Long.valueOf(8L), esg.getNumberOfObservedEntities());

		assertEquals(esg.getEquivalentEntities(p("f")), Sets.newHashSet(p("f"), p("f1")));
		assertEquals(esg.getEquivalentEntities(p("f1")), Sets.newHashSet(p("f"), p("f1")));

		assertEquals(esg.getEquivalentEntities(p("f2")), Sets.newHashSet(p("f2"), p("f3")));
		assertEquals(esg.getEquivalentEntities(p("f3")), Sets.newHashSet(p("f2"), p("f3")));

		assertEquals(esg.getEquivalentEntities("http://example.org/a"), Sets.newHashSet("http://example.org/a",
				"http://example.org/c", "http://example.org/d", "http://example.org/e"));
		assertEquals(esg.getEquivalentEntities("http://example.org/c"), Sets.newHashSet("http://example.org/a",
				"http://example.org/c", "http://example.org/d", "http://example.org/e"));
		assertEquals(esg.getEquivalentEntities("http://example.org/d"), Sets.newHashSet("http://example.org/a",
				"http://example.org/c", "http://example.org/d", "http://example.org/e"));
		assertEquals(esg.getEquivalentEntities("http://example.org/e"), Sets.newHashSet("http://example.org/a",
				"http://example.org/c", "http://example.org/d", "http://example.org/e"));
		esg.close();
		TestUtils.clean();

	}

	@Test
	public void t4() {

		System.out.println("\n\n\n\nTEST 4\n\n\n\n");

		File f = new File("src/main/resources/testResources/t4.properties");

		TestUtils.clean();

		EquivalenceSetGraph esg = Edwin.computeESG(f.getAbsolutePath());
		assertEquals(Long.valueOf(3L), esg.getNumberOfEquivalenceSets());
		assertEquals(Long.valueOf(8L), esg.getNumberOfObservedEntities());

		assertEquals(
				Sets.newHashSet("http://example.org/f2", "http://example.org/f", "http://example.org/e",
						"http://example.org/f1", "http://example.org/f3", "http://example.org/a",
						"http://example.org/d", "http://example.org/c"),
				esg.getEntitiesImplicityEquivalentToOrSubsumedBy("http://example.org/f2"));

		assertEquals(
				Sets.newHashSet("http://example.org/f", "http://example.org/e", "http://example.org/f1",
						"http://example.org/a", "http://example.org/d", "http://example.org/c"),
				esg.getEntitiesImplicityEquivalentToOrSubsumedBy("http://example.org/f1"));

		assertEquals(
				Sets.newHashSet("http://example.org/e", "http://example.org/a", "http://example.org/d",
						"http://example.org/c"),
				esg.getEntitiesImplicityEquivalentToOrSubsumedBy("http://example.org/e"));

		esg.close();
		TestUtils.clean();

	}

	@Test
	public void t5() {

		System.out.println("\n\n\n\nTEST 5\n\n\n\n");

		File f = new File("src/main/resources/testResources/t5.properties");

		TestUtils.clean();

		EquivalenceSetGraph esg = Edwin.computeESG(f.getAbsolutePath());
		assertEquals(Long.valueOf(3L), esg.getNumberOfEquivalenceSets());
		assertEquals(Long.valueOf(8L), esg.getNumberOfObservedEntities());

		Set<String> actual = Sets.newHashSet("http://example.org/f2", "http://example.org/f", "http://example.org/e",
				"http://example.org/f1", "http://example.org/f3", "http://example.org/a", "http://example.org/d",
				"http://example.org/c");

		for (String a : actual) {
			assertEquals(actual, esg.getEntitiesImplicityEquivalentToOrSubsumedBy(a));

		}

		esg.close();
		TestUtils.clean();

	}

	@Test
	public void t6() {

		System.out.println("\n\n\n\nTEST 6\n\n\n\n");

		File f = new File("src/main/resources/testResources/t6.properties");

		TestUtils.clean();

		EquivalenceSetGraph esg = Edwin.computeESG(f.getAbsolutePath());

		try {
			EquivalenceSetGraphAnalyser.countBlankNodes(esg);
//			System.out.println(esg.getStats().bns);
			assertEquals(2L, esg.getStats().bns);
		} catch (IOException e) {
			e.printStackTrace();
		}

		esg.close();
		TestUtils.clean();

	}

	@Test
	public void t7() {

		System.out.println("\n\n\n\nTEST 7\n\n\n\n");

		File f = new File("src/main/resources/testResources/t7.properties");

		TestUtils.clean();

		EquivalenceSetGraph esg = Edwin.computeESG(f.getAbsolutePath());
		assertEquals(Long.valueOf(3L), esg.getNumberOfEquivalenceSets());
		assertEquals(Long.valueOf(8L), esg.getNumberOfObservedEntities());

		try {
			EquivalenceSetGraphAnalyser.countEquivalenceSetsHavingHeterogeneousEntities(esg);
			assertEquals(1L, esg.getStats().hetES);
		} catch (IOException e) {
			e.printStackTrace();
		}

		esg.close();
		TestUtils.clean();

	}

	@Test
	public void t8() {

		System.out.println("\n\n\n\nTEST 7\n\n\n\n");

		File f = new File("src/main/resources/testResources/t8.properties");

		TestUtils.clean();

		EquivalenceSetGraph esg = Edwin.computeESG(f.getAbsolutePath());
		assertEquals(Long.valueOf(6L), esg.getNumberOfEquivalenceSets());
		assertEquals(Long.valueOf(14L), esg.getNumberOfObservedEntities());

		esg.print();

		System.out.println("\n\nMerge\n\n");

		esg.mergeEquivalenceSets(esg.getEquivalenceSetIdOfIRI("http://example.org/f"),
				esg.getEquivalenceSetIdOfIRI("http://example.org/f2"),
				esg.getEquivalenceSetIdOfIRI("http://example.org/f6"));

		assertEquals(Long.valueOf(4L), esg.getNumberOfEquivalenceSets());
		assertEquals(Long.valueOf(14L), esg.getNumberOfObservedEntities());

		esg.print();

		assertEquals(
				Sets.newHashSet(esg.getEquivalenceSetIdOfIRI("http://example1.org/a"),
						esg.getEquivalenceSetIdOfIRI("http://example.org/f4"),
						esg.getEquivalenceSetIdOfIRI("http://example.org/f")),
				esg.getEquivalenceSetsSubsumedBy(esg.getEquivalenceSetIdOfIRI("http://example.org/f")));

		assertEquals(
				Sets.newHashSet(esg.getEquivalenceSetIdOfIRI("http://example1.org/a"),
						esg.getEquivalenceSetIdOfIRI("http://example.org/f8"),
						esg.getEquivalenceSetIdOfIRI("http://example.org/f")),
				esg.getSuperEquivalenceSets(esg.getEquivalenceSetIdOfIRI("http://example.org/f")));

		esg.close();
		TestUtils.clean();

	}

}
