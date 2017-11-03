package com.paduvi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.paduvi.alg.impl.NondominatedSortingGeneticAlgorithm2;
import com.paduvi.entities.Individual;
import com.paduvi.entities.Population;

public class TestNSGA2 {

	public static int binaryToDecimal(byte[] arr) {
		int decimal = 0;
		int p = 0;
		for (int i = arr.length - 2; i >= 0; i--) {
			int temp = arr[i] % 10;
			decimal += temp * Math.pow(2, p);
			p++;
		}
		if (arr[0] == 1)
			decimal = -decimal;
		return decimal;
	}

	private static List<Integer> extractNumber(byte[] arr) {
		List<Integer> list = new ArrayList<>();
		for (int i = 0; i < arr.length; i += 8) {
			byte[] temp = Arrays.copyOfRange(arr, i, i + 7);
			list.add(binaryToDecimal(temp));
		}
		return list;
	}

	private final static Function<byte[], Double> OBJECTIVE_1 = arr -> {
		List<Integer> x = extractNumber(arr);
		int n = x.size();
		double sum = 0;
		for (int i = 0; i < n - 1; i++) {
			sum += -10 * Math.pow(Math.E, -0.2) * Math.sqrt(Math.pow(x.get(i), 2) + Math.pow(x.get(i + 1), 2));
		}
		return sum;
	};

	private final static Function<byte[], Double> OBJECTIVE_2 = arr -> extractNumber(arr).parallelStream()
			.mapToDouble(x -> Math.pow(Math.abs(x), 0.8) + 5 * Math.sin(Math.pow(x, 3))).sum();

	public static void main(String[] args) {
		List<Function<byte[], Double>> fitnessFuncList = new ArrayList<>();
		fitnessFuncList.add(OBJECTIVE_1);
		fitnessFuncList.add(OBJECTIVE_2);

		// Hàm mục tiêu lấy theo bài viết:
		// http://samarkanov.info/blog/optimization-with-nsga-ii-a-simple-web-application.html
		// Với đk: n=2, -64 <=x<= 64. Như vậy gene_length=16
		Population myPop = new Population(1000, 16, fitnessFuncList);

		// Evolve our population until we reach an optimum solution
		int generationCount = 0;
		NondominatedSortingGeneticAlgorithm2 nsga2 = new NondominatedSortingGeneticAlgorithm2(myPop);
		do {
			System.out.println("Generation: " + generationCount);
			generationCount++;
			myPop = nsga2.evolvePopulation();
			Integer[] sortedIndices = nsga2.getSortIndices();
			Individual best = myPop.getIndividual(sortedIndices[0]);
			byte[] gene = new byte[best.size()];
			for (int i = 0; i < best.size(); i++) {
				gene[i] = best.getGene(i);
			}
			System.out.println("Numbers: "
					+ extractNumber(gene).stream().map(x -> String.valueOf(x)).collect(Collectors.joining(", ")));
		} while (!myPop.isStopConditionReached());

		Integer[] sortedIndices = nsga2.getSortIndices();
		Individual best = myPop.getIndividual(sortedIndices[0]);
		System.out.println("Solution found! Fitness: " + Arrays.toString(best.getFitness()));
		System.out.println("Genes: " + best);
		byte[] gene = new byte[best.size()];
		for (int i = 0; i < best.size(); i++) {
			gene[i] = best.getGene(i);
		}
		System.out.println("Numbers: "
				+ extractNumber(gene).stream().map(x -> String.valueOf(x)).collect(Collectors.joining(", ")));
	}

}
