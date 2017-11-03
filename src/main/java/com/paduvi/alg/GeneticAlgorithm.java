package com.paduvi.alg;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.IntStream;

import com.paduvi.alg.ga.Individual;
import com.paduvi.alg.ga.Population;
import com.paduvi.util.Constants;
import com.paduvi.util.SortUtils.ArrayIndexComparator;

public class GeneticAlgorithm {
	/* GA parameters */
	private static final double uniformRate = 0.5;
	private static final double mutationRate = 0.015;
	private static final int tournamentSize = 5;

	private Population pop;
	private boolean elitism;
	private Function<double[], Double> combineFitnessFunc;
	private Integer[] sortIndices;
	private Double maxFitness;

	public GeneticAlgorithm() {
	}

	public GeneticAlgorithm(Population pop) {
		this(pop, (Function<double[], Double>) arr -> Arrays.stream(arr).sum());
	}

	public GeneticAlgorithm(Population pop, Function<double[], Double> combineFitnessFunc) {
		this(pop, combineFitnessFunc, null);
	}

	public GeneticAlgorithm(Population pop, Function<double[], Double> combineFitnessFunc, Double maxFitness) {
		this(pop, combineFitnessFunc, maxFitness, true);
	}

	public GeneticAlgorithm(Population pop, Function<double[], Double> combineFitnessFunc, Double maxFitness,
			boolean elitism) {
		this.pop = pop;
		this.combineFitnessFunc = combineFitnessFunc;
		this.maxFitness = maxFitness;
		this.elitism = elitism;
		this.sortIndices = sortResult(pop);
	}

	/* Public methods */
	// Evolve a population
	public Population evolvePopulation() {
		double best = combineFitnessFunc.apply(pop.getIndividual(sortIndices[0]).getFitness());
		double second = combineFitnessFunc.apply(pop.getIndividual(sortIndices[1]).getFitness());

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

		if (combineFitnessFunc.apply(newPopulation.getIndividual(sortIndices[0]).getFitness()) == best
				&& combineFitnessFunc.apply(newPopulation.getIndividual(sortIndices[1]).getFitness()) == second) {
			newPopulation.setStopConditionReached(true);
		}

		if (maxFitness != null
				&& combineFitnessFunc.apply(newPopulation.getIndividual(sortIndices[0]).getFitness()) >= maxFitness) {
			newPopulation.setStopConditionReached(true);
		}

		this.pop = newPopulation;
		return newPopulation;
	}

	protected Integer[] sortResult(Population pop) {
		double[][] fitnesses = IntStream.rangeClosed(0, pop.size() - 1)
				.mapToObj(idx -> pop.getIndividual(idx).getFitness()).toArray(double[][]::new);
		ArrayIndexComparator<double[], Double> comparator = new ArrayIndexComparator<>(fitnesses, combineFitnessFunc);
		Integer[] indices = comparator.createIndexArray();
		Arrays.sort(indices, comparator);
		return indices;
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

	public Population getPop() {
		return pop;
	}

	public Integer[] getSortIndices() {
		return sortIndices;
	}

	public Function<double[], Double> getCombineFitnessFunc() {
		return combineFitnessFunc;
	}

	public static void main(String[] args) {
		List<Function<byte[], Double>> fitnessFuncList = new ArrayList<>();
		fitnessFuncList.add(Constants.fitnessFunc);
		Population myPop = new Population(1000, 50, fitnessFuncList);

		// Evolve our population until we reach an optimum solution
		int generationCount = 0;
		GeneticAlgorithm ga = new GeneticAlgorithm(myPop);
		do {
			Integer[] sortedIndices = ga.getSortIndices();
			Individual best = myPop.getIndividual(sortedIndices[0]);
			System.out.println("Generation: " + generationCount + " - Fittest: "
					+ ga.getCombineFitnessFunc().apply(best.getFitness()));
			generationCount++;
			myPop = ga.evolvePopulation();
		} while (!myPop.isStopConditionReached());

		Integer[] sortedIndices = ga.getSortIndices();
		Individual best = myPop.getIndividual(sortedIndices[0]);
		System.out.println("Solution found! Fitness: " + ga.getCombineFitnessFunc().apply(best.getFitness()));
		System.out.println("Generation: " + generationCount);
		System.out.println("Genes: " + best);
	}
}
