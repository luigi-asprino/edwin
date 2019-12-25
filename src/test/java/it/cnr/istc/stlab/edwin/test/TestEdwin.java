package it.cnr.istc.stlab.edwin.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;

import org.apache.commons.compress.utils.Sets;
import org.junit.jupiter.api.Test;

import it.cnr.istc.stlab.edwin.Edwin;
import it.cnr.istc.stlab.edwin.EquivalenceSetGraph;

public class TestEdwin {

	@Test
	public void testEquivalenceSets() {
		File f = new File("src/main/resources/testResources/config.properties");

		EquivalenceSetGraph esg = Edwin.computeESG(f.getAbsolutePath());

		assertEquals(esg.getEquivalentOrSubsumedEntities("http://example.org/a"), Sets.newHashSet(
				"http://example.org/a", "http://example.org/c", "http://example.org/d", "http://example.org/e"));

		assertFalse(!esg.getEquivalentOrSubsumedEntities("http://example.org/a").contains("http://example.org/f"));
	}

}
