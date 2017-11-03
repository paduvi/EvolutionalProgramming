package com.paduvi.util;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class SortUtils {

	public static class ArrayIndexComparator<T, V extends Comparable<V>> implements Comparator<Integer> {
		private final List<V> array;
		private boolean isReverse;

		public ArrayIndexComparator(T[] array, Function<T, V> scoreFunc) {
			this(array, scoreFunc, true);
		}

		public ArrayIndexComparator(T[] array, Function<T, V> scoreFunc, boolean isReverse) {
			this.array = Arrays.stream(array).map(scoreFunc).collect(Collectors.toList());
			this.isReverse = isReverse;
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
			if (isReverse)
				return array.get(index2).compareTo(array.get(index1));
			return array.get(index1).compareTo(array.get(index2));
		}
	}
	
}
