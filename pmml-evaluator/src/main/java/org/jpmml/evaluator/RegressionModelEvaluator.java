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
	public Map<FieldName, ?> evaluate(Map<FieldName, ?> parameters){
		RegressionModel regressionModel = getModel();

		Map<FieldName, ?> predictions;

		EvaluationContext context = new ModelManagerEvaluationContext(this, parameters);

		MiningFunctionType miningFunction = regressionModel.getFunctionName();
		switch(miningFunction){
			case REGRESSION:
				predictions = evaluateRegression(context);
				break;
			default:
				throw new UnsupportedFeatureException(miningFunction);
		}

		return OutputUtil.evaluate(this, parameters, predictions);
	}

	public Map<FieldName, Double> evaluateRegression(EvaluationContext context){
		double result = 0D;

		result += getIntercept();

		List<NumericPredictor> numericPredictors = getNumericPrecictors();
		for(NumericPredictor numericPredictor : numericPredictors){
			result += evaluateNumericPredictor(numericPredictor, context);
		}

		return Collections.singletonMap(getTarget(), result);
	}

	private double evaluateNumericPredictor(NumericPredictor numericPredictor, EvaluationContext context){
		Number value = (Number)ExpressionUtil.evaluate(numericPredictor.getName(), context);
		if(value == null){
			throw new MissingParameterException(numericPredictor.getName());
		}

		return numericPredictor.getCoefficient() * value.doubleValue();
	}
}