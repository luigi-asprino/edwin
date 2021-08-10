package it.cnr.istc.stlab.edwin;

import java.io.File;
import java.io.IOException;

import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.rdfhdt.hdt.exceptions.NotFoundException;
import org.rocksdb.RocksDBException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.cnr.istc.stlab.edwin.analysis.EquivalenceSetGraphAnalyser;
import it.cnr.istc.stlab.edwin.model.EquivalenceSetGraph;
import it.cnr.istc.stlab.edwin.rocksdb.RocksDBBackedEquivalenceSetGraph;

public class EdwinTests {

	private static Logger logger = LoggerFactory.getLogger(Edwin.class);

	public static void main(String[] args) {
		try {

			logger.info("Edwin Tests v0.0.1");

			String configFile = "config.properties";

			if (args.length > 0) {
				configFile = args[0];
			}
			Configurations configs = new Configurations();
			Configuration config = configs.properties(configFile);

			computeForTest();

			boolean computeESG = false, oeSizeClasses = false, testRDF = false, testCC = false, testStats = false,
					testStats2 = false, query = false;

			if (computeESG)
				computeEsg(configFile);

			if (testRDF) {
				testRDF(config);
			}

			if (testCC)
				testCC(config);

			if (testStats) {
				testStats(config);
			}

			if (oeSizeClasses) {
				estimateExtensionalSize(config);
			}

			if (testStats2) {
				testStats("/Users/lgu/Desktop/demo/1571670035414");
			}

			if (query) {
				query();
			}

		} catch (ConfigurationException e) {
			e.printStackTrace();
		} catch (RocksDBException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void computeForTest() throws IOException, NotFoundException, CompressorException {
		File f = new File("src/main/resources/testResources/t1.properties");

		Edwin.computeESG(f.getAbsolutePath());

		org.apache.commons.io.FileUtils.deleteDirectory(new File("src/main/resources/testResources/ESGs"));
	}

	private static void computeEsg(String configPath) throws IOException, NotFoundException, CompressorException {
		Edwin.computeESG(configPath);

	}

	private static void testStats(Configuration config) throws RocksDBException, IOException {

		EquivalenceSetGraph esg = EquivalenceSetGraphLoader
				.loadEquivalenceSetGraphFromFolder(config.getString("esgFolder"));
//
//		esg.getStats().oe = esg.ID.keySet().size();
		esg.getStats().oe = esg.getNumberOfObservedEntities();
//		esg.getStats().es = esg.IS.keySet().size();
		esg.getStats().es = esg.getNumberOfEquivalenceSets();

		EquivalenceSetGraphAnalyser.countBlankNodes(esg);
		EquivalenceSetGraphAnalyser.countEdges(esg);
		EquivalenceSetGraphAnalyser.computeHeight(esg);
		EquivalenceSetGraphAnalyser.countIsoltatedEquivalenceSets(esg);
		EquivalenceSetGraphAnalyser.countTopLevelEquivalenceSetsAndAssessEmptyNodes(esg);
		EquivalenceSetGraphAnalyser.computeDistributionOfExtensionalSizeOfEquivalenceSets(esg);
		EquivalenceSetGraphAnalyser.countObservedEntitiesWithEmptyExtesion(esg);
		EquivalenceSetGraphAnalyser.countEquivalenceSetsWithEmptyExtension(esg);

		System.out.println(esg.getStats().in);
		System.out.println("TL " + esg.getStats().tl);
		System.out.println("TLbn " + esg.getStats().tlWithoutBNs);
		System.out.println("OE-TL " + esg.getStats().oeInTL);
		System.out.println("OE-TLbn " + esg.getStats().oeInTLWithoutBNs);
		System.out.println("OE-TL0 " + esg.getStats().oeInTL0);
		System.out.println("OE-TL0bn " + esg.getStats().oeInTl0WithoutBN);

		esg.getStats().toTSVFile("/Users/lgu/Desktop/stats.tsv");

		EquivalenceSetGraphAnalyser.exportIESDistributionAsTSV(esg, "/Users/lgu/Desktop/ies_classes.tsv", 0.99);

		RocksDBBackedEquivalenceSetGraph esgProperties = EquivalenceSetGraphLoader
				.loadEquivalenceSetGraphFromFolder(config.getString("esgPropertiesFolder"));

		EquivalenceSetGraphAnalyser.exportIESDistributionAsTSV(esgProperties, "/Users/lgu/Desktop/ies_properties.tsv",
				0.99);

	}

	private static void testStats(String esgFolder) throws RocksDBException, IOException {

		RocksDBBackedEquivalenceSetGraph esg = EquivalenceSetGraphLoader.loadEquivalenceSetGraphFromFolder(esgFolder);

//		esg.getStats().oe = esg.ID.keySet().size();
//		esg.getStats().es = esg.IS.keySet().size();

		esg.getStats().oe = esg.getNumberOfObservedEntities();
		esg.getStats().es = esg.getNumberOfEquivalenceSets();

		EquivalenceSetGraphAnalyser.countBlankNodes(esg);
		EquivalenceSetGraphAnalyser.countEdges(esg);
		EquivalenceSetGraphAnalyser.computeHeight(esg);
		EquivalenceSetGraphAnalyser.countIsoltatedEquivalenceSets(esg);
		EquivalenceSetGraphAnalyser.countTopLevelEquivalenceSetsAndAssessEmptyNodes(esg);
		EquivalenceSetGraphAnalyser.computeDistributionOfExtensionalSizeOfEquivalenceSets(esg);
		EquivalenceSetGraphAnalyser.countObservedEntitiesWithEmptyExtesion(esg);
		EquivalenceSetGraphAnalyser.countEquivalenceSetsWithEmptyExtension(esg);

		System.out.println(esg.getStats().in);
		System.out.println("TL " + esg.getStats().tl);
		System.out.println("TLbn " + esg.getStats().tlWithoutBNs);
		System.out.println("OE-TL " + esg.getStats().oeInTL);
		System.out.println("OE-TLbn " + esg.getStats().oeInTLWithoutBNs);
		System.out.println("OE-TL0 " + esg.getStats().oeInTL0);
		System.out.println("OE-TL0bn " + esg.getStats().oeInTl0WithoutBN);

		esg.getStats().toTSVFile("/Users/lgu/Desktop/stats.tsv");

	}

	private static void testRDF(Configuration config) throws Exception {

		RocksDBBackedEquivalenceSetGraph esg = EquivalenceSetGraphLoader
				.loadEquivalenceSetGraphFromFolder(config.getString("esgFolder"));

		esg.toRDF("/Users/lgu/Desktop/" + config.getString("esgName") + ".nt", config.getString("esgBaseURI"),
				config.getString("esgName"));

	}

	private static void estimateExtensionalSize(Configuration config) throws RocksDBException, IOException {

//		EquivalenceSetGraph esg = EquivalenceSetGraphLoader
//				.loadEquivalenceSetGraphFromFolder(config.getString("esgFolder"));

//		Collection<String> oe = esg.ID.keySet();
//		
//		oe.removeAll(esg.IS.values());
//		System.out.println(oe.size());
//		
//		EquivalenceSetGraphAnalyser.countIsoltatedEquivalenceSets(esg);
//		
//		System.out.println(esg.getStats().in);

//		esg.printSimpleStats();

//		EquivalenceSetGraph esgProperties = EquivalenceSetGraphLoader
//				.loadEquivalenceSetGraphFromFolder(config.getString("esgPropertiesFolder"));
//		InputDataset id = InputDataset.getInstance(config.getString("hdtFilePath"));
//		new ClassSizeEstimator().estimateObservedEntitiesSizeUsingESGForProperties(esg, esgProperties, id.getHDT());

	}

	private static void testCC(Configuration config) throws RocksDBException, IOException {

		RocksDBBackedEquivalenceSetGraph esg = EquivalenceSetGraphLoader
				.loadEquivalenceSetGraphFromFolder(config.getString("esgFolder"));
		esg.toEdgeListNodeList(config.getString("esgFolder"));

	}

	private static void query() throws RocksDBException {
		RocksDBBackedEquivalenceSetGraph esg = EquivalenceSetGraphLoader
				.loadEquivalenceSetGraphFromFolder("/Users/lgu/Desktop/ESGs/properties");

		System.out.println("http://www.w3.org/2008/05/skos#broaderTransitive");
		System.out.println(esg.getSizeOfEntity("http://www.w3.org/2008/05/skos#broaderTransitive"));

		esg.getEntitiesImplicityEquivalentToOrSubsumedBy("http://www.w3.org/2008/05/skos#broaderTransitive")
				.forEach(i -> {
					System.out.println(i);
					System.out.println(esg.getSizeOfEntity(i));
				});

	}
}
