package it.cnr.istc.stlab.edwin;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.configuration2.Configuration;

public class EquivalenceSetGraphBuilderParameters {

	private String esgFolder, esgName, esgBaseURI;

	private String equivalencePropertyToObserve, specializationPropertyToObserve, specializationPropertyForProperties,
			equivalencePropertiesForProperties, esgPropertiesFolder, esgClassesFolder;

	private RocksDBBackedEquivalenceSetGraph esgProperties;

	private boolean computeClosure = false;
	private boolean computeStats = true;
	private boolean computeEstimation = false;
	private boolean exportInRDFFormat = false;

	private Set<String> notEquivalenceProperties = new HashSet<>();
	private Set<String> notSpecializationProperties = new HashSet<>();
	private Set<String> additionalEquivalencePropertiesToObserve = new HashSet<>(),
			additionalSpecializationPropertiesToObserve = new HashSet<>();
	private String[] datasetPaths;
	private ObservedEntitiesSelector observedEntitiesSelector;
	private ExtensionalSizeEstimator extensionalSizeEstimator;

	public String getEsgPropertiesFolder() {
		return esgPropertiesFolder;
	}

	public void setEsgPropertiesFolder(String esgPropertiesFolder) {
		this.esgPropertiesFolder = esgPropertiesFolder;
	}

	public String getEsgFolder() {
		return esgFolder;
	}

	public ObservedEntitiesSelector getObservedEntitiesSelector() {
		return observedEntitiesSelector;
	}

	public void setObservedEntitiesSelector(ObservedEntitiesSelector observedEntitiesSelector) {
		this.observedEntitiesSelector = observedEntitiesSelector;
	}

	public void setEsgFolder(String esgFolder) {
		this.esgFolder = esgFolder;
	}

	public void addNotEquivalenceProperties(String... uris) {
		for (String s : uris) {
			notEquivalenceProperties.add(s);
		}
	}

	public void addNotEquivalenceProperties(List<String> uris) {
		for (String s : uris) {
			notEquivalenceProperties.add(s);
		}
	}

	public void addNotSpecializationProperties(String... uris) {
		for (String s : uris) {
			notSpecializationProperties.add(s);
		}
	}

	public void addNotSpecializationProperties(List<String> uris) {
		for (String s : uris) {
			notSpecializationProperties.add(s);
		}
	}

	public Set<String> getNotEquivalenceProperties() {
		return notEquivalenceProperties;
	}

	public Set<String> getNotSpecializationProperties() {
		return notSpecializationProperties;
	}

	public void setNotEquivalenceProperties(Set<String> notEquivalenceProperties) {
		this.notEquivalenceProperties = notEquivalenceProperties;
	}

	public void setNotSpecializationProperties(Set<String> notSpecializationProperties) {
		this.notSpecializationProperties = notSpecializationProperties;
	}

	public String getEquivalencePropertyToObserve() {
		return equivalencePropertyToObserve;
	}

	public void setEquivalencePropertyToObserve(String equivalencePropertyToObserve) {
		this.equivalencePropertyToObserve = equivalencePropertyToObserve;
	}

	public String getSpecializationPropertyToObserve() {
		return specializationPropertyToObserve;
	}

	public void setSpecializationPropertyToObserve(String specializationPropertyToObserve) {
		this.specializationPropertyToObserve = specializationPropertyToObserve;
	}

	public String getSpecializationPropertyForProperties() {
		return specializationPropertyForProperties;
	}

	public void setSpecializationPropertyForProperties(String specializationPropertyForProperties) {
		this.specializationPropertyForProperties = specializationPropertyForProperties;
	}

	public String getEquivalencePropertiesForProperties() {
		return equivalencePropertiesForProperties;
	}

	public void setEquivalencePropertiesForProperties(String equivalencePropertiesForProperties) {
		this.equivalencePropertiesForProperties = equivalencePropertiesForProperties;
	}

	public String getEsgClassesFolder() {
		return esgClassesFolder;
	}

	public void setEsgClassesFolder(String esgClassesFolder) {
		this.esgClassesFolder = esgClassesFolder;
	}

	public ExtensionalSizeEstimator getExtensionalSizeEstimator() {
		return extensionalSizeEstimator;
	}

	public void setExtensionalSizeEstimator(ExtensionalSizeEstimator extensionalSizeEstimator) {
		this.extensionalSizeEstimator = extensionalSizeEstimator;
	}

	public static EquivalenceSetGraphBuilderParameters getParameters(Configuration config)
			throws InstantiationException, IllegalAccessException, ClassNotFoundException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		EquivalenceSetGraphBuilderParameters parameters = new EquivalenceSetGraphBuilderParameters();

