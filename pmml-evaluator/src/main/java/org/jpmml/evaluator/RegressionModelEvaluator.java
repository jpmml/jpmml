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
			case CLASSIFICATION:
				predictions = evaluateClassification(context);
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

		FieldName name = getTarget();

		RegressionNormalizationMethodType regressionNormalizationMethod = regressionModel.getNormalizationMethod();

		value = normalizeRegressionResult(regressionNormalizationMethod, value);

		return Collections.singletonMap(name, value);
	}

	public Map<FieldName, String> evaluateClassification(EvaluationContext context){
		RegressionModel regressionModel = getModel();

		List<RegressionTable> regressionTables = getRegressionTables();
		if(regressionTables.size() < 1){
			throw new EvaluationException();
		}

		double sumExp = 0d;

		Map<RegressionTable, Double> values = new LinkedHashMap<RegressionTable, Double>();

		for(RegressionTable regressionTable : regressionTables){
			Double value = evaluateRegressionTable(regressionTable, context);

			sumExp += Math.exp(value.doubleValue());

			values.put(regressionTable, value);
		}

		FieldName name = getTarget();

		DataField dataField = getDataField(name);

		OpType opType = dataField.getOptype();
		switch(opType){
			case CATEGORICAL:
				break;
			default:
				throw new UnsupportedFeatureException(opType);
		}

		RegressionNormalizationMethodType regressionNormalizationMethod = regressionModel.getNormalizationMethod();

		values = normalizeClassificationResult(regressionNormalizationMethod, values, sumExp);

		Map.Entry<RegressionTable, Double> maxEntry = null;

		Collection<Map.Entry<RegressionTable, Double>> entries = values.entrySet();
		for(Map.Entry<RegressionTable, Double> entry : entries){

			if(maxEntry == null || (maxEntry.getValue()).compareTo(entry.getValue()) < 0){
				maxEntry = entry;
			}
		}

		RegressionTable regressionTable = maxEntry.getKey();

		return Collections.singletonMap(name, regressionTable.getTargetCategory());
	}

	static
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

	static
	private Double normalizeRegressionResult(RegressionNormalizationMethodType regressionNormalizationMethod, Double value){

		switch(regressionNormalizationMethod){
			case NONE:
				return value;
			case SOFTMAX:
			case LOGIT:
				return 1d / (1d + Math.exp(-value));
			case EXP:
				return Math.exp(value);
			default:
				throw new UnsupportedFeatureException(regressionNormalizationMethod);
		}
	}

	static
	private <K> Map<K, Double> normalizeClassificationResult(RegressionNormalizationMethodType regressionNormalizationMethod, Map<K, Double> values, Double sumExp){
		Map<K, Double> result = new LinkedHashMap<K, Double>();

		Collection<Map.Entry<K, Double>> entries = values.entrySet();
		for(Map.Entry<K, Double> entry : entries){
			result.put(entry.getKey(), normalizeClassificationResult(regressionNormalizationMethod, entry.getValue(), sumExp));
		}

		return result;
	}

	static
	private Double normalizeClassificationResult(RegressionNormalizationMethodType regressionNormalizationMethod, Double value, Double sumExp){

		switch(regressionNormalizationMethod){
			case NONE:
				return value;
			case SOFTMAX:
				return Math.exp(value) / sumExp;
			case LOGIT:
				return 1d / (1d + Math.exp(-value));
			case CLOGLOG:
				return 1d - Math.exp(-Math.exp(value));
			case LOGLOG:
				return Math.exp(-Math.exp(-value));
			default:
				throw new UnsupportedFeatureException(regressionNormalizationMethod);
		}
	}
}