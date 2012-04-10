/*
 * Copyright (c) 2012 University of Tartu
 */
package org.jpmml.manager;

import java.util.*;

import org.dmg.pmml.*;

public class RegressionModelEvaluator extends RegressionModelManager implements Evaluator {

	public RegressionModelEvaluator(PMML pmml){
		super(pmml);
	}

	public RegressionModelEvaluator(PMML pmml, RegressionModel regressionModel){
		super(pmml, regressionModel);
	}

	public RegressionModelEvaluator(RegressionModelManager parent){
		this(parent.getPmml(), parent.getModel());
	}

	/**
	 * @see #evaluateRegression(Map)
	 */
	public Double evaluate(Map<FieldName, ?> parameters){
		RegressionModel regressionModel = getModel();

		MiningFunctionType miningFunction = regressionModel.getFunctionName();
		switch(miningFunction){
			case REGRESSION:
				return evaluateRegression(parameters);
			default:
				throw new UnsupportedFeatureException(miningFunction);
		}
	}

	public Double evaluateRegression(Map<FieldName, ?> parameters){
		double result = 0D;

		result += getIntercept();

		List<NumericPredictor> numericPredictors = getNumericPrecictors();
		for(NumericPredictor numericPredictor : numericPredictors){
			result += evaluateNumericPredictor(numericPredictor, parameters);
		}

		return Double.valueOf(result);
	}

	private double evaluateNumericPredictor(NumericPredictor numericPredictor, Map<FieldName, ?> parameters){
		Number value = (Number)ParameterUtil.getValue(parameters, numericPredictor.getName());

		return numericPredictor.getCoefficient() * value.doubleValue();
	}
}