package com.paduvi.app.models;

import java.util.List;

public class Product {

	private int productId;
	private int quantity;
	private long buyPrice;
	private long sellPrice;
	private List<Discount> discounts;

	public int getProductId() {
		return productId;
	}

	public void setProductId(int id) {
		this.productId = id;
	}

	public int getQuantity() {
		return quantity;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

	public long getBuyPrice() {
		return buyPrice;
	}

	public void setBuyPrice(long buyPrice) {
		this.buyPrice = buyPrice;
	}

	public long getSellPrice() {
		return sellPrice;
	}

	public void setSellPrice(long sellPrice) {
		this.sellPrice = sellPrice;
	}

	public List<Discount> getDiscounts() {
		return discounts;
	}

	public void setDiscounts(List<Discount> discounts) {
		this.discounts = discounts;
	}

	public boolean isDiscounted(Timestamp t) {
		return this.discounts.parallelStream().anyMatch(d -> d.isValid(t));
	}

	public double getDiscountRate(Timestamp t) {
		Discount discount = this.discounts.parallelStream().filter(d -> d.isValid(t)).findAny().orElse(null);
		if (discount == null)
			return 0;
		return discount.getRate();
	}

	public class Discount {
		private Timestamp from;
		private Timestamp to;
		private double rate;

		public boolean isValid(Timestamp t) {
			return t.get() >= from.get() && t.get() <= to.get();
		}

		public double getRate() {
			return rate;
		}

		public void setRate(double rate) {
			this.rate = rate;
		}

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
