package it.cnr.istc.stlab.edwin;

import java.io.File;
import java.io.IOException;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.rdfhdt.hdt.hdt.HDT;
import org.rocksdb.RocksDBException;

public final class EquivalenceSetGraphBuilders {

	public static void classesAndProperties(String hdtPathFile, String configClasses, String configProperties)
			throws IOException {

		// Build graphs
		EquivalenceSetGraph esg_properties = properties(configProperties);
		EquivalenceSetGraph esg_classes = classes(configClasses);

		HDT hdt = InputDataset.getInstance(hdtPathFile).getHDT();
		PropertiesSelector ps = new PropertiesSelector();
		ps.addSpareEntitiesToEquivalentSetGraph(esg_properties, esg_classes, hdt);
		ClassesSelector cs = new ClassesSelector();
		cs.addSpareEntitiesToEquivalentSetGraph(esg_classes, esg_properties, hdt);

		PropertySizeEstimator pse = new PropertySizeEstimator();
		pse.estimateObservedEntitiesSize(esg_properties, hdt);

		ClassSizeEstimator cse = new ClassSizeEstimator();
		cse.estimateObservedEntitiesSize(esg_classes, esg_properties, hdt);

	}

	private static EquivalenceSetGraph properties(String configProperties) {
		try {

			Configurations configs = new Configurations();
			Configuration config = configs.properties(configProperties);

			EquivalenceSetGraphBuilderParameters parameters = new EquivalenceSetGraphBuilderParameters();

			parameters.setEquivalencePropertyToObserve(config.getString("equivalencePropertyToObserve"));
			parameters.setEquivalencePropertiesForProperties(config.getString("equivalencePropertyForProperties"));

			parameters.setSpecializationPropertyToObserve(config.getString("specializationPropertyToObserve"));
			parameters.setSpecializationPropertyForProperties(config.getString("specializationPropertyForProperties"));

			parameters.addNotEquivalenceProperties(config.getString("notEquivalenceProperties").split(","));
			parameters.addNotSpecializationProperties(config.getString("notSpecializationProperties").split(","));

			parameters.setEsgFolder(config.getString("esgFolder"));
			parameters.setEsgPropertiesFolder(config.getString("esgPropertiesFolder"));

			if (config.containsKey("observedEntitiesSelector")) {
				parameters.setObservedEntitiesSelector((ObservedEntitiesSelector) Class
						.forName(config.getString("observedEntitiesSelector")).newInstance());
			}

			new File(config.getString("esgFolder")).mkdirs();

			EquivalenceSetGraphBuilder esgb = EquivalenceSetGraphBuilder.getInstance(config.getString("hdtFilePath"));
			EquivalenceSetGraph esg = esgb.build(parameters);

			return esg;

		} catch (ConfigurationException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (RocksDBException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

		return null;
	}

	private static EquivalenceSetGraph classes(String configClasses) {
		try {

			Configurations configs = new Configurations();
			Configuration config = configs.properties(configClasses);

			EquivalenceSetGraphBuilderParameters parameters = new EquivalenceSetGraphBuilderParameters();

			parameters.setEquivalencePropertyToObserve(config.getString("equivalencePropertyToObserve"));
			parameters.setEquivalencePropertiesForProperties(config.getString("equivalencePropertyForProperties"));

			parameters.setSpecializationPropertyToObserve(config.getString("specializationPropertyToObserve"));
			parameters.setSpecializationPropertyForProperties(config.getString("specializationPropertyForProperties"));

			parameters.addNotEquivalenceProperties(config.getString("notEquivalenceProperties").split(","));
			parameters.addNotSpecializationProperties(config.getString("notSpecializationProperties").split(","));

			parameters.setEsgFolder(config.getString("esgFolder"));
			parameters.setEsgPropertiesFolder(config.getString("esgPropertiesFolder"));

			if (config.containsKey("observedEntitiesSelector")) {
				parameters.setObservedEntitiesSelector((ObservedEntitiesSelector) Class
						.forName(config.getString("observedEntitiesSelector")).newInstance());
			}

			new File(config.getString("esgFolder")).mkdirs();

			EquivalenceSetGraphBuilder esgb = EquivalenceSetGraphBuilder.getInstance(config.getString("hdtFilePath"));
			EquivalenceSetGraph esg = esgb.build(parameters);

			return esg;

		} catch (ConfigurationException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (RocksDBException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

		return null;
	}

}
