package com.paduvi.alg;

import com.paduvi.alg.ga.Individual;
import com.paduvi.alg.ga.Population;
import com.paduvi.util.Constants;

public class GeneticAlgorithm {
	/* GA parameters */
	private static final double uniformRate = 0.5;
	private static final double mutationRate = 0.015;
	private static final int tournamentSize = 5;

	private Population pop;
	private boolean elitism;

	public static GeneticAlgorithm getInstance(Population pop) {
		return getInstance(pop, true);
	}

	public static GeneticAlgorithm getInstance(Population pop, boolean elitism) {
		return new GeneticAlgorithm(pop, elitism);
	}

	public GeneticAlgorithm(Population pop, boolean elitism) {
		this.pop = pop;
		this.elitism = elitism;
	}

	public GeneticAlgorithm() {

	}

	/* Public methods */
	// Evolve a population
	public Population evolvePopulation() {
		double best = pop.getBestFittest().getFitness();
		double second = pop.getSecondFittest().getFitness();

		Population newPopulation = new Population(pop.size());
		if (pop.getMaxFitness() != null) {
			newPopulation.setMaxFitness(pop.getMaxFitness());
		}

		// Keep our best individual
		if (elitism) {
			newPopulation.saveIndividual(0, pop.getBestFittest());
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

		if (newPopulation.getBestFittest().getFitness() == best
				&& newPopulation.getSecondFittest().getFitness() == second) {
			newPopulation.setStopConditionReached(true);
		}

		if (newPopulation.getBestFittest().getFitness() >= newPopulation.getMaxFitness()) {
			newPopulation.setStopConditionReached(true);
		}

		return newPopulation;
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
				byte gene = (byte) Math.round(Constants.rand(1));
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
		Individual fittest = tournament.getBestFittest();
		return fittest;
	}

	protected Population getPop() {
		return pop;
	}

	public static void main(String[] args) {
		Population myPop = new Population(1000, 50, Constants.fitnessFunc);
		myPop.setMaxFitness(50.);

		// Evolve our population until we reach an optimum solution
		int generationCount = 0;
		do {
			System.out.println("Generation: " + generationCount + " - Fittest: " + myPop.getBestFittest().getFitness());
			generationCount++;
			myPop = GeneticAlgorithm.getInstance(myPop).evolvePopulation();
		} while (!myPop.isStopConditionReached());

		System.out.println("Solution found! Fitness: " + myPop.getBestFittest().getFitness());
		System.out.println("Generation: " + generationCount);
		System.out.println("Genes: " + myPop.getBestFittest());
	}
}
