package com.paduvi.alg.ga;

import java.util.List;
import java.util.function.Function;

public class Population {

	private Individual[] individuals;
	private boolean stopConditionReached = false;

	/*
	 * Constructors
	 */
	public Population(int populationSize) {
		individuals = new Individual[populationSize];
	}

	public Population(int populationSize, int geneLength, List<Function<byte[], Double>> fitnessFuncList) {
		individuals = new Individual[populationSize];
		// Initialise population
		// Loop and create individuals
		for (int i = 0; i < size(); i++) {
			Individual newIndividual = new Individual(geneLength, fitnessFuncList);
			saveIndividual(i, newIndividual);
		}
	}

	/* Getters */
	public Individual getIndividual(int index) {
		return individuals[index];
	}

	/* Public methods */
	// Get population size
	public int size() {
		return individuals.length;
	}

	// Save individual
	public void saveIndividual(int index, Individual indiv) {
		individuals[index] = indiv;
	}

	public boolean isStopConditionReached() {
		return stopConditionReached;
	}

	public void setStopConditionReached(boolean stopConditionReached) {
		this.stopConditionReached = stopConditionReached;
	}
}
