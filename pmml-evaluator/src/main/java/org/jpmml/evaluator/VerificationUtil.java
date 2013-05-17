/*
 * Copyright (c) 2013 University of Tartu
 */
package org.jpmml.evaluator;

public class VerificationUtil {

	private VerificationUtil(){
	}

	static
	public boolean acceptable(Object expected, Object actual, double precision){

		if(expected == null){
			return (actual == null);
		} else

		{
			if(expected instanceof Number && actual instanceof Number){
				return acceptable((Number)expected, (Number)actual, precision);
			}

			return (expected).equals(actual);
		}
	}

	/**
	 * @param precision The acceptable range given <em>in proportion</em> of the expected value, including its boundaries.
	 */
	static
	public boolean acceptable(Number expected, Number actual, double precision){
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
}