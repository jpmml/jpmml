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
		RegressionModel regressionModel = getModel();

		List<RegressionTable> regressionTables = getRegressionTables();
		if(regressionTables.size() != 1){
			throw new EvaluationException();
		}

		RegressionTable regressionTable = regressionTables.get(0);

		Double value = evaluateRegressionTable(regressionTable, context);

		RegressionNormalizationMethodType regressionNormalizationMethod = regressionModel.getNormalizationMethod();
		switch(regressionNormalizationMethod){
			case NONE:
				break;
			case SOFTMAX:
			case LOGIT:
				value = (1d / (1d + Math.exp(-value)));
				break;
			case EXP:
				value = Math.exp(value);
				break;
			default:
				throw new UnsupportedFeatureException(regressionNormalizationMethod);
		}

		return Collections.singletonMap(getTarget(), value);
	}

	private Double evaluateRegressionTable(RegressionTable regressionTable, EvaluationContext context){
		double result = 0D;

		result += regressionTable.getIntercept();

		List<NumericPredictor> numericPredictors = regressionTable.getNumericPredictors();
		for(NumericPredictor numericPredictor : numericPredictors){
			Object value = ExpressionUtil.evaluate(numericPredictor.getName(), context);

			// "if the input value is missing then the result evaluates to a missing value"
			if(value == null){
				return null;
			}

			result +=  numericPredictor.getCoefficient() * Math.pow(((Number)value).doubleValue(), (numericPredictor.getExponent()).doubleValue());
		}

		List<CategoricalPredictor> categoricalPredictors = regressionTable.getCategoricalPredictors();
		for(CategoricalPredictor categoricalPredictor : categoricalPredictors){
			Object value = ExpressionUtil.evaluate(categoricalPredictor.getName(), context);

			// "if the input value is missing then the product is ignored"
			if(value == null){
				continue;
			}

			DataType dataType = ParameterUtil.getDataType(value);

			boolean equals = (ParameterUtil.parse(dataType, categoricalPredictor.getValue())).equals(value);

			result += categoricalPredictor.getCoefficient() * (equals ? 1d : 0d);
		}

		List<PredictorTerm> predictorTerms = regressionTable.getPredictorTerms();
		for(PredictorTerm predictorTerm : predictorTerms){
			throw new UnsupportedFeatureException(predictorTerm);
		}

		return Double.valueOf(result);
	}
}