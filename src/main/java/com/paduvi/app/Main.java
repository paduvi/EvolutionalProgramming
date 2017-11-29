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
import com.paduvi.app.models.Pack;
import com.paduvi.app.models.Product;
import com.paduvi.app.models.Project;
import com.paduvi.app.models.Timestamp;
import com.paduvi.util.ProcessUtils;

public class Main {

	private Project project;
	private List<Contractor> contractors;
	private long rangeOfDay;
	private Population pop;
	private NondominatedSortingGeneticAlgorithm2 ga;

	private static final int MAX_EPOCH = 300;

	/*
	 * We need to minimize all objectives
	 * 
	 * Get minimum project pay. Constraint: sum_sell <= estimated_cost
	 */
	private Function<byte[], Double> OBJECTIVE_1 = arr -> {
		List<Solution> solutions = getValidSolution(arr);
		if (solutions == null)
			return Double.MAX_VALUE;
		double sumPaid = IntStream.range(0, project.getPackages().size()).parallel().mapToDouble(i -> {
			Contractor contractor = solutions.get(i).getContractor();
			Timestamp executedDate = solutions.get(i).getExecutedDate();
			Pack pkg = project.getPackages().get(i);
			double sumSell = pkg.getProducts().parallelStream().mapToDouble(p -> {
				int productId = p.getProductId();
				Product temp = contractor.getProducts().parallelStream().filter(p1 -> p1.getProductId() == productId)
						.findAny().get();
				return (1 - temp.getDiscountRate(executedDate)) * temp.getSellPrice() * p.getQuantity();
			}).sum();
			if (sumSell > pkg.getEstimatedCost())
				return Double.MAX_VALUE;
			return sumSell
					* Math.exp(project.getInflationRate() * (executedDate.get() - project.getStartDate().get()) / 7);
		}).sum();
		return sumPaid;
	};

	/*
	 * Get maximum contractor's quality as we can.
	 * 
	 * As higher the package is, more important quality is.
	 */
	private Function<byte[], Double> OBJECTIVE_2 = arr -> {
		List<Solution> solutions = getValidSolution(arr);
		if (solutions == null)
			return Double.MAX_VALUE;

		double sumQuality = solutions.parallelStream().mapToDouble(solution -> solution.getContractor().getQuality())
				.sum();
		return 1 / sumQuality;
	};

	/*
	 * Get maximum contractor's revenue as we can.
	 */
	private Function<byte[], Double> OBJECTIVE_3 = arr -> {
		List<Solution> solutions = getValidSolution(arr);
		if (solutions == null)
			return Double.MAX_VALUE;
		double[] listRevenues = new double[contractors.size()];

		IntStream.range(0, project.getPackages().size()).parallel().forEach(i -> {
			Contractor contractor = solutions.get(i).getContractor();
			Timestamp executedDate = solutions.get(i).getExecutedDate();
			Pack pkg = project.getPackages().get(i);
			double differentPrice = pkg.getProducts().parallelStream().mapToDouble(p -> {
				int productId = p.getProductId();
				Product temp = contractor.getProducts().parallelStream().filter(p1 -> p1.getProductId() == productId)
						.findAny().get();
				return ((1 - temp.getDiscountRate(executedDate)) * temp.getSellPrice() - temp.getBuyPrice())
						* p.getQuantity();
			}).sum();
			listRevenues[contractor.getContractorId()] += differentPrice
					* Math.exp(project.getInflationRate() * (executedDate.get() - project.getStartDate().get()) / 7)
					* contractor.getRelationship();
		});

		double sum = 0;
		for (int i = 0; i < contractors.size() - 1; i++) {
			for (int j = i; j < contractors.size(); j++) {
				sum += Math.abs(listRevenues[i] - listRevenues[j]);
			}
		}
		return sum;
	};

