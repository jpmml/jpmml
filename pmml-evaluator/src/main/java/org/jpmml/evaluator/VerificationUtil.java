/*
 * Copyright (c) 2013 University of Tartu
 */
package org.jpmml.evaluator;

public class VerificationUtil {

	private VerificationUtil(){
	}

	static
	public boolean acceptable(Object expected, Object actual, double precision, double zeroThreshold){

		if(expected == null){
			return (actual == null);
		} else

		{
			if(expected instanceof Number && actual instanceof Number){
				return acceptable((Number)expected, (Number)actual, precision, zeroThreshold);
			}

			return (expected).equals(actual);
		}
	}

	/**
	 * A convenience method for unit testing purposes only
	 */
	static
	boolean acceptable(Number expected, Number actual){
		return acceptable(expected, actual, 0.0000001, 0.0000001);
	}

	/**
	 * @param precision The acceptable range given <em>in proportion</em> of the expected value, including its boundaries.
	 * @param zeroThreshold The threshold for distinguishing between zero and non-zero values.
	 */
	static
	public boolean acceptable(Number expected, Number actual, double precision, double zeroThreshold){

		if(isZero(expected, zeroThreshold) && isZero(actual, zeroThreshold)){
			return true;
		}

		double zeroBoundary = expected.doubleValue() * (1d - precision); // Pointed towards zero
		double infinityBoundary = expected.doubleValue() * (1d + precision); // Pointed towards positive or negative infinity

		// positive values
		if(expected.doubleValue() >= 0){
			return (actual.doubleValue() >= zeroBoundary) && (actual.doubleValue() <= infinityBoundary);
		} else

		// negative values
		{
			return (actual.doubleValue() <= zeroBoundary) && (actual.doubleValue() >= infinityBoundary);
		}
	}

	static
	public boolean isZero(Number value, double zeroThreshold){
		return (value.doubleValue() >= -zeroThreshold) && (value.doubleValue() <= zeroThreshold);
	}
}