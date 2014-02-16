/*
 * Copyright (c) 2011 University of Tartu
 */
package org.jpmml.evaluator;

import java.util.*;

import org.jpmml.manager.*;

import org.dmg.pmml.*;

public class NormalizationUtil {

	static
	public FieldValue normalize(NormContinuous normContinuous, FieldValue value){
		double result = normalize(normContinuous, (value.asNumber()).doubleValue());

		return FieldValueUtil.create(result);
	}

	static
	public double normalize(NormContinuous normContinuous, double value){
		List<LinearNorm> linearNorms = normContinuous.getLinearNorms();
		if(linearNorms.size() < 2){
			throw new InvalidFeatureException(normContinuous);
		}

		LinearNorm rangeStart = linearNorms.get(0);
		LinearNorm rangeEnd = linearNorms.get(linearNorms.size() - 1);

		// Select proper interval for normalization
		if(value >= rangeStart.getOrig() && value <= rangeEnd.getOrig()){

			for(int i = 1; i < linearNorms.size() - 1; i++){
				LinearNorm linearNorm = linearNorms.get(i);

				if(value >= linearNorm.getOrig()){
					rangeStart = linearNorm;
				} else

				if(value <= linearNorm.getOrig()){
					rangeEnd = linearNorm;

					break;
				}
			}
		} else

		// Deal with outliers
		{
			OutlierTreatmentMethodType outlierTreatmentMethod = normContinuous.getOutliers();

			switch(outlierTreatmentMethod){
				case AS_MISSING_VALUES:
					Double missing = normContinuous.getMapMissingTo();
					if(missing == null){
						throw new InvalidFeatureException(normContinuous);
					}
					return missing;
				case AS_IS:
					if(value < rangeStart.getOrig()){
						rangeEnd = linearNorms.get(1);
					} else

					{
						rangeStart = linearNorms.get(linearNorms.size() - 2);
					}
					break;
				case AS_EXTREME_VALUES:
					if(value < rangeStart.getOrig()){
						return rangeStart.getNorm();
					} else

					{
						return rangeEnd.getNorm();
					}
				default:
					throw new UnsupportedFeatureException(normContinuous, outlierTreatmentMethod);
			}
		}

		double origRange = rangeEnd.getOrig() - rangeStart.getOrig();
		double normRange = rangeEnd.getNorm() - rangeStart.getNorm();

		return rangeStart.getNorm() + (value - rangeStart.getOrig()) / origRange * normRange;
	}

	static
	public double denormalize(NormContinuous normContinuous, double value){
		List<LinearNorm> linearNorms = normContinuous.getLinearNorms();
		if(linearNorms.size() < 2){
			throw new InvalidFeatureException(normContinuous);
		}

		LinearNorm rangeStart = linearNorms.get(0);
		LinearNorm rangeEnd = linearNorms.get(linearNorms.size() - 1);

		for(int i = 1; i < linearNorms.size() - 1; i++){
			LinearNorm linearNorm = linearNorms.get(i);

			if(value >= linearNorm.getNorm()){
				rangeStart = linearNorm;
			} else

			if(value <= linearNorm.getNorm()){
				rangeEnd = linearNorm;

				break;
			}
		}

		double origRange = rangeEnd.getOrig() - rangeStart.getOrig();
		double normRange = rangeEnd.getNorm() - rangeStart.getNorm();

		return (value - rangeStart.getNorm()) / normRange * origRange + rangeStart.getOrig();
	}

}
