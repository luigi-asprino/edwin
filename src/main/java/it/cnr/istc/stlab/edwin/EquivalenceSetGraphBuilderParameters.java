package it.cnr.istc.stlab.edwin;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class EquivalenceSetGraphBuilderParameters {

	private String esgFolder;

	private String equivalencePropertyToObserve, specializationPropertyToObserve, specializationPropertyForProperties,
			equivalencePropertiesForProperties, esgPropertiesFolder;
	
	private Set<String> notEquivalenceProperties = new HashSet<>();
	private Set<String> notSpecializationProperties = new HashSet<>();
	private ObservedEntitiesSelector observedEntitiesSelector;


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

	@Override
	public String toString() {
		return "EquivalenceSetGraphBuilderParameters [esgFolder=" + esgFolder + ", equivalencePropertyToObserve="
				+ equivalencePropertyToObserve + ", specializationPropertyToObserve=" + specializationPropertyToObserve
				+ ", specializationPropertyForProperties=" + specializationPropertyForProperties
				+ ", equivalencePropertiesForProperties=" + equivalencePropertiesForProperties
				+ ", notEquivalenceProperties=" + notEquivalenceProperties + ", notSpecializationProperties="
				+ notSpecializationProperties + ", observedEntitiesSelector=" + observedEntitiesSelector + "]";
	}

}
