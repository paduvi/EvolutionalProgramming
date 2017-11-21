package com.paduvi.app;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.paduvi.alg.entities.Individual;
import com.paduvi.alg.entities.Population;
import com.paduvi.alg.impl.NondominatedSortingGeneticAlgorithm2;
import com.paduvi.app.models.Contractor;
import com.paduvi.app.models.Owner;
import com.paduvi.app.models.Pack;
import com.paduvi.util.ProcessUtils;

public class Main {

	private Owner owner;
	private List<Contractor> contractors;
	private Population pop;
	private NondominatedSortingGeneticAlgorithm2 ga;

	private static final int MAX_EPOCH = 200;

	/*
	 * We need to minimize all objectives
	 * 
	 * Get minimum owner pay. Constraint: sum_sell <= estimated_cost
	 */
	private Function<byte[], Double> OBJECTIVE_1 = arr -> {
		List<Integer> solution = getValidSolution(arr);
		if (solution == null)
			return Double.MAX_VALUE;
		double sumPaid = IntStream.range(0, owner.getPackages().size()).parallel().mapToDouble(i -> {
			Contractor contractor = contractors.get(solution.get(i));
			Pack pkg = owner.getPackages().get(i);
			double sumSell = contractor.getPackageById(pkg.getId()).getProducts().parallelStream()
					.mapToDouble(p -> p.getSell() * p.getnUnits()).sum();
			return (sumSell > pkg.getEstimatedCost()) ? Double.MAX_VALUE : sumSell;
		}).sum();
		return sumPaid;
	};

	/*
	 * Get maximum contractor's relationship as we can.
	 */
	private Function<byte[], Double> OBJECTIVE_2 = arr -> {
		List<Integer> solution = getValidSolution(arr);
		if (solution == null)
			return Double.MAX_VALUE;
		double sumRelation = solution.parallelStream().mapToDouble(idx -> contractors.get(idx).getRelationship()).sum();
		return 1 / sumRelation;
	};

	/*
	 * Get maximum contractor's quality as we can.
	 * 
	 * As higher the package is, more important quality is.
	 */
	private Function<byte[], Double> OBJECTIVE_3 = arr -> {
		List<Integer> solution = getValidSolution(arr);
		if (solution == null)
			return Double.MAX_VALUE;
		double sumQuality = IntStream.range(0, owner.getPackages().size()).parallel().mapToDouble(i -> {
			Contractor contractor = contractors.get(solution.get(i));
			Pack pkg = owner.getPackages().get(i);
			return contractor.getQuality() * pkg.getEstimatedCost();
		}).sum();
		return 1 / sumQuality;
	};

	/*
	 * Get maximum contractor's revenue as we can.
	 */
	private Function<byte[], Double> OBJECTIVE_4 = arr -> {
		List<Integer> solution = getValidSolution(arr);
		if (solution == null)
			return Double.MAX_VALUE;
		double sumRevenue = IntStream.range(0, owner.getPackages().size()).parallel().mapToDouble(i -> {
			Contractor contractor = contractors.get(solution.get(i));
			Pack pkg = owner.getPackages().get(i);
			return contractor.getPackageById(pkg.getId()).getProducts().parallelStream()
					.mapToDouble(p -> (p.getSell() - p.getBuy()) * p.getnUnits()).sum();
		}).sum();
		return 1 / sumRevenue;
	};

	public Main(Owner owner, List<Contractor> contractors) {
		this.owner = owner;
		this.contractors = contractors;

		List<Function<byte[], Double>> fitnessFuncList = new ArrayList<>();
		fitnessFuncList.add(OBJECTIVE_1);
		fitnessFuncList.add(OBJECTIVE_2);
		fitnessFuncList.add(OBJECTIVE_3);
		fitnessFuncList.add(OBJECTIVE_4);

		this.setPop(new Population(20, contractors.size(), contractors.size() * owner.getPackages().size(),
				fitnessFuncList));
		this.setGa(new NondominatedSortingGeneticAlgorithm2(pop));

		// Evolve our population until we reach an optimum solution
		int generationCount = 0;
		do {
			System.out.println("Generation: " + generationCount);
			generationCount++;
			this.setPop(ga.evolvePopulation());
			Integer[] sortedIndices = ga.getSortIndices();
			Individual best = pop.getIndividual(sortedIndices[0]);
			byte[] genes = new byte[best.size()];
			for (int i = 0; i < best.size(); i++) {
				genes[i] = best.getGene(i);
			}
			System.out.println("Numbers: " + ProcessUtils.extractNumber(genes, contractors.size()).stream()
					.map(x -> String.valueOf(x)).collect(Collectors.joining(", ")));
		} while (!pop.isStopConditionReached() && generationCount < MAX_EPOCH);
	}

	private List<Integer> getValidSolution(byte[] arr) {
		List<Integer> solution = ProcessUtils.extractNumber(arr, contractors.size());
		boolean isInvalidSolution = IntStream.range(0, owner.getPackages().size()).parallel().anyMatch(i -> {
			Contractor contractor = contractors.get(solution.get(i));
			Pack pkg = owner.getPackages().get(i);
			return !contractor.hasPackage(pkg.getId());
		});
		return isInvalidSolution ? null : solution;
	}

	public void printResult() {
		Integer[] sortedIndices = ga.getSortIndices();
		Individual best = pop.getIndividual(sortedIndices[0]);
		System.out.println("Solution found! Fitness: " + Arrays.toString(best.getFitness()));
		// System.out.println("Genes: " + best);
		byte[] genes = new byte[best.size()];
		for (int i = 0; i < best.size(); i++) {
			genes[i] = best.getGene(i);
		}
		List<Integer> solution = getValidSolution(genes);
		System.out
				.println("Numbers: " + solution.stream().map(x -> String.valueOf(x)).collect(Collectors.joining(", ")));
		System.out.println("=======================================");
		IntStream.range(0, owner.getPackages().size()).forEach(i -> {
			Contractor contractor = contractors.get(solution.get(i));
			Pack pkg = owner.getPackages().get(i);
			System.out.println("Goi thau: " + pkg.getDescription() + " - Nha thau: " + contractor.getName());
			double revenue = contractor.getPackageById(pkg.getId()).getProducts().parallelStream()
					.mapToDouble(p -> (p.getSell() - p.getBuy()) * p.getnUnits()).sum();
			System.out.println("Loi nhuan nha thau: " + revenue);
		});

	}

	public Population getPop() {
		return pop;
	}

	private void setPop(Population pop) {
		this.pop = pop;
	}

	public NondominatedSortingGeneticAlgorithm2 getGa() {
		return ga;
	}

	private void setGa(NondominatedSortingGeneticAlgorithm2 ga) {
		this.ga = ga;
	}

}
