package com.paduvi.alg;

import com.paduvi.alg.ga.Individual;
import com.paduvi.alg.ga.Population;
import com.paduvi.util.Constants;

public class NondominatedSortingGeneticAlgorithm2 extends GeneticAlgorithm {
	private Population pop;

	public static NondominatedSortingGeneticAlgorithm2 getInstance(Population pop) {
		return new NondominatedSortingGeneticAlgorithm2(pop);
	}

	public NondominatedSortingGeneticAlgorithm2(Population pop) {
		this.pop = pop;
	}

	@Override
	public Population evolvePopulation() {
		double best = pop.getBestFittest().getFitness();
		double second = pop.getSecondFittest().getFitness();

		Population newPopulation = new Population(pop.size());
		if (pop.getMaxFitness() != null) {
			newPopulation.setMaxFitness(pop.getMaxFitness());
		}

		// Crossover population
		// Loop over the population size and create new individuals with
		// crossover
		for (int i = 0; i < pop.size(); i++) {
			Individual indiv1 = tournamentSelection(pop);
			Individual indiv2;
			do {
				indiv2 = tournamentSelection(pop);
			} while (indiv1.equals(indiv2));

			Individual newOffspring = crossover(indiv1, indiv2);
			newPopulation.saveIndividual(i, newOffspring);
		}

		// Mutate population
		for (int i = 0; i < newPopulation.size(); i++) {
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

	public static void main(String[] args) {
		Population myPop = new Population(1000, 50, Constants.fitnessFunc);
		myPop.setMaxFitness(50.);

		// Evolve our population until we reach an optimum solution
		int generationCount = 0;
		do {
			System.out.println("Generation: " + generationCount + " - Fittest: " + myPop.getBestFittest().getFitness());
			generationCount++;
			myPop = NondominatedSortingGeneticAlgorithm2.getInstance(myPop).evolvePopulation();
		} while (!myPop.isStopConditionReached());

		System.out.println("Solution found! Fitness: " + myPop.getBestFittest().getFitness());
		System.out.println("Generation: " + generationCount);
		System.out.println("Genes: " + myPop.getBestFittest());
	}
}
