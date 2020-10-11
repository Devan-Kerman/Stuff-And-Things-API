package net.devtech.snt.api.util;

import net.minecraft.nbt.IntArrayTag;

public class FluidUtil {
	/**
	 * <b>THIS DENOMINATOR IS NOT FINAL, IT MAY CHANGE IN THE FUTURE</b>
	 *
	 * denominator: 648, should cover all vanilla recipes, and is granular enough for approximations to still be ok
	 *
	 * @see #safeFromTag(IntArrayTag)
	 * @see #safeToTag(int)
	 */
	public static final int DENOMINATOR = 3 * 3 * 3 * 3 * 2 * 2 * 2;
	/**
	 * the fixed denominator as a double
	 */
	public static final double DOUBLE_DENOMINATOR = DENOMINATOR;

	public static final int ONE_THIRD = exactFraction(1, 3);
	public static final int ONE_NINTH = exactFraction(1, 9);

	public static final int ONE_HALF = exactFraction(1, 4);
	public static final int ONE_FOURTH = exactFraction(1, 4);
	public static final int ONE_EIGHTH = exactFraction(1, 4);


	private static final int SMALLEST = Integer.MAX_VALUE / DENOMINATOR;

	public static int floorDiv(int a, int b) {
		return Math.floorDiv(a, b);
	}

	public static int floorToBucket(int drops) {
		return floorDiv(drops, DENOMINATOR) * DENOMINATOR;
	}

	public static int exactFraction(int buckets, int divisor) throws ArithmeticException {
		validate(buckets, divisor);
		int i = Math.multiplyExact(buckets, DENOMINATOR);
		if (i % divisor != 0) {
			throw new ArithmeticException(DENOMINATOR + " x " + buckets + " / " + divisor + " is an invalid fraction!");
		}
		return i / divisor;
	}

	/**
	 * get the number of drops for an amount of buckets without overflowing
	 */
	public static int fuzzyBucket(int buckets) {
		if(buckets >= SMALLEST) return Integer.MAX_VALUE;
		return DENOMINATOR * buckets;
	}

	public static int fuzzyFraction(int buckets, int divisor) {
		validate(buckets, divisor);
		// check for overflow
		if(buckets > SMALLEST) {
			if(DENOMINATOR % divisor == 0) {
				int max = (DENOMINATOR / divisor);
				if(buckets > Integer.MAX_VALUE / max) {
					return Integer.MAX_VALUE;
				}
				return max * buckets;
			}
			return Integer.MAX_VALUE;
		}
		return buckets * DENOMINATOR / divisor;
	}

	public static void validate(int numerator, int denominator) {
		if(denominator == 0) throw new IllegalArgumentException("denominator cannot be 0!");
		if(numerator < 0 || denominator < 0) throw new IllegalArgumentException("positive numbers only!");
	}

	public static int getFraction(int buckets, int divisor) {
		validate(buckets, divisor);
		// (denominator * buckets) / divisor
		return buckets * DENOMINATOR / divisor;
	}

	public static IntArrayTag safeToTag(int amount) {
		return new IntArrayTag(new int[] {
				amount,
				DENOMINATOR
		});
	}

	public static int safeFromTag(IntArrayTag amount) {
		int[] arr = amount.getIntArray();
		// new * value / old
		long mul = DENOMINATOR * arr[0];
		mul /= arr[1];
		return (int) mul;
	}

	public static String asPrettyString(int drops) {
		return String.format("%3.3f", drops / DOUBLE_DENOMINATOR);
	}
}
