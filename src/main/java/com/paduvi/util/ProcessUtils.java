package com.paduvi.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ProcessUtils {

	public static byte[] makeOneHotEncode(int value, int size) {
		byte[] arr = new byte[size];
		if (value > 0)
			arr[value - 1] = 1;
		return arr;
	}

	public static int makeOneHotDecode(byte[] arr) {
		for (int i = 0; i < arr.length; i++) {
			if (arr[i] == 1) {
				return i + 1;
			}
		}
		return 0;
	}

	public static List<Integer> extractNumber(byte[] arr, int geneSize) {
		List<Integer> list = new ArrayList<>();
		for (int i = 0; i < arr.length; i += geneSize) {
			byte[] temp = Arrays.copyOfRange(arr, i, i + geneSize - 1);
			list.add(makeOneHotDecode(temp));
		}
		return list;
	}
}
