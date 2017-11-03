package com.paduvi.alg;

import com.paduvi.entities.Individual;
import com.paduvi.entities.Population;

public abstract class AbstractGeneticAlgorithm {
	/* GA parameters */
	private static final double uniformRate = 0.5;
	private static final double mutationRate = 0.015;
	private static final int tournamentSize = 5;
	private Population pop;
	private Integer[] sortIndices;
	private boolean elitism = true;
	private int convergeLeft = 3;

	public Population evolvePopulation() {
		Population newPopulation = new Population(pop.size());

		// Keep our best individual
		if (elitism) {
			newPopulation.saveIndividual(0, pop.getIndividual(sortIndices[0]));
		}

		// Crossover population
		int elitismOffset;
		if (elitism) {
			elitismOffset = 1;
		} else {
			elitismOffset = 0;
		}
		// Loop over the population size and create new individuals with
		// crossover
		for (int i = elitismOffset; i < pop.size(); i++) {
			Individual indiv1 = tournamentSelection(pop);
			Individual indiv2;
			do {
				indiv2 = tournamentSelection(pop);
			} while (indiv1.equals(indiv2));

			Individual newOffspring = crossover(indiv1, indiv2);
			newPopulation.saveIndividual(i, newOffspring);
		}

		// Mutate population
		for (int i = elitismOffset; i < newPopulation.size(); i++) {
			mutate(newPopulation.getIndividual(i));
		}

		this.sortIndices = sortResult(newPopulation);
		this.pop = newPopulation;

		if (isConverging()) {
			convergeLeft--;
			if (convergeLeft == 0) {
				pop.setStopConditionReached();
			}
			return pop;
		}
		convergeLeft = 3;
		return pop;
	}

	// Crossover individuals
	protected Individual crossover(Individual indiv1, Individual indiv2) {
		Individual newSol = new Individual(indiv1.size(), indiv1.getFitnessFunc());
		// Loop through genes
		for (int i = 0; i < indiv1.size(); i++) {
			// Crossover
			if (Math.random() <= uniformRate) {
				newSol.setGene(i, indiv1.getGene(i));
			} else {
				newSol.setGene(i, indiv2.getGene(i));
			}
		}
		return newSol;
	}

	// Mutate an individual
	protected void mutate(Individual indiv) {
		// Loop through genes
		for (int i = 0; i < indiv.size(); i++) {
			if (Math.random() <= mutationRate) {
				// Create random gene
				byte gene = (byte) Math.round(Math.random());
				indiv.setGene(i, gene);
			}
		}
	}

	// Select individuals for crossover
	protected Individual tournamentSelection(Population pop) {
		// Create a tournament population
		Population tournament = new Population(tournamentSize);
		// For each place in the tournament get a random individual
		for (int i = 0; i < tournamentSize; i++) {
			int randomId = (int) (Math.random() * pop.size());
			tournament.saveIndividual(i, pop.getIndividual(randomId));
		}
		// Get the fittest
		Integer[] indices = sortResult(tournament);
		Individual fittest = tournament.getIndividual(indices[0]);
		return fittest;
	}

	protected void setPop(Population pop) {
		this.pop = pop;
	}

	public Population getPop() {
		return pop;
	}

	protected void setElitism(boolean elitism) {
		this.elitism = elitism;
	}

	protected void setSortIndices(Integer[] sortIndices) {
		this.sortIndices = sortIndices;
	}

	public Integer[] getSortIndices() {
		return sortIndices;
	}

	protected abstract Integer[] sortResult(Population pop);

	protected abstract boolean isConverging();

}
