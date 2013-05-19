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
		Map<FieldName, Object> values = new LinkedHashMap<FieldName, Object>();
		values.putAll(parameters);
		values.putAll(EvaluatorUtil.decodeValues(predictions));

		EvaluationContext context = new ModelManagerEvaluationContext(modelManager, values);

		Output output = modelManager.getOrCreateOutput();

		List<OutputField> outputFields = output.getOutputFields();
		for(OutputField outputField : outputFields){
			ResultFeatureType resultFeature = outputField.getFeature();

			Object value;

			switch(resultFeature){
				case PREDICTED_VALUE:
					{
						FieldName target = outputField.getTargetField();
						if(target == null){
							target = modelManager.getTarget();
						} // End if

						if(!predictions.containsKey(target)){
							throw new EvaluationException();
						}

						value = values.get(target);
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
				default:
					throw new UnsupportedFeatureException(resultFeature);
			}

			FieldName name = outputField.getName();

			result.put(name, value);

			values.put(name, EvaluatorUtil.decode(value));
		}

		return result;
	}
}