		parameters.setDatasetPaths(config.getStringArray("datasetPaths"));

		parameters.setEquivalencePropertyToObserve(config.getString("equivalencePropertyToObserve"));
		parameters.setEquivalencePropertiesForProperties(config.getString("equivalencePropertyForProperties"));

		parameters.setSpecializationPropertyToObserve(config.getString("specializationPropertyToObserve"));
		parameters.setSpecializationPropertyForProperties(config.getString("specializationPropertyForProperties"));

		if (config.containsKey("notEquivalenceProperties")) {
			parameters.addNotEquivalenceProperties(config.getString("notEquivalenceProperties").split(","));
		}
		if (config.containsKey("notSpecializationProperties")) {
			parameters.addNotSpecializationProperties(config.getString("notSpecializationProperties").split(","));
		}

		if (config.containsKey("additionalEquivalencePropertiesToObserve")) {
			parameters.setAdditionalEquivalencePropertiesToObserve(
					new HashSet<>(Arrays.asList(config.getString("additionalEquivalencePropertiesToObserve"))));
		}

		if (config.containsKey("additionalSpecializationPropertiesToObserve")) {
			parameters.setAdditionalEquivalencePropertiesToObserve(
					new HashSet<>(Arrays.asList(config.getString("additionalSpecializationPropertiesToObserve"))));
		}

		parameters.setEsgFolder(config.getString("esgFolder"));

		if (config.containsKey("esgPropertiesFolder")) {
			parameters.setEsgPropertiesFolder(config.getString("esgPropertiesFolder"));
		}

		if (config.containsKey("esgClassesFolder")) {
			parameters.setEsgPropertiesFolder(config.getString("esgClassesFolder"));
		}

		if (config.containsKey("observedEntitiesSelector")) {
			parameters.setObservedEntitiesSelector((ObservedEntitiesSelector) Class
					.forName(config.getString("observedEntitiesSelector")).getConstructor().newInstance());
		}

		if (config.containsKey("extensionalSizeEstimator")) {
			parameters.setExtensionalSizeEstimator((ExtensionalSizeEstimator) Class
					.forName(config.getString("extensionalSizeEstimator")).getConstructor().newInstance());
		}

		if (config.containsKey("esgName")) {
			parameters.setEsgName(config.getString("esgName"));
		}

		if (config.containsKey("esgBaseURI")) {
			parameters.setEsgBaseURI(config.getString("esgBaseURI"));
		}

		if (config.containsKey("computeEstimations")) {
			parameters.setComputeEstimation(config.getBoolean("computeEstimations"));
		}

		if (config.containsKey("exportInRDFFormat")) {
			parameters.setExportInRDFFormat(config.getBoolean("exportInRDFFormat"));
		}

