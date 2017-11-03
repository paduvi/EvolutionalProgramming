package com.paduvi.alg;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import com.paduvi.alg.ga.Individual;
import com.paduvi.alg.ga.Population;
import com.paduvi.util.Constants;

public class NondominatedSortingGeneticAlgorithm2 extends GeneticAlgorithm {
	private Population pop;
	private Function<double[], Double> combineFitnessFunc;
	private Integer[] sortIndices;

	public static NondominatedSortingGeneticAlgorithm2 getInstance(Population pop) {
		return new NondominatedSortingGeneticAlgorithm2(pop);
	}

	public NondominatedSortingGeneticAlgorithm2(Population pop) {
		this.pop = pop;
	}

	@Override
	/* Public methods */
	// Evolve a population
	public Population evolvePopulation() {
		double best = combineFitnessFunc.apply(pop.getIndividual(sortIndices[0]).getFitness());
		double second = combineFitnessFunc.apply(pop.getIndividual(sortIndices[1]).getFitness());

		Population newPopulation = new Population(pop.size());

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

		this.sortIndices = sortResult(newPopulation);

		if (combineFitnessFunc.apply(newPopulation.getIndividual(sortIndices[0]).getFitness()) == best
				&& combineFitnessFunc.apply(newPopulation.getIndividual(sortIndices[1]).getFitness()) == second) {
			newPopulation.setStopConditionReached(true);
		}

		this.pop = newPopulation;
		return newPopulation;
	}

	// private Population fastNonDominatedSort(Population pop) {
	// for (int i = 0; i < pop.size(); i++) {
	// for (int j = 0; j < pop.size(); j++) {
	// if (i == j)
	// continue;
	// if (pop.getIndividual(i).getFitness() ==
	// pop.getIndividual(j).getFitness())
	// continue;
	// }
	// }
	// }

	public static void main(String[] args) {
		List<Function<byte[], Double>> fitnessFuncList = new ArrayList<>();
		fitnessFuncList.add(Constants.fitnessFunc);
		Population myPop = new Population(1000, 50, fitnessFuncList);

		// Evolve our population until we reach an optimum solution
		int generationCount = 0;
		NondominatedSortingGeneticAlgorithm2 nsga2 = new NondominatedSortingGeneticAlgorithm2(myPop);
		do {
			Integer[] sortedIndices = nsga2.getSortIndices();
			Individual best = myPop.getIndividual(sortedIndices[0]);
			System.out.println("Generation: " + generationCount + " - Fittest: "
					+ nsga2.getCombineFitnessFunc().apply(best.getFitness()));
			generationCount++;
			myPop = nsga2.evolvePopulation();
		} while (!myPop.isStopConditionReached());

		Integer[] sortedIndices = nsga2.getSortIndices();
		Individual best = myPop.getIndividual(sortedIndices[0]);
		System.out.println("Solution found! Fitness: " + nsga2.getCombineFitnessFunc().apply(best.getFitness()));
		System.out.println("Generation: " + generationCount);
		System.out.println("Genes: " + best);
	}
}
