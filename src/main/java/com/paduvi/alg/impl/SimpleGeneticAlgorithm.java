package com.paduvi.alg.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.IntStream;

import com.paduvi.alg.AbstractGeneticAlgorithm;
import com.paduvi.alg.entities.Individual;
import com.paduvi.alg.entities.Population;
import com.paduvi.util.Constants;
import com.paduvi.util.SortUtils.ArrayIndexComparator;

public class SimpleGeneticAlgorithm extends AbstractGeneticAlgorithm {

	private Function<double[], Double> combineFitnessFunc;
	private Double maxFitness;
	Individual best, second;

	public SimpleGeneticAlgorithm(Population pop) {
		this(pop, (Function<double[], Double>) arr -> Arrays.stream(arr).sum());
	}

	public SimpleGeneticAlgorithm(Population pop, Double maxFitness) {
		this(pop, (Function<double[], Double>) arr -> Arrays.stream(arr).sum(), maxFitness);
	}

	public SimpleGeneticAlgorithm(Population pop, Function<double[], Double> combineFitnessFunc) {
		this(pop, combineFitnessFunc, null);
	}

	public SimpleGeneticAlgorithm(Population pop, Function<double[], Double> combineFitnessFunc, Double maxFitness) {
		this(pop, combineFitnessFunc, maxFitness, true);
	}

	public SimpleGeneticAlgorithm(Population pop, Function<double[], Double> combineFitnessFunc, Double maxFitness,
			boolean elitism) {
		this.setPop(pop);
		this.combineFitnessFunc = combineFitnessFunc;
		this.maxFitness = maxFitness;
		this.setElitism(elitism);
		Integer[] sortIndices = sortResult(pop);
		this.setSortIndices(sortIndices);

		this.best = pop.getIndividual(sortIndices[0]);
		this.second = pop.getIndividual(sortIndices[1]);
	}

	@Override
	protected Integer[] sortResult(Population pop) {
		double[][] fitnesses = IntStream.rangeClosed(0, pop.size() - 1)
				.mapToObj(idx -> pop.getIndividual(idx).getFitness()).toArray(double[][]::new);
		ArrayIndexComparator<double[], Double> comparator = new ArrayIndexComparator<>(fitnesses, combineFitnessFunc);
		Integer[] indices = comparator.createIndexArray();
		Arrays.sort(indices, comparator);
		return indices;
	}

	@Override
	protected boolean isConverging() {
		Population pop = getPop();
		Integer[] sortIndices = getSortIndices();

		Individual best = pop.getIndividual(sortIndices[0]);
		Individual second = pop.getIndividual(sortIndices[1]);

		if (best.equals(this.best)) {
			this.best = best;
			this.second = second;
			return true;
		}
		this.best = best;
		this.second = second;
		if (best.equals(second)) {
			return true;
		}
		if (maxFitness != null && combineFitnessFunc.apply(best.getFitness()) <= 1 / maxFitness) {
			return true;
		}
		return false;
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
		SimpleGeneticAlgorithm ga = new SimpleGeneticAlgorithm(myPop, 50.);
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
		System.out.println("Genes: " + best);
	}

}
