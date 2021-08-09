package it.cnr.istc.stlab.edwin.test;

import static org.junit.Assert.assertEquals;

import java.io.File;

import org.junit.Test;

import it.cnr.istc.stlab.edwin.Edwin;
import it.cnr.istc.stlab.edwin.analysis.DatasetDistributionAnalyser;
import it.cnr.istc.stlab.edwin.model.EquivalenceSetGraph;

public class AnalyserTest {
	
	@Test
	public void testDatasetDistribution() {
		System.out.println("\n\n\n\nTEST 7\n\n\n\n");

		File f = new File("src/main/resources/testResources/t8.properties");

		TestUtils.clean();

		EquivalenceSetGraph esg = Edwin.computeESG(f.getAbsolutePath());
		
		DatasetDistributionAnalyser dda = new DatasetDistributionAnalyser(esg);
		
		assertEquals(1, dda.getNumberOfHeterogeneousNamespaces());

		esg.close();
		TestUtils.clean();
	}

}
