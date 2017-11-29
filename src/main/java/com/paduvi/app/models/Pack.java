package com.paduvi.app.models;

import java.util.List;

public class Pack {

	private int packageId;
	private long estimatedCost;
	private List<Product> products;
	private Timeline timeline;
	private List<Integer> joinedContractors;

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

	public Timeline getTimeline() {
		return timeline;
	}

	public void setTimeline(Timeline timeline) {
		this.timeline = timeline;
	}

	public List<Integer> getJoinedContractors() {
		return joinedContractors;
	}

	public void setJoinedContractors(List<Integer> joinedContractors) {
		this.joinedContractors = joinedContractors;
	}

	public int getPackageId() {
		return packageId;
	}

	public void setPackageId(int packageId) {
		this.packageId = packageId;
	}

	public class Timeline {
		private Timestamp from;
		private Timestamp to;

		public Timestamp getFrom() {
			return from;
		}

		public void setFrom(Timestamp from) {
			this.from = from;
		}

		public Timestamp getTo() {
			return to;
		}

		public void setTo(Timestamp to) {
			this.to = to;
		}

	}
}
