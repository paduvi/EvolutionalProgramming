package com.paduvi.app.models;

import java.util.List;

public class Contractor {

	private int contractorId;
	private String description;
	private double relationship;
	private double quality;
	private List<Product> products;

	public double getRelationship() {
		return relationship;
	}

	public void setRelationship(double relationship) {
		this.relationship = relationship;
	}

	public double getQuality() {
		return quality;
	}

	public void setQuality(double quality) {
		this.quality = quality;
	}

	public int getContractorId() {
		return contractorId;
	}

	public void setContractorId(int contractorId) {
		this.contractorId = contractorId;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public List<Product> getProducts() {
		return products;
	}

	public void setProducts(List<Product> products) {
		this.products = products;
	}

}
