package com.paduvi.alg.ga;

import java.util.function.Function;

public class Population {

	private Individual[] individuals;
	private Double maxFitness;
	private boolean stopConditionReached = false;

	/*
	 * Constructors
	 */
	public Population(int populationSize) {
		individuals = new Individual[populationSize];
	}

	public Population(int populationSize, int geneLength, Function<byte[], Double> fitnessFunc) {
		individuals = new Individual[populationSize];
		// Initialise population
		// Loop and create individuals
		for (int i = 0; i < size(); i++) {
			Individual newIndividual = new Individual(geneLength, fitnessFunc);
			saveIndividual(i, newIndividual);
		}
	}

	/* Getters */
	public Individual getIndividual(int index) {
		return individuals[index];
	}

	public Individual getBestFittest() {
		Individual fittest = individuals[0];
		// Loop through individuals to find fittest

		for (int i = 0; i < size(); i++) {
			if (fittest.getFitness() <= getIndividual(i).getFitness()) {
				fittest = getIndividual(i);
			}
		}
		return fittest;
	}

	public Individual getSecondFittest() {
		Individual fittest = individuals[0];
		Individual second = individuals[0];
		// Loop through individuals to find fittest

		for (int i = 0; i < size(); i++) {
			if (fittest.getFitness() <= getIndividual(i).getFitness()) {
				second = fittest;
				fittest = getIndividual(i);
			} else if (second.getFitness() <= getIndividual(i).getFitness()) {
				second = getIndividual(i);
			}
		}
		return second;
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

	public Double getMaxFitness() {
		return maxFitness;
	}

	public void setMaxFitness(Double maxFitness) {
		this.maxFitness = maxFitness;
	}

	public boolean isStopConditionReached() {
		return stopConditionReached;
	}

	public void setStopConditionReached(boolean stopConditionReached) {
		this.stopConditionReached = stopConditionReached;
	}
}
