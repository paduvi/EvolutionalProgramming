package com.paduvi.alg.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import com.paduvi.alg.AbstractGeneticAlgorithm;
import com.paduvi.entities.FrontPoint;
import com.paduvi.entities.Individual;
import com.paduvi.entities.Population;
import com.paduvi.util.Constants;

public class NondominatedSortingGeneticAlgorithm2 extends AbstractGeneticAlgorithm {

	Individual best, second;

	public NondominatedSortingGeneticAlgorithm2(Population pop) {
		this(pop, true);
	}

	public NondominatedSortingGeneticAlgorithm2(Population pop, boolean elitism) {
		this.setPop(pop);
		Integer[] sortIndices = sortResult(pop);
		this.setSortIndices(sortIndices);
		this.setElitism(elitism);

		this.best = pop.getIndividual(sortIndices[0]);
		this.second = pop.getIndividual(sortIndices[1]);
	}

	@Override
	protected Integer[] sortResult(Population pop) {
		List<FrontPoint> solutions = new ArrayList<>();

		List<List<FrontPoint>> fronts = fastNonDominatedSort(pop);
		for (List<FrontPoint> front : fronts) {
			FrontPoint.crowdingDistanceAssignment(front);
			solutions.addAll(front);
		}

		Collections.sort(solutions, Collections.reverseOrder());
		return solutions.stream().map(solution -> solution.getIndice()).toArray(Integer[]::new);
	}

	private List<List<FrontPoint>> fastNonDominatedSort(Population pop) {
		List<List<FrontPoint>> fronts = new ArrayList<>();
		int[] dominateCount = new int[pop.size()];
		Map<Integer, List<Integer>> dominatedMap = new HashMap<>();
		List<FrontPoint> currentFront = new ArrayList<>();
		for (int i = 0; i < pop.size(); i++) {
			for (int j = 0; j < pop.size(); j++) {
				if (i == j)
					continue;
				Individual p = pop.getIndividual(i);
				Individual q = pop.getIndividual(j);
				if (p.rshift(q)) { // p dominate q
					List<Integer> list = dominatedMap.getOrDefault(i, new ArrayList<>());
					list.add(j);
					dominatedMap.put(i, list);
				} else if (p.lshift(q)) {
					dominateCount[i]++;
				}
			}
			if (dominateCount[i] == 0) {
				currentFront.add(new FrontPoint(i, pop.getIndividual(i)));
			}
		}
		fronts.add(currentFront);
		int i = 0;
		while (fronts.get(i).size() != 0) {
			List<FrontPoint> nextFront = new ArrayList<>();
			for (FrontPoint r : fronts.get(i)) {
				for (Integer s : dominatedMap.getOrDefault(r.getIndice(), new ArrayList<>())) {
					dominateCount[s]--;
					if (dominateCount[s] == 0) {
						nextFront.add(new FrontPoint(s, pop.getIndividual(s)));
					}
				}
			}
			i++;
			fronts.add(nextFront);
		}
		return fronts;
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
		return false;
	}

	public static void main(String[] args) {
		List<Function<byte[], Double>> fitnessFuncList = new ArrayList<>();
		fitnessFuncList.add(Constants.fitnessFunc);
		Population myPop = new Population(1000, 50, fitnessFuncList);

		// Evolve our population until we reach an optimum solution
		int generationCount = 0;
		NondominatedSortingGeneticAlgorithm2 nsga2 = new NondominatedSortingGeneticAlgorithm2(myPop);
		do {
			System.out.println("Generation: " + generationCount);
			generationCount++;
			myPop = nsga2.evolvePopulation();
		} while (!myPop.isStopConditionReached());

		Integer[] sortedIndices = nsga2.getSortIndices();
		Individual best = myPop.getIndividual(sortedIndices[0]);
		System.out.println("Solution found! Fitness: " + Arrays.toString(best.getFitness()));
		System.out.println("Genes: " + best);
	}

}
