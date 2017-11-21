package com.paduvi.app.models;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Owner {

	@JsonProperty("r")
	private float r;

	@JsonProperty("packages")
	private List<Pack> packages;

	public float getR() {
		return r;
	}

	public void setR(float r) {
		this.r = r;
	}

	public List<Pack> getPackages() {
		return packages;
	}

	public void setPackages(List<Pack> packages) {
		this.packages = packages;
	}

}
