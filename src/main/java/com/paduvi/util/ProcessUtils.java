package com.paduvi.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ProcessUtils {

	public static byte[] makeOneHotEncode(int value, int size) {
		byte[] arr = new byte[size];
		arr[value] = 1;
		return arr;
	}

	public static int makeOneHotDecode(byte[] arr) {
		for (int i = 0; i < arr.length; i++) {
			if (arr[i] == 1) {
				return i;
			}
		}
		return 0;
	}

	public static List<Integer> extractContractor(byte[] arr, int geneSize, int bitCount) {
		List<Integer> list = new ArrayList<>();
		for (int i = 0; i < arr.length; i += geneSize) {
			byte[] temp = Arrays.copyOfRange(arr, i, i + bitCount);
			assert bitCount == temp.length;
			list.add(makeBinaryDecode(temp));
		}
		return list;
	}

	public static List<Integer> extractDay(byte[] arr, int geneSize, int bitCount) {
		List<Integer> list = new ArrayList<>();
		for (int i = 0; i < arr.length; i += geneSize) {
			byte[] temp = Arrays.copyOfRange(arr, i + bitCount, i + geneSize);
			assert (geneSize - bitCount) == temp.length;
			list.add(makeBinaryDecode(temp));
		}
		return list;
	}

	public static byte[] makeBinaryEncode(int value, int size) {
		assert Integer.bitCount(value) <= size;
		byte[] result = new byte[size];
		for (int i = 0; i < size; ++i, value /= 2) {
			result[size - 1 - i] = (byte) (value % 2);
		}
		return result;
	}

	public static int makeBinaryDecode(byte[] value) {
		int decimal = 0;
		for (int i = value.length - 1, p = 1; i >= 0; i--) {
			int temp = value[i] % 10;
			decimal += temp * p;
			p *= 2;
		}
		return decimal;
	}

	public static int maxBitCount(long value) {
		return (int) (Math.floor(Math.log(value) / Math.log(2)));
	}
}
