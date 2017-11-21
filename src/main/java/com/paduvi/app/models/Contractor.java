package com.paduvi.app.models;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Contractor {

	@JsonProperty("name")
	private String name;

	@JsonProperty("relationship")
	private float relationship;

	@JsonProperty("quality")
	private float quality;

	@JsonProperty("packages")
	private List<Pack> packages;

	public boolean hasPackage(int id) {
		return packages.parallelStream().anyMatch(pkg -> pkg.getId() == id);
	}

	public Pack getPackageById(int id) {
		for (Pack pkg : packages) {
			if (pkg.getId() == id)
				return pkg;
		}
		return null;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public float getRelationship() {
		return relationship;
	}

	public void setRelationship(float relationship) {
		this.relationship = relationship;
	}

	public float getQuality() {
		return quality;
	}

	public void setQuality(float quality) {
		this.quality = quality;
	}

	public List<Pack> getPackages() {
		return packages;
	}

	public void setPackages(List<Pack> packages) {
		this.packages = packages;
	}
}
