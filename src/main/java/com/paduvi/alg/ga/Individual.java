package com.paduvi.alg.ga;

import java.util.Arrays;
import java.util.function.Function;

import com.paduvi.util.Constants;

public class Individual {
	private int geneLength;
	private byte[] genes;
	private double fitness;
	private Function<byte[], Double> fitnessFunc;

	public Individual(int geneLength, Function<byte[], Double> fitnessFunc) {
		this.fitnessFunc = fitnessFunc;
		this.geneLength = geneLength;
		this.genes = new byte[geneLength];
		for (int i = 0; i < size(); i++) {
			byte gene = (byte) Math.round(Constants.rand(1));
			genes[i] = gene;
		}
		calcFitness();
	}

	public void calcFitness() {
		this.fitness = fitnessFunc.apply(genes);
	}

	/* Getters and setters */
	public Function<byte[], Double> getFitnessFunc() {
		return fitnessFunc;
	}

	public byte getGene(int index) {
		return genes[index];
	}

	public void setGene(int index, byte value) {
		genes[index] = value;
		calcFitness();
	}

	public double getFitness() {
		return fitness;
	}

	public int size() {
		return geneLength;
	}

	@Override
	public String toString() {
		return Arrays.toString(genes);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Individual) {
			Individual other = (Individual) obj;
			return Arrays.equals(genes, other.genes);
		}
		return false;
	}
}