/*
 * Copyright (c) 2011 University of Tartu
 */
package org.jpmml.evaluator;

import java.util.*;

import org.dmg.pmml.*;

public class NormalizationUtil {

	static
	public double normalize(NormContinuous normContinuous, Number number) {

		// handle missing values
		if (number == null) {
			Double missing = normContinuous.getMapMissingTo();
			if (missing == null) {
				throw new EvaluationException();
			}

			return missing;
		}

		return normalize(normContinuous, number.doubleValue());
	}

	static
	public double normalize(NormContinuous normContinuous, double value){
		List<LinearNorm> linearNorms = normContinuous.getLinearNorms();

		LinearNorm rangeStart = linearNorms.get(0);
		LinearNorm rangeEnd = linearNorms.get(linearNorms.size() - 1);

		// select proper interval for normalization
		if (value >= rangeStart.getOrig() && value <= rangeEnd.getOrig()) {

			for (int i = 1; i < linearNorms.size() - 1; i++) {
				LinearNorm linearNorm = linearNorms.get(i);

				if (value >= linearNorm.getOrig()) {
					rangeStart = linearNorm;
				} else

				if (value <= linearNorm.getOrig()) {
					rangeEnd = linearNorm;

					break;
				}
			}
		} else

		// deal with outliers
		{
			OutlierTreatmentMethodType outlierTreatmentMethod = normContinuous.getOutliers();

			switch (outlierTreatmentMethod) {
				case AS_MISSING_VALUES:
					Double missing = normContinuous.getMapMissingTo();
					if (missing == null) {
						throw new EvaluationException();
					}
					return missing;
				case AS_IS:
					if (value < rangeStart.getOrig()) {
						rangeEnd = linearNorms.get(1);
					} else

					{
						rangeStart = linearNorms.get(linearNorms.size() - 2);
					}
					break;
				case AS_EXTREME_VALUES:
					return value < rangeStart.getOrig() ? rangeStart.getNorm() : rangeEnd.getNorm();
				default:
					break;
			}
		}

		double origRange = rangeEnd.getOrig() - rangeStart.getOrig();
		double normRange = rangeEnd.getNorm() - rangeStart.getNorm();

		return rangeStart.getNorm() + (value - rangeStart.getOrig()) / origRange * normRange;
	}

	static
	public double denormalize(NormContinuous normContinuous, double value) {
		List<LinearNorm> linearNorms = normContinuous.getLinearNorms();

		LinearNorm rangeStart = linearNorms.get(0);
		LinearNorm rangeEnd = linearNorms.get(linearNorms.size() - 1);

		for (int i = 1; i < linearNorms.size() - 1; i++) {
			LinearNorm linearNorm = linearNorms.get(i);

			if (value >= linearNorm.getNorm()) {
				rangeStart = linearNorm;
			} else

			if (value <= linearNorm.getNorm()) {
				rangeEnd = linearNorm;

				break;
			}
		}

		double origRange = rangeEnd.getOrig() - rangeStart.getOrig();
		double normRange = rangeEnd.getNorm() - rangeStart.getNorm();

		return (value - rangeStart.getNorm()) / normRange * origRange + rangeStart.getOrig();
	}

}
