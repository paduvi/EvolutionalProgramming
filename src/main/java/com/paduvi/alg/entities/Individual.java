package com.paduvi.alg.entities;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

public class Individual {
	private int nGenes;
	private byte[] genes;
	private double[] fitness;
	private List<Function<byte[], Double>> fitnessFuncList;

	public Individual(int nGenes, List<Function<byte[], Double>> fitnessFuncList) {
		this.fitnessFuncList = fitnessFuncList;
		this.nGenes = nGenes;
		this.genes = new byte[nGenes];
		for (int i = 0; i < size(); i++) {
			this.genes[i] = (byte) Math.round(Math.random());
		}
		calcFitness();
	}

	private void calcFitness() {
		this.fitness = fitnessFuncList.stream().mapToDouble(func -> func.apply(genes)).toArray();
	}

	/* Getters and setters */
	public List<Function<byte[], Double>> getFitnessFunc() {
		return fitnessFuncList;
	}

	public byte getGene(int index) {
		return genes[index];
	}

	public void setGene(int index, byte value) {
		genes[index] = value;
		calcFitness();
	}

	public double[] getFitness() {
		return fitness;
	}

	public int size() {
		return nGenes;
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

	public boolean rshift(Individual other) {
		boolean dominate = false;
		for (int i = 0; i < fitnessFuncList.size(); i++) {
			if (this.fitness[i] > other.fitness[i]) {
				return false;
			}
			if (this.fitness[i] < other.fitness[i]) {
				dominate = true;
			}
		}
		return dominate;
	}

	public boolean lshift(Individual other) {
		return other.rshift(this);
	}
}