	public Main(Project project, List<Contractor> contractors) {
		this.project = project;
		this.contractors = contractors;

		this.rangeOfDay = project.getPackages().parallelStream()
				.mapToLong(p -> p.getTimeline().getTo().get() - p.getTimeline().getFrom().get()).max().getAsLong();

		List<Function<byte[], Double>> fitnessFuncList = new ArrayList<>();
		fitnessFuncList.add(OBJECTIVE_1);
		fitnessFuncList.add(OBJECTIVE_2);
		fitnessFuncList.add(OBJECTIVE_3);
		for (Contractor contractor : contractors) {
			fitnessFuncList.add(arr -> {
				List<Solution> solutions = getValidSolution(arr);
				if (solutions == null)
					return Double.MAX_VALUE;

				return IntStream.range(0, project.getPackages().size()).parallel().mapToDouble(i -> {
					if (solutions.get(i).getContractor().getContractorId() != contractor.getContractorId()) {
						return 0;
					}
					Timestamp executedDate = solutions.get(i).getExecutedDate();
					Pack pkg = project.getPackages().get(i);
					double differentPrice = pkg.getProducts().parallelStream().mapToDouble(p -> {
						int productId = p.getProductId();
						Product temp = contractor.getProducts().parallelStream()
								.filter(p1 -> p1.getProductId() == productId).findAny().get();
						return ((1 - temp.getDiscountRate(executedDate)) * temp.getSellPrice() - temp.getBuyPrice())
								* p.getQuantity();
					}).sum();
					return differentPrice * Math
							.exp(project.getInflationRate() * (executedDate.get() - project.getStartDate().get()) / 7);
				}).sum();
			});
		}

		this.setPop(new Population(1000,
				(ProcessUtils.maxBitCount(contractors.size()) + ProcessUtils.maxBitCount(rangeOfDay))
						* project.getPackages().size(),
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
			List<Solution> solutions = getValidSolution(genes);
			if (solutions == null) {
				System.out.println("Invalid solution");
			} else {
				System.out.println("Contractor IDs: "
						+ solutions.parallelStream().map(x -> String.valueOf(x.getContractor().getContractorId()))
								.collect(Collectors.joining(", ")));
				System.out.println("Executed Date: " + solutions.parallelStream()
						.map(x -> String.valueOf(x.getExecutedDate().toString())).collect(Collectors.joining(", ")));
			}

		} while (!pop.isStopConditionReached() && generationCount < MAX_EPOCH);
	}

	private List<Solution> getValidSolution(byte[] arr) {
		List<Integer> chosenContractors = ProcessUtils.extractContractor(arr,
				ProcessUtils.maxBitCount(contractors.size()) + ProcessUtils.maxBitCount(rangeOfDay),
				ProcessUtils.maxBitCount(contractors.size()));
		List<Integer> days = ProcessUtils.extractDay(arr,
				ProcessUtils.maxBitCount(contractors.size()) + ProcessUtils.maxBitCount(rangeOfDay),
				ProcessUtils.maxBitCount(contractors.size()));
		boolean isInvalidSolution = IntStream.range(0, project.getPackages().size()).parallel().anyMatch(i -> {
			Pack pkg = project.getPackages().get(i);
			Integer day = days.get(i);
			return day > (pkg.getTimeline().getTo().get() - pkg.getTimeline().getFrom().get());
		});
		if (isInvalidSolution)
			return null;
		isInvalidSolution = IntStream.range(0, project.getPackages().size()).parallel().anyMatch(i -> {
			Contractor contractor = contractors.get(chosenContractors.get(i));
			Pack pkg = project.getPackages().get(i);
			return !pkg.getJoinedContractors().contains(contractor.getContractorId());
		});
		if (isInvalidSolution)
			return null;
		return IntStream.range(0, project.getPackages().size()).parallel().mapToObj(i -> {
			Pack pkg = project.getPackages().get(i);
			Integer day = days.get(i);
			Contractor contractor = contractors.get(chosenContractors.get(i));
			return new Solution(contractor, new Timestamp(pkg.getTimeline().getFrom().get() + day));
		}).collect(Collectors.toList());
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
		List<Solution> solutions = getValidSolution(genes);
		System.out.println("Numbers: " + solutions.parallelStream()
				.map(x -> String.valueOf(x.getContractor().getContractorId())).collect(Collectors.joining(", ")));
		System.out.println("=======================================");
		IntStream.range(0, project.getPackages().size()).forEach(i -> {
			Contractor contractor = solutions.get(i).getContractor();
			Timestamp executedDate = solutions.get(i).getExecutedDate();
			Pack pkg = project.getPackages().get(i);
			System.out.println("Goi thau: " + pkg.getPackageId() + " - Nha thau: " + contractor.getDescription()
					+ " - Ngay: " + executedDate.toString());
			double revenue = pkg.getProducts().parallelStream().mapToDouble(p -> {
				int productId = p.getProductId();
				Product temp = contractor.getProducts().parallelStream().filter(p1 -> p1.getProductId() == productId)
						.findAny().get();
				return ((1 - temp.getDiscountRate(executedDate)) * temp.getSellPrice() - temp.getBuyPrice())
						* p.getQuantity();
			}).sum();
			System.out.println("Loi nhuan nha thau: " + revenue
					* Math.exp(project.getInflationRate() * (executedDate.get() - project.getStartDate().get()) / 7));
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

	public class Solution {
		private Contractor contractor;
		private Timestamp executedDate;

		public Solution(Contractor contractor, Timestamp executedDate) {
			this.contractor = contractor;
			this.executedDate = executedDate;
		}

		public Contractor getContractor() {
			return this.contractor;
		}

		public Timestamp getExecutedDate() {
			return this.executedDate;
		}
	}
}
