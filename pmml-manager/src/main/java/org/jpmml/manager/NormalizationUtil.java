/*
 * Copyright (c) 2011 University of Tartu
 */
package org.jpmml.manager;

import org.dmg.pmml.LinearNorm;
import org.dmg.pmml.NormContinuous;
import org.jpmml.manager.EvaluationException;

public class NormalizationUtil {
	
	static
	public double normalize(NormContinuous norm, Double value) {
		String fieldName = norm.getField().getValue();
		
		// handle missing values
		if (value == null) {
			Double missing = norm.getMapMissingTo();
			if (missing == null) {
				throw new EvaluationException("Can't map missing value for "+fieldName);
			} else {
				return missing;
			}
		}
		
		LinearNorm rangeStart = norm.getLinearNorms().get(0);
		LinearNorm rangeEnd = norm.getLinearNorms().get(norm.getLinearNorms().size()-1);
		
		// select proper interval for normalization
		if (rangeStart.getOrig() <= value && value <= rangeEnd.getOrig()) {
			for (int i=1; i<norm.getLinearNorms().size()-1; ++i) {
				LinearNorm linearNorm = norm.getLinearNorms().get(i);
				if (linearNorm.getOrig() <= value) {
					rangeStart = linearNorm;
				} else if (value <= linearNorm.getOrig()) {
					rangeEnd = linearNorm;
					break;
				}
			}
		} else { // deal with outliers
			switch (norm.getOutliers()) {
			case AS_MISSING_VALUES:
				if (norm.getMapMissingTo() == null) {
					throw new EvaluationException("Can't map outlier to missing value:"+fieldName);
				} else {
					return norm.getMapMissingTo();
				}
			case AS_EXTREME_VALUES:
				return value < rangeStart.getOrig() ? rangeStart.getNorm() : rangeEnd.getNorm();
			case AS_IS:
				if (value < rangeStart.getOrig()) {
					rangeEnd = norm.getLinearNorms().get(1);
				} else {
					rangeStart = norm.getLinearNorms().get(norm.getLinearNorms().size()-2);
				}
				break;
			}
		}
		
		double origRange = rangeEnd.getOrig() - rangeStart.getOrig();
		double normRange = rangeEnd.getNorm() - rangeStart.getNorm();
		value = rangeStart.getNorm() + (value-rangeStart.getOrig())/origRange * normRange;
		
		return value;
	}

	static
	public double denormalize(double value, NormContinuous norm) {
		LinearNorm rangeStart = norm.getLinearNorms().get(0);
		LinearNorm rangeEnd = norm.getLinearNorms().get(norm.getLinearNorms().size()-1);
		for (int i=1; i<norm.getLinearNorms().size()-1; ++i) {
			LinearNorm linearNorm = norm.getLinearNorms().get(i);
			if (linearNorm.getNorm() <= value) {
				rangeStart = linearNorm;
			} else if (value <= rangeEnd.getNorm()) {
				rangeEnd = linearNorm;
				break;
			}
		}

		double origRange = rangeEnd.getOrig() - rangeStart.getOrig();
		double normRange = rangeEnd.getNorm() - rangeStart.getNorm();
		return (value-rangeStart.getNorm())/normRange * origRange + rangeStart.getOrig();
	}

}
