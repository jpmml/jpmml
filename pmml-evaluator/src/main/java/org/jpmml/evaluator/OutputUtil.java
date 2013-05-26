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
	 * @param parameters Map of {@link Evaluator#getActiveFields() active field} values.
	 * @param predictions Map of {@link Evaluator#getPredictedFields() predicted field} values.
	 *
	 * @return Map of {@link Evaluator#getPredictedFields() predicted field} values together with {@link Evaluator#getOutputFields() output field} values.
	 */
	static
	public <M extends Model> Map<FieldName, Object> evaluate(ModelManager<M> modelManager, Map<FieldName, ?> parameters, Map<FieldName, ?> predictions){
		Map<FieldName, Object> result = new LinkedHashMap<FieldName, Object>();

		if(!Collections.disjoint(parameters.keySet(), predictions.keySet())){
			throw new EvaluationException();
		}

		result.putAll(predictions);

		// Global scope contains all active, predicted and (soon to be added-) output fields. Here, all fields values must be simple values
		Map<FieldName, Object> globalParameters = new LinkedHashMap<FieldName, Object>();
		globalParameters.putAll(parameters);
		globalParameters.putAll(EvaluatorUtil.decodeValues(predictions));

		EvaluationContext context = new ModelManagerEvaluationContext(modelManager, globalParameters);

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

						// Global scope contains simple value, whereas prediction results may contain both complex and simple values
						value = globalParameters.get(target);
					}
					break;
				case TRANSFORMED_VALUE:
					{
						Expression expression = outputField.getExpression();
						if(expression == null){
							throw new EvaluationException();
						}

						value = ExpressionUtil.evaluate(expression, context);

						// Exppression must produce simple value
						if(value instanceof Computable){
							throw new EvaluationException();
						}
					}
					break;
				case PROBABILITY:
					{
						FieldName target = getTarget(modelManager, outputField);

						if(!predictions.containsKey(target)){
							throw new EvaluationException();
						}

						value = getProbability(result.get(target), outputField.getValue());
					}
					break;
				default:
					throw new UnsupportedFeatureException(resultFeature);
			}

			DataType dataType = outputField.getDataType();
			if(dataType != null){
				value = ParameterUtil.cast(dataType, value);
			}

			FieldName name = outputField.getName();

			result.put(name, value);

			// The result of one output field becomes available to other output fields
			globalParameters.put(name, value);
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