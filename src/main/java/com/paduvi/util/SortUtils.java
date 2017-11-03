package com.paduvi.util;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class SortUtils {

	public static class ArrayIndexComparator<T, V extends Comparable<V>> implements Comparator<Integer> {
		private final List<V> array;
		private boolean asc;

		public ArrayIndexComparator(T[] array, Function<T, V> scoreFunc) {
			this(array, scoreFunc, true);
		}

		public ArrayIndexComparator(T[] array, Function<T, V> scoreFunc, boolean asc) {
			this.array = Arrays.stream(array).map(scoreFunc).collect(Collectors.toList());
			this.asc = asc;
		}

		public Integer[] createIndexArray() {
			Integer[] indexes = new Integer[array.size()];
			for (int i = 0; i < array.size(); i++) {
				indexes[i] = i; // Autoboxing
			}
			return indexes;
		}

		@Override
		public int compare(Integer index1, Integer index2) {
			// Autounbox from Integer to int to use as array indexes
			if (asc)
				return array.get(index1).compareTo(array.get(index2));
			return array.get(index2).compareTo(array.get(index1));
		}
	}

	public static class ListIndexComparator<T, V extends Comparable<V>> implements Comparator<Integer> {
		private final List<V> array;
		private boolean asc;

		public ListIndexComparator(List<T> list, Function<T, V> scoreFunc) {
			this(list, scoreFunc, true);
		}

		public ListIndexComparator(List<T> list, Function<T, V> scoreFunc, boolean asc) {
			this.array = list.stream().map(scoreFunc).collect(Collectors.toList());
			this.asc = asc;
		}

		public Integer[] createIndexArray() {
			Integer[] indexes = new Integer[array.size()];
			for (int i = 0; i < array.size(); i++) {
				indexes[i] = i; // Autoboxing
			}
			return indexes;
		}

		@Override
		public int compare(Integer index1, Integer index2) {
			// Autounbox from Integer to int to use as array indexes
			if (asc)
				return array.get(index1).compareTo(array.get(index2));
			return array.get(index2).compareTo(array.get(index1));
		}
	}

}
