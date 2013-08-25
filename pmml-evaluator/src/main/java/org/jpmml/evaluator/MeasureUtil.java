/*
 * Copyright (c) 2013 University of Tartu
 */
package org.jpmml.evaluator;

import java.util.*;

import org.jpmml.manager.*;

import org.dmg.pmml.*;

import com.google.common.collect.*;

public class MeasureUtil {

	private MeasureUtil(){
	}

	static
	public boolean isDistance(Measure measure){
		return (measure instanceof Euclidean || measure instanceof SquaredEuclidean || measure instanceof Chebychev || measure instanceof CityBlock || measure instanceof Minkowski);
	}

	static
	public Double evaluateDistance(ComparisonMeasure comparisonMeasure, List<ClusteringField> clusteringFields, List<FieldValue> values, List<? extends Number> centerValues, List<Double> fieldWeights, Double adjustment){
		double innerPower;
		double outerPower;

		Measure measure = comparisonMeasure.getMeasure();
		if(measure instanceof Euclidean){
			innerPower = outerPower = 2;
		} else

		if(measure instanceof SquaredEuclidean){
			innerPower = 2;
			outerPower = 1;
		} else

		if(measure instanceof Chebychev || measure instanceof CityBlock){
			innerPower = outerPower = 1;
		} else

		if(measure instanceof Minkowski){
			Minkowski minkowski = (Minkowski)measure;

			double p = minkowski.getPParameter();
			if(p < 0){
				throw new InvalidFeatureException(minkowski);
			}

			innerPower = outerPower = p;
		} else

		{
			throw new UnsupportedFeatureException(measure);
		}

		List<Double> distances = Lists.newArrayList();

		clusteringFields:
		for(int i = 0; i < clusteringFields.size(); i++){
			FieldValue value = values.get(i);
			if(value == null){
				continue clusteringFields;
			}

			Double distance = evaluateInnerFunction(comparisonMeasure, clusteringFields.get(i), value.asNumber(), centerValues.get(i), fieldWeights.get(i), innerPower);

			distances.add(distance);
		}

		if(measure instanceof Euclidean || measure instanceof SquaredEuclidean || measure instanceof CityBlock || measure instanceof Minkowski){
			double sum = 0;

			for(Double distance : distances){
				sum += distance.doubleValue();
			}

			return Math.pow(sum * adjustment.doubleValue(), 1d / outerPower);
		} else

		if(measure instanceof Chebychev){
			Double max = Collections.max(distances);

			return max.doubleValue() * adjustment.doubleValue();
		} else

		{
			throw new UnsupportedFeatureException(measure);
		}
	}

	static
	private double evaluateInnerFunction(ComparisonMeasure comparisonMeasure, ClusteringField clusteringField, Number x, Number y, Double weight, Double power){
		CompareFunctionType compareFunction = clusteringField.getCompareFunction();

		if(compareFunction == null){
			compareFunction = comparisonMeasure.getCompareFunction();

			// The ComparisonMeasure element is limited to "attribute-less" comparison functions
			switch(compareFunction){
				case ABS_DIFF:
				case DELTA:
				case EQUAL:
					break;
				case GAUSS_SIM:
				case TABLE:
					throw new InvalidFeatureException(comparisonMeasure);
				default:
					throw new UnsupportedFeatureException(comparisonMeasure, compareFunction);
			}
		}

		double distance;

		switch(compareFunction){
			case ABS_DIFF:
				{
					distance = Math.abs(x.doubleValue() - y.doubleValue());
				}
				break;
			case GAUSS_SIM:
				{
					Double similarityScale = clusteringField.getSimilarityScale();
					if(similarityScale == null){
						throw new InvalidFeatureException(clusteringField);
					}

					double z = (x.doubleValue() - y.doubleValue());
					double s = similarityScale.doubleValue();

					distance = Math.exp(-Math.log(2d) * Math.pow(z, 2d) / Math.pow(s, 2d));
				}
				break;
			case DELTA:
			case EQUAL:
				{
					DataType dataType = TypeUtil.getResultDataType(TypeUtil.getDataType(x), TypeUtil.getDataType(y));

					boolean equals = TypeUtil.equals(dataType, x, y);

					if((CompareFunctionType.DELTA).equals(compareFunction)){
						distance = (equals ? 0 : 1);
					} else

					{
						distance = (equals ? 1 : 0);
					} // End if
				}
				break;
			case TABLE:
				throw new UnsupportedFeatureException(clusteringField, compareFunction);
			default:
				throw new UnsupportedFeatureException(clusteringField, compareFunction);
		}

		return weight.doubleValue() * Math.pow(distance, power.doubleValue());
	}

	static
	public boolean isSimilarity(Measure measure){
		return (measure instanceof SimpleMatching || measure instanceof Jaccard || measure instanceof Tanimoto || measure instanceof BinarySimilarity);
	}
}