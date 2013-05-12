/*
 * Copyright (c) 2012 University of Tartu
 */
package org.jpmml.evaluator;

import java.util.*;

import org.jpmml.manager.*;

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
	 * @see #evaluateRegression(EvaluationContext)
	 */
	public Double evaluate(Map<FieldName, ?> parameters){
		RegressionModel regressionModel = getModel();

		EvaluationContext context = new ModelManagerEvaluationContext(this, parameters);

		MiningFunctionType miningFunction = regressionModel.getFunctionName();
		switch(miningFunction){
			case REGRESSION:
				return evaluateRegression(context);
			default:
				throw new UnsupportedFeatureException(miningFunction);
		}
	}

	public Double evaluateRegression(EvaluationContext context){
		double result = 0D;

		result += getIntercept();

		List<NumericPredictor> numericPredictors = getNumericPrecictors();
		for(NumericPredictor numericPredictor : numericPredictors){
			result += evaluateNumericPredictor(numericPredictor, context);
		}

		return Double.valueOf(result);
	}

	private double evaluateNumericPredictor(NumericPredictor numericPredictor, EvaluationContext context){
		Number value = (Number)ExpressionUtil.evaluate(numericPredictor.getName(), context);
		if(value == null){
			throw new MissingParameterException(numericPredictor.getName());
		}

		return numericPredictor.getCoefficient() * value.doubleValue();
	}
}