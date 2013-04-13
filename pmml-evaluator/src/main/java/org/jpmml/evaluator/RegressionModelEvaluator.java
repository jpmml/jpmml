/*
 * Copyright (c) 2012 University of Tartu
 */
package org.jpmml.evaluator;

import java.util.*;

import org.jpmml.manager.*;

import org.dmg.pmml.*;

/**
 * This class evaluates the variables on the model. It reads the pmml object
 * to return a result.
 * For information about the regression model, see {@link RegressionModelManager}.
 * 
 * @author tbadie
 *
 */
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

	public Object prepare(FieldName name, Object value){
		return ParameterUtil.prepare(getDataField(name), getMiningField(name), value);
	}

	/**
	 * @see #evaluateRegression(EvaluationContext)
	 * @see #evaluateClassification(EvaluationContext)
	 */
	public Object evaluate(Map<FieldName, ?> parameters){
		RegressionModel regressionModel = getModel();

		Map<FieldName, ?> predictions;

		ModelManagerEvaluationContext context = new ModelManagerEvaluationContext(this, parameters);

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

		return OutputUtil.evaluate(predictions, context);
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

	public Map<FieldName, ClassificationMap> evaluateClassification(EvaluationContext context){
		RegressionModel regressionModel = getModel();

		List<RegressionTable> regressionTables = getRegressionTables();
		if(regressionTables.size() < 1){
			throw new EvaluationException();
		}

		double sumExp = 0d;

		ClassificationMap values = new ClassificationMap();

		for(RegressionTable regressionTable : regressionTables){
			Double value = evaluateRegressionTable(regressionTable, context);

			sumExp += Math.exp(value.doubleValue());

			values.put(regressionTable.getTargetCategory(), value);
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

		Collection<Map.Entry<String, Double>> entries = values.entrySet();
		for(Map.Entry<String, Double> entry : entries){
			entry.setValue(normalizeClassificationResult(regressionNormalizationMethod, entry.getValue(), sumExp));
		}

		return Collections.singletonMap(name, values);
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

			result += numericPredictor.getCoefficient() * Math.pow(((Number)value).doubleValue(), numericPredictor.getExponent());
		}

		List<CategoricalPredictor> categoricalPredictors = regressionTable.getCategoricalPredictors();
		for(CategoricalPredictor categoricalPredictor : categoricalPredictors){
			Object value = ExpressionUtil.evaluate(categoricalPredictor.getName(), context);

			// "if the input value is missing then the product is ignored"
			if(value == null){
				continue;
			}

			boolean equals = ParameterUtil.equals(value, categoricalPredictor.getValue());

			result += categoricalPredictor.getCoefficient() * (equals ? 1d : 0d);
		}

		List<PredictorTerm> predictorTerms = regressionTable.getPredictorTerms();
		for(PredictorTerm predictorTerm : predictorTerms){
			throw new UnsupportedFeatureException(predictorTerm);
		}
		String result = new String();
		if (targetCategoryToScore.isEmpty()) return null;
		
		TreeMap<Double, String> scoreToCategory = new TreeMap<Double, String>();
		switch (getNormalizationMethodType()) {
			case NONE:
				// pick the category with top score
				for (Map.Entry<String, Double> categoryScore : targetCategoryToScore.entrySet()) {
					scoreToCategory.put(categoryScore.getValue(), categoryScore.getKey());
				}
				result = scoreToCategory.lastEntry().getValue();
				break;
			case LOGIT:
				// pick the max of pj = 1 / ( 1 + exp( -yj ) )
				for (Map.Entry<String, Double> categoryScore : targetCategoryToScore.entrySet()) {
					double yj = categoryScore.getValue();
					double pj = 1.0/(1.0 + Math.exp(yj));

					scoreToCategory.put(pj, categoryScore.getKey());
				}
				result = scoreToCategory.lastEntry().getValue();
				break;
			case EXP:
				// pick the max of exp(yj) 
				for (Map.Entry<String, Double> categoryScore : targetCategoryToScore.entrySet()) {
					double yj = categoryScore.getValue();
					double pj = Math.exp(yj);
					scoreToCategory.put(pj, categoryScore.getKey());
				}
				result = scoreToCategory.lastEntry().getValue();
				break;
			case SOFTMAX:
				// pj = exp(yj) / (Sum[i = 1 to N](exp(yi) ) ) 
				double sum = 0.0;
				for (Map.Entry<String, Double> categoryScore : targetCategoryToScore.entrySet()) {
					double yj = categoryScore.getValue();
					sum += Math.exp(yj);
				}
				for (Map.Entry<String, Double> categoryScore : targetCategoryToScore.entrySet()) {
					double yj = categoryScore.getValue();
					double pj = Math.exp(yj) / sum;
					scoreToCategory.put(pj, categoryScore.getKey());
				}
				result = scoreToCategory.lastEntry().getValue();
				break;
			case CLOGLOG:
				// pick the max of pj = 1 - exp( -exp( yj ) ) 
				for (Map.Entry<String, Double> categoryScore : targetCategoryToScore.entrySet()) {
					double yj = categoryScore.getValue();
					double pj = 1 - Math.exp(-Math.exp(yj));
					scoreToCategory.put(pj, categoryScore.getKey());
				}
				result = scoreToCategory.lastEntry().getValue();
				break;
			case LOGLOG:
				// pick the max of pj = exp( -exp( -yj ) ) 
				for (Map.Entry<String, Double> categoryScore : targetCategoryToScore.entrySet()) {
					double yj = categoryScore.getValue();
					double pj = Math.exp(-Math.exp( -yj));
					scoreToCategory.put(pj, categoryScore.getKey());
				}
				result = scoreToCategory.lastEntry().getValue();
				break;
			default:
				
				result = null;					
		}

		return result;
	}

	private Double evaluateRegression(Map<FieldName, ?> parameters) {
		// When it's a simple regression, there is only one table. So we just
		// evaluate it, normalize the result and return it.   
		double result = evaluateRegressionTable(getOrCreateRegressionTable(), parameters);

		RegressionNormalizationMethodType normalizationMethod = getNormalizationMethodType();
		switch (normalizationMethod) {
			case NONE:
				// The same thing than: result = result;
				break;
			case SOFTMAX:
			case LOGIT:
				result = 1.0 / (1.0 + Math.exp(-result));
				break;
			case EXP:
				result = Math.exp(result);
				break;
			default:
				// We should never be here.
				assert false;
				break;
		}
				
		return result;
	}

	/**
	 * Evaluate a regression table.
	 * 
	 * @param rt The regression table.
	 * @param parameters The set of parameters.
	 * @return The evaluation.
	 */
	private double evaluateRegressionTable(RegressionTable rt, Map<FieldName, ?> parameters) {
		// Evaluating a regression table is only evaluate all the numeric predictors,
		// and all the categorical predictors.
		double result = 0D;

		result += getIntercept(rt);
		
		// If a value is missing for a numeric predictors, it's an error. 
		List<NumericPredictor> numericPredictors = rt.getNumericPredictors();
		for(NumericPredictor numericPredictor : numericPredictors) {
			result += evaluateNumericPredictor(numericPredictor, parameters);
		}

		List<CategoricalPredictor> categoricalPredictors = rt.getCategoricalPredictors();
		for (CategoricalPredictor categoricalPredictor : categoricalPredictors) {
			result += evaluateCategoricalPredictor(categoricalPredictor, parameters);
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
	
	/**
	 * Evaluate a categorical predictor on a set of parameters.
	 * 
	 * @param categoricalPredictor The predictor.
	 * @param parameters The parameters.
	 * @return The result of the evaluation.
	 */
	private double evaluateCategoricalPredictor(CategoricalPredictor categoricalPredictor, Map<FieldName, ?> parameters) {
		// The concept of the categorical predictor is: if a variable has a
		// certain value, we return the coefficient. Otherwise we return 0.
		// The problem is, the value can be a string, a double, an integer...
		// And the equality is not done the same way. So we have to look the
		// type of the variable used to know how to compare them. Because 0.0 != "0".
		// This is why there is this ugly switch below. It's unfortunate, but it's the
		// only way that works I have found.
		Object blobValue =  ParameterUtil.getValue(parameters, categoricalPredictor.getName());
		boolean isEqual = false;
		List<DataField> ldf = getDataDictionary().getDataFields();

		for (DataField df : ldf) {
			if (df.getName().getValue().equals(categoricalPredictor.getName().getValue())) {
				switch (df.getDataType()) {
				case INTEGER:
					isEqual = (Integer) blobValue == Integer.parseInt(categoricalPredictor.getValue());
					break;
				case DOUBLE:
					isEqual = (Double) blobValue == Double.parseDouble(categoricalPredictor.getValue());
					break;
				case BOOLEAN:
					isEqual = (Boolean) blobValue == Boolean.parseBoolean(categoricalPredictor.getValue());
					break;
				case FLOAT:
					isEqual = (Float) blobValue == Float.parseFloat(categoricalPredictor.getValue());
					break;
				case STRING:
					isEqual = categoricalPredictor.getValue().equals((String)blobValue);
					break;
				default:
					throw new UnsupportedOperationException();
				}
			}
		}

		return categoricalPredictor.getCoefficient() * (isEqual ? 1 : 0);
	}
}