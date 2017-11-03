package com.paduvi.util;

import java.util.function.Function;

public class Constants {

	public static Function<byte[], Double> fitnessFunc = genes -> {
		double sum = 0;
		for (byte gene : genes) {
			sum += gene;
		}
		return sum;
	};
	
}
