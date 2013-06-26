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
			Object value = null;

			ResultFeatureType resultFeature = outputField.getFeature();
			switch(resultFeature){
				case PREDICTED_VALUE:
				case PROBABILITY:
				case ENTITY_ID:
				case REASON_CODE:
					{
						FieldName target = outputField.getTargetField();
						if(target == null){
							target = modelManager.getTarget();
						} // End if

						if(!predictions.containsKey(target)){
							throw new MissingFieldException(target, outputField);
						}

						// Prediction results could be either simple or complex values
						value = predictions.get(target);
					}
					break;
				default:
					break;
			} // End switch

			switch(resultFeature){
				case PREDICTED_VALUE:
					{
						value = getResult(value);
					}
					break;
				case TRANSFORMED_VALUE:
					{
						Expression expression = outputField.getExpression();
						if(expression == null){
							throw new InvalidFeatureException(outputField);
						}

						value = ExpressionUtil.evaluate(expression, context);
					}
					break;
				case PROBABILITY:
					{
						value = getProbability(value, outputField.getValue());
					}
					break;
				case ENTITY_ID:
					{
						value = getEntityId(value);
					}
					break;
				case REASON_CODE:
					{
						value = getReasonCode(value, outputField.getRank());
					}
					break;
				default:
					throw new UnsupportedFeatureException(outputField, resultFeature);
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
	private Object getResult(Object object){
		return EvaluatorUtil.decode(object);
	}

	static
	private Double getProbability(Object object, String value){

		if(!(object instanceof HasProbability)){
			throw new EvaluationException();
		}

		HasProbability hasProbability = (HasProbability)object;

		return hasProbability.getProbability(value);
	}

	static
	private String getEntityId(Object object){

		if(!(object instanceof HasEntityId)){
			throw new EvaluationException();
		}

		HasEntityId hasEntityId = (HasEntityId)object;

		return hasEntityId.getEntityId();
	}

	static
	public String getReasonCode(Object object, int rank){

		if(!(object instanceof HasReasonCode)){
			throw new EvaluationException();
		}

		HasReasonCode hasReasonCode = (HasReasonCode)object;

		return hasReasonCode.getReasonCode(rank);
	}
}