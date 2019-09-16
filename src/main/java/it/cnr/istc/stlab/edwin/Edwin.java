package it.cnr.istc.stlab.edwin;

import java.io.File;
import java.io.IOException;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.rocksdb.RocksDBException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Edwin {

	private static Logger logger = LoggerFactory.getLogger(Edwin.class);

	public static void main(String[] args) {
		try {

			logger.info("Edwin v0.0.1");

			String configFile = "config.properties";

			if (args.length > 0) {
				configFile = args[0];
			}
			Configurations configs = new Configurations();
			Configuration config = configs.properties(configFile);

			computeESG(config);

		} catch (ConfigurationException e) {
			e.printStackTrace();
		} catch (RocksDBException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void computeESG(Configuration config) throws InstantiationException, IllegalAccessException,
			ClassNotFoundException, IOException, RocksDBException {

		EquivalenceSetGraphBuilderParameters parameters = new EquivalenceSetGraphBuilderParameters();

		parameters.setEquivalencePropertyToObserve(config.getString("equivalencePropertyToObserve"));
		parameters.setEquivalencePropertiesForProperties(config.getString("equivalencePropertyForProperties"));

		parameters.setSpecializationPropertyToObserve(config.getString("specializationPropertyToObserve"));
		parameters.setSpecializationPropertyForProperties(config.getString("specializationPropertyForProperties"));

		parameters.addNotEquivalenceProperties(config.getString("notEquivalenceProperties").split(","));
		parameters.addNotSpecializationProperties(config.getString("notSpecializationProperties").split(","));

		parameters.setEsgFolder(config.getString("esgFolder"));

		if (config.containsKey("esgPropertiesFolder")) {
			parameters.setEsgPropertiesFolder(config.getString("esgPropertiesFolder"));
		}

		if (config.containsKey("esgClassesFolder")) {
			parameters.setEsgPropertiesFolder(config.getString("esgClassesFolder"));
		}

		if (config.containsKey("observedEntitiesSelector")) {
			parameters.setObservedEntitiesSelector((ObservedEntitiesSelector) Class
					.forName(config.getString("observedEntitiesSelector")).newInstance());
		}

		if (config.containsKey("extensionalSizeEstimator")) {
			parameters.setExtensionalSizeEstimator((ExtensionalSizeEstimator) Class
					.forName(config.getString("extensionalSizeEstimator")).newInstance());
		}

		new File(config.getString("esgFolder")).mkdirs();

		EquivalenceSetGraphBuilder esgb = EquivalenceSetGraphBuilder.getInstance(config.getString("hdtFilePath"));
		EquivalenceSetGraph esg = esgb.build(parameters);

		esg.printSimpleStats();
		esg.getStats().toTSVFile(config.getString("esgFolder") + "/stats.tsv");
		esg.toEdgeListNodeList(config.getString("esgFolder"));
		esg.toFile();
		esg.toRDF(config.getString("esgFolder") + "/esg.nt", "https://w3id.org/edwin/", "properties");

	}

}
