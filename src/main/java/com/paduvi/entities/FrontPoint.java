package com.paduvi.entities;

import java.util.Arrays;
import java.util.List;

import com.paduvi.util.SortUtils.ArrayIndexComparator;

public class FrontPoint implements Comparable<FrontPoint> {

	private int indice;
	private Double distance = 0.;
	private Individual individual;
	private Integer rank;

	public FrontPoint(int indice, Individual individual, int rank) {
		this.indice = indice;
		this.individual = individual;
		this.rank = rank;
	}

	public static void crowdingDistanceAssignment(List<FrontPoint> front) {
		if (front.size() == 0)
			return;
		int nObjectives = front.get(0).getIndividual().getFitnessFunc().size();
		for (int j = 0; j < nObjectives; j++) {
			Double[] fitnesses = new Double[front.size()];
			for (int i = 0; i < front.size(); i++) {
				fitnesses[i] = front.get(i).getIndividual().getFitness()[j];
			}

			ArrayIndexComparator<Double, Double> comparator = new ArrayIndexComparator<>(fitnesses, x -> x);
			Integer[] indices = comparator.createIndexArray();
			Arrays.sort(indices, comparator);

			double min = fitnesses[indices[0]];
			double max = fitnesses[indices[indices.length - 1]];

			// Boundary Points always selected
			front.get(indices[0]).setDistance(Double.POSITIVE_INFINITY);
			front.get(indices[indices.length - 1]).setDistance(Double.POSITIVE_INFINITY);

			for (int k = 1; k < indices.length - 1; k++) {
				int idx = indices[k];
				double distance = front.get(idx).getDistance()
						+ (fitnesses[indices[k + 1]] - fitnesses[indices[k - 1]]) / (max - min);
				front.get(idx).setDistance(distance);
			}
		}
	}

	public double getDistance() {
		return distance;
	}

	public void setDistance(double distance) {
		this.distance = distance;
	}

	public int getIndice() {
		return indice;
	}

	public Individual getIndividual() {
		return individual;
	}

	@Override
	public int compareTo(FrontPoint o) {
		if (this.rank.equals(o.rank))
			return o.distance.compareTo(this.distance);
		return this.rank.compareTo(o.rank);
	}
}