		return parameters;
	}

	public String getEsgName() {
		return esgName;
	}

	public void setEsgName(String esgName) {
		this.esgName = esgName;
	}

	public String getEsgBaseURI() {
		return esgBaseURI;
	}

	public void setEsgBaseURI(String esgBaseURI) {
		this.esgBaseURI = esgBaseURI;
	}

	public boolean isComputeClosure() {
		return computeClosure;
	}

	public void setComputeClosure(boolean computeClosure) {
		this.computeClosure = computeClosure;
	}

	public boolean isComputeStats() {
		return computeStats;
	}

	public void setComputeStats(boolean computeStats) {
		this.computeStats = computeStats;
	}

	public RocksDBBackedEquivalenceSetGraph getEsgProperties() {
		return esgProperties;
	}

	public void setEsgProperties(RocksDBBackedEquivalenceSetGraph esgProperties) {
		this.esgProperties = esgProperties;
	}

	public boolean isComputeEstimation() {
		return computeEstimation;
	}

	public void setComputeEstimation(boolean computeEstimation) {
		this.computeEstimation = computeEstimation;
	}

	@Override
	public String toString() {
		return "EquivalenceSetGraphBuilderParameters [\n esgFolder=" + esgFolder + ",\n esgName=" + esgName
				+ ",\n esgBaseURI=" + esgBaseURI + ",\n equivalencePropertyToObserve=" + equivalencePropertyToObserve
				+ ",\n specializationPropertyToObserve=" + specializationPropertyToObserve
				+ ",\n specializationPropertyForProperties=" + specializationPropertyForProperties
				+ ",\n equivalencePropertiesForProperties=" + equivalencePropertiesForProperties
				+ ",\n esgPropertiesFolder=" + esgPropertiesFolder + ",\n esgClassesFolder=" + esgClassesFolder
				+ ",\n esgProperties=" + esgProperties + ",\n computeClosure=" + computeClosure + ",\n computeStats="
				+ computeStats + ",\n computeEstimation=" + computeEstimation + ",\n exportInRDFFormat="
				+ exportInRDFFormat + ",\n notEquivalenceProperties=" + notEquivalenceProperties
				+ ",\n notSpecializationProperties=" + notSpecializationProperties
				+ ",\n additionalEquivalencePropertiesToObserve=" + additionalEquivalencePropertiesToObserve
				+ ",\n additionalSpecializationPropertiesToObserve=" + additionalSpecializationPropertiesToObserve
				+ ",\n datasetPaths=" + Arrays.toString(datasetPaths) + ",\n observedEntitiesSelector="
				+ observedEntitiesSelector + ",\n extensionalSizeEstimator=" + extensionalSizeEstimator + "]";
	}

	public String[] getDatasetPaths() {
		return datasetPaths;
	}

	public void setDatasetPaths(String[] datasetPaths) {
		this.datasetPaths = datasetPaths;
	}

	public Set<String> getAdditionalEquivalencePropertiesToObserve() {
		return additionalEquivalencePropertiesToObserve;
	}

	public void setAdditionalEquivalencePropertiesToObserve(Set<String> additionalEquivalencePropertiesToObserve) {
		this.additionalEquivalencePropertiesToObserve = additionalEquivalencePropertiesToObserve;
	}

	public Set<String> getAdditionalSpecializationPropertiesToObserve() {
		return additionalSpecializationPropertiesToObserve;
	}

	public void setAdditionalSpecializationPropertiesToObserve(
			Set<String> additionalSpecializationPropertiesToObserve) {
		this.additionalSpecializationPropertiesToObserve = additionalSpecializationPropertiesToObserve;
	}

	public boolean isExportInRDFFormat() {
		return exportInRDFFormat;
	}

	public void setExportInRDFFormat(boolean exportInRDFFormat) {
		this.exportInRDFFormat = exportInRDFFormat;
	}

	public EquivalenceSetGraphBuilderParameters clone() {
		EquivalenceSetGraphBuilderParameters parameters = new EquivalenceSetGraphBuilderParameters();

		if (this.datasetPaths != null) {
			parameters.setDatasetPaths(this.datasetPaths.clone());
		}

		if (this.equivalencePropertyToObserve != null) {
			parameters.setEquivalencePropertyToObserve(new String(this.equivalencePropertyToObserve));
		}

		if (this.equivalencePropertiesForProperties != null) {
			parameters.setEquivalencePropertiesForProperties(new String(this.equivalencePropertiesForProperties));
		}

		if (this.specializationPropertyToObserve != null) {
			parameters.setSpecializationPropertyToObserve(new String(this.specializationPropertyToObserve));
		}

		if (this.specializationPropertyForProperties != null) {
			parameters.setSpecializationPropertyForProperties(new String(this.specializationPropertyForProperties));
		}

		if (this.notEquivalenceProperties != null) {
			parameters.notEquivalenceProperties = new HashSet<>(this.notEquivalenceProperties);
		}

		if (this.notSpecializationProperties != null) {
			parameters.notSpecializationProperties = new HashSet<>(this.notSpecializationProperties);
		}

		if (this.additionalEquivalencePropertiesToObserve != null) {
			parameters.additionalEquivalencePropertiesToObserve = new HashSet<>(
					this.additionalEquivalencePropertiesToObserve);
		}

		if (this.additionalSpecializationPropertiesToObserve != null) {
			parameters.additionalSpecializationPropertiesToObserve = new HashSet<>(
					this.additionalSpecializationPropertiesToObserve);
		}

		if (this.esgFolder != null) {
			parameters.esgFolder = new String(this.esgFolder);
		}

		if (this.esgPropertiesFolder != null) {
			parameters.esgPropertiesFolder = new String(this.esgPropertiesFolder);
		}

		if (this.esgClassesFolder != null) {
			parameters.esgClassesFolder = new String(this.esgClassesFolder);
		}

		if (this.observedEntitiesSelector != null) {
			parameters.observedEntitiesSelector = this.observedEntitiesSelector;
		}

		if (this.extensionalSizeEstimator != null) {
			parameters.extensionalSizeEstimator = this.extensionalSizeEstimator;
		}

		if (this.esgName != null) {
			parameters.esgName = new String(this.esgName);
		}

		if (this.esgBaseURI != null) {
			parameters.esgBaseURI = new String(this.esgBaseURI);
		}

		parameters.computeEstimation = this.computeEstimation;
		parameters.computeClosure = this.computeClosure;

		return parameters;

	}

}
