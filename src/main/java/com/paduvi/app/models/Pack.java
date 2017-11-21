package com.paduvi.app.models;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Pack {

	@JsonProperty("id")
	private int id;

	@JsonProperty("description")
	private String description;

	@JsonProperty("duration")
	private int duration;

	@JsonProperty("estimated_cost")
	private long estimatedCost;

	@JsonProperty("products")
	private List<Product> products;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public int getDuration() {
		return duration;
	}

	public void setDuration(int duration) {
		this.duration = duration;
	}

	public long getEstimatedCost() {
		return estimatedCost;
	}

	public void setEstimatedCost(long estimatedCost) {
		this.estimatedCost = estimatedCost;
	}

	public List<Product> getProducts() {
		return products;
	}

	public void setProducts(List<Product> products) {
		this.products = products;
	}

}
