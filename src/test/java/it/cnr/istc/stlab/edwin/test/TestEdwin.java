package it.cnr.istc.stlab.edwin.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.File;
import java.io.IOException;

import org.apache.commons.compress.utils.Sets;
import org.junit.jupiter.api.Test;

import it.cnr.istc.stlab.edwin.Edwin;
import it.cnr.istc.stlab.edwin.EquivalenceSetGraph;

public class TestEdwin {

	private static void clean() {
		try {
			org.apache.commons.io.FileUtils.deleteDirectory(new File("src/main/resources/testResources/ESGs"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testEquivalenceSets() {
		File f = new File("src/main/resources/testResources/testEquivalenceSets.properties");
		
		clean();

		EquivalenceSetGraph esg = Edwin.computeESG(f.getAbsolutePath());

		System.out.println(esg.getEquivalentEntities("http://example.org/a"));

		assertEquals(esg.getEquivalentEntities("http://example.org/a"), Sets.newHashSet("http://example.org/a",
				"http://example.org/c", "http://example.org/d", "http://example.org/e"));
		assertEquals(esg.getEquivalentEntities("http://example.org/c"), Sets.newHashSet("http://example.org/a",
				"http://example.org/c", "http://example.org/d", "http://example.org/e"));
		assertEquals(esg.getEquivalentEntities("http://example.org/d"), Sets.newHashSet("http://example.org/a",
				"http://example.org/c", "http://example.org/d", "http://example.org/e"));
		assertEquals(esg.getEquivalentEntities("http://example.org/e"), Sets.newHashSet("http://example.org/a",
				"http://example.org/c", "http://example.org/d", "http://example.org/e"));

		assertEquals(esg.getEquivalentOrSubsumedEntities("http://example.org/f"),
				Sets.newHashSet("http://example.org/f", "http://example.org/f1"));
		assertEquals(esg.getEquivalentOrSubsumedEntities("http://example.org/f1"),
				Sets.newHashSet("http://example.org/f", "http://example.org/f1"));

		assertFalse(esg.getEquivalentOrSubsumedEntities("http://example.org/a").contains("http://example.org/f"));

		assertEquals(esg.getEquivalentEntities("http://example.org/a").size(), 4);
		assertEquals(esg.getEquivalentEntities("http://example.org/f").size(), 2);

		assertEquals(esg.getNumberOfEquivalenceSets(), 2);
		assertEquals(esg.getNumberOfObservedEntities(), 6);
		
		clean();
	}

	@Test
	public void testEquivalenceSetsWithSpecializationOfEquivalenceRelation() {
		File f = new File(
				"src/main/resources/testResources/testEquivalenceSetsWithSpecializationOfEquivalenceRelation.properties");
		
		clean();

		EquivalenceSetGraph esgProperties = Edwin.computeESG(f.getAbsolutePath());
		assertEquals(esgProperties.getNumberOfEquivalenceSets(), 2);
		assertEquals(esgProperties.getEquivalentEntities("http://example.org/equal"),
				Sets.newHashSet("http://example.org/equal"));
		assertEquals(esgProperties.getEquivalentEntities("http://example.org/equal2"),
				Sets.newHashSet("http://example.org/equal2"));
		assertEquals(esgProperties.getEquivalentOrSubsumedEntities("http://example.org/equal"),
				Sets.newHashSet("http://example.org/equal2", "http://example.org/equal"));
		
		clean();

	}

}
