package com.paduvi.app.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Product {

	@JsonProperty("id")
	private int id;

	@JsonProperty("n_units")
	private int nUnits;

	@JsonProperty("description")
	private String description;

	@JsonProperty("buy")
	private long buy;

	@JsonProperty("sell")
	private long sell;

	@JsonProperty("discount")
	private int discount;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getnUnits() {
		return nUnits;
	}

	public void setnUnits(int nUnits) {
		this.nUnits = nUnits;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public long getBuy() {
		return buy;
	}

	public void setBuy(long buy) {
		this.buy = buy;
	}

	public long getSell() {
		return sell;
	}

	public void setSell(long sell) {
		this.sell = sell;
	}

	public int getDiscount() {
		return discount;
	}

	public void setDiscount(int discount) {
		this.discount = discount;
	}
}
