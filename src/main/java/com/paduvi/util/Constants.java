package com.paduvi.util;

import java.util.function.Function;

import org.apache.commons.math3.distribution.NormalDistribution;

public class Constants {

	public static Function<byte[], Double> fitnessFunc = genes -> {
		double sum = 0;
		for (byte gene : genes) {
			sum += gene;
		}
		return sum;
	};
	
	public static double rand(double bound) {
		NormalDistribution dist = new NormalDistribution(0, bound);
		return Math.abs(dist.sample());
	};
}
