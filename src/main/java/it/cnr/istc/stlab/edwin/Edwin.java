package it.cnr.istc.stlab.edwin;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.ConfigurationUtils;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.rocksdb.RocksDBException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.cnr.istc.stlab.edwin.model.EquivalenceSetGraph;

public class Edwin {

	private static Logger logger = LoggerFactory.getLogger(Edwin.class);

	public static void main(String[] args) {
		try {

			logger.info("Edwin v0.0.1");

			String configFile = "config.properties";

			if (args.length > 0) {
				configFile = args[0];
			}

			logger.info("Configuration file {}", new File(configFile).getAbsolutePath());

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
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		}
	}

	public static EquivalenceSetGraph computeESG(Configuration config) throws InstantiationException,
			IllegalAccessException, ClassNotFoundException, IOException, RocksDBException, IllegalArgumentException,
			InvocationTargetException, NoSuchMethodException, SecurityException {
		return computeESG(EquivalenceSetGraphBuilderParameters.getParameters(config));
	}

	public static EquivalenceSetGraph computeESG(EquivalenceSetGraphBuilderParameters parameters)
			throws InstantiationException, IllegalAccessException, ClassNotFoundException, IOException,
			RocksDBException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException,
			SecurityException {

		logger.info(parameters.toString());

		EquivalenceSetGraphBuilderImpl esgb = new EquivalenceSetGraphBuilderImpl(parameters.getDatasetPaths());
		RocksDBBackedEquivalenceSetGraph esg = esgb.build(parameters);

		esg.printSimpleStats();
		esg.getStats().toTSVFile(parameters.getEsgFolder() + "/stats.tsv");
		esg.toEdgeListNodeList(parameters.getEsgFolder());
		esg.toFile();
		if (parameters.isExportInRDFFormat()) {
			esg.toRDF(parameters.getEsgFolder() + "/esg.nt", parameters.getEsgBaseURI(), parameters.getEsgName());
		}

		return esg;

	}

	public static EquivalenceSetGraph computeESG(String configFile) {
		try {

			logger.info("Edwin v0.0.1");

			Configurations configs = new Configurations();
			Configuration config = configs.properties(configFile);

			logger.trace("Configurations {}", ConfigurationUtils.toString(config));

			return computeESG(config);

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
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		}

		return null;
	}

}
