package it.cnr.istc.stlab.edwin;

import it.cnr.istc.stlab.edwin.model.EquivalenceSetGraph;
import it.cnr.istc.stlab.edwin.rocksdb.RocksDBBackedEquivalenceSetGraph;
import it.cnr.istc.stlab.edwin.rocksdb.RocksDBEquivalenceSetGraphBuilderImpl;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.ConfigurationUtils;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.rocksdb.RocksDBException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

public class Edwin {

    private static final Logger logger = LoggerFactory.getLogger(Edwin.class);

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

        } catch (ConfigurationException | RocksDBException | InstantiationException | SecurityException |
                 NoSuchMethodException | IllegalAccessException | ClassNotFoundException | IOException |
                 IllegalArgumentException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    public static EquivalenceSetGraph computeESG(Configuration config) throws InstantiationException, IllegalAccessException, ClassNotFoundException, IOException, RocksDBException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
        return computeESG(EquivalenceSetGraphBuilderParameters.getParameters(config));
    }

    public static EquivalenceSetGraph computeESG(EquivalenceSetGraphBuilderParameters parameters) throws IOException, RocksDBException, IllegalArgumentException, SecurityException {

        logger.info(parameters.toString());

        RocksDBEquivalenceSetGraphBuilderImpl esgb = new RocksDBEquivalenceSetGraphBuilderImpl(parameters.getDatasetPaths());
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

        } catch (ConfigurationException | RocksDBException | InstantiationException | IllegalAccessException |
                 ClassNotFoundException | IOException | IllegalArgumentException | InvocationTargetException |
                 NoSuchMethodException | SecurityException e) {
            e.printStackTrace();
        }

        return null;
    }

}
