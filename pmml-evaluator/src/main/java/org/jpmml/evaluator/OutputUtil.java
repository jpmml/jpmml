/*
 * Copyright (c) 2013 University of Tartu
 */
package org.jpmml.evaluator;

import java.util.*;

import org.jpmml.manager.*;

import org.dmg.pmml.*;

public class OutputUtil {

	private OutputUtil(){
	}

	/**
	 * Evaluates the {@link Output} element.
	 *
	 * @param predictions Map of {@link Evaluator#getPredictedFields() predicted field} values.
	 *
	 * @return Map of {@link Evaluator#getPredictedFields() predicted field} values together with {@link Evaluator#getOutputFields() output field} values.
	 */
	static
	public Map<FieldName, Object> evaluate(Map<FieldName, ?> predictions, ModelManagerEvaluationContext context){
		Map<FieldName, Object> result = new LinkedHashMap<FieldName, Object>(predictions);

		// Create a modifiable context instance
		context = context.clone();

		ModelManager<?> modelManager = context.getModelManager();

		Output output = modelManager.getOrCreateOutput();

		List<OutputField> outputFields = output.getOutputFields();
		for(OutputField outputField : outputFields){
			ResultFeatureType resultFeature = outputField.getFeature();

			Object value;

			switch(resultFeature){
				case PREDICTED_VALUE:
					{
						FieldName target = getTarget(modelManager, outputField);

						if(!predictions.containsKey(target)){
							throw new EvaluationException();
						}

						// Prediction results may be either simple or complex values
						value = EvaluatorUtil.decode(predictions.get(target));
					}
					break;
				case TRANSFORMED_VALUE:
					{
						Expression expression = outputField.getExpression();
						if(expression == null){
							throw new EvaluationException();
						}

						value = ExpressionUtil.evaluate(expression, context);
					}
					break;
				case PROBABILITY:
					{
						FieldName target = getTarget(modelManager, outputField);

						if(!predictions.containsKey(target)){
							throw new EvaluationException();
						}

						value = getProbability(predictions.get(target), outputField.getValue());
					}
					break;
				default:
					throw new UnsupportedFeatureException(resultFeature);
			}

			FieldName name = outputField.getName();

			DataType dataType = outputField.getDataType();
			if(dataType != null){
				value = ParameterUtil.cast(dataType, value);
			}

			result.put(name, value);

			// The result of one output field becomes available to other output fields
			context.putParameter(name, value);
		}

		return result;
	}

	static
	private FieldName getTarget(ModelManager<?> modelManager, OutputField outputField){
		FieldName result = outputField.getTargetField();
		if(result == null){
			result = modelManager.getTarget();
		}

		return result;
	}

	static
	private Double getProbability(Object result, String value){

		if(!(result instanceof Classification)){
			throw new EvaluationException();
		}

		Classification classification = (Classification)result;

		return classification.getProbability(value);
	}
}