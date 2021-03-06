package it.cnr.istc.stlab.edwin;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.configuration2.Configuration;

public class EquivalenceSetGraphBuilderParameters {

	private String esgFolder, esgName, esgBaseURI;

	private String equivalencePropertyToObserve, specializationPropertyToObserve, specializationPropertyForProperties,
			equivalencePropertiesForProperties, esgPropertiesFolder, esgClassesFolder;

	private EquivalenceSetGraph esgProperties;

	private boolean computeClosure = true;
	private boolean computeStats = true;

	private Set<String> notEquivalenceProperties = new HashSet<>();
	private Set<String> notSpecializationProperties = new HashSet<>();
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
			throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		EquivalenceSetGraphBuilderParameters parameters = new EquivalenceSetGraphBuilderParameters();

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

		if (config.containsKey("esgName")) {
			parameters.setEsgName(config.getString("esgName"));
		}

		if (config.containsKey("esgBaseURI")) {
			parameters.setEsgBaseURI(config.getString("esgBaseURI"));
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

	@Override
	public String toString() {
		return "EquivalenceSetGraphBuilderParameters [esgFolder=" + esgFolder + ", esgName=" + esgName + ", esgBaseURI="
				+ esgBaseURI + ", equivalencePropertyToObserve=" + equivalencePropertyToObserve
				+ ", specializationPropertyToObserve=" + specializationPropertyToObserve
				+ ", specializationPropertyForProperties=" + specializationPropertyForProperties
				+ ", equivalencePropertiesForProperties=" + equivalencePropertiesForProperties
				+ ", esgPropertiesFolder=" + esgPropertiesFolder + ", esgClassesFolder=" + esgClassesFolder
				+ ", notEquivalenceProperties=" + notEquivalenceProperties + ", notSpecializationProperties="
				+ notSpecializationProperties + ", observedEntitiesSelector=" + observedEntitiesSelector
				+ ", extensionalSizeEstimator=" + extensionalSizeEstimator + "]";
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

	public EquivalenceSetGraph getEsgProperties() {
		return esgProperties;
	}

	public void setEsgProperties(EquivalenceSetGraph esgProperties) {
		this.esgProperties = esgProperties;
	}

}
