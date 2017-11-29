package com.paduvi.app.models;

import java.util.List;

public class Project {

	private double inflationRate;
	private List<Pack> packages;
	private Timestamp startDate;

	public List<Pack> getPackages() {
		return packages;
	}

	public void setPackages(List<Pack> packages) {
		this.packages = packages;
	}

	public double getInflationRate() {
		return inflationRate;
	}

	public void setInflationRate(double inflationRate) {
		this.inflationRate = inflationRate;
	}

	public Timestamp getStartDate() {
		return startDate;
	}

	public void setStartDate(Timestamp startDate) {
		this.startDate = startDate;
	}

}
