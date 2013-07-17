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
		ModelManager<?> modelManager = context.getModelManager();

		Map<FieldName, Object> frame = new LinkedHashMap<FieldName, Object>();

		context.pushFrame(frame);

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
				case RULE_VALUE:
					{
						FieldName targetField = outputField.getTargetField();
						if(targetField == null){
							targetField = modelManager.getTargetField();
						} // End if

						if(!predictions.containsKey(targetField)){
							throw new MissingFieldException(targetField, outputField);
						}

						// Prediction results could be either simple or complex values
						value = predictions.get(targetField);
					}
					break;
				default:
					break;
			} // End switch

			switch(resultFeature){
				case PREDICTED_VALUE:
					{
						value = EvaluatorUtil.decode(value);
					}
					break;
				case TRANSFORMED_VALUE:
				case DECISION:
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
						value = getProbability(value, outputField);
					}
					break;
				case ENTITY_ID:
					{
						value = getEntityId(value);
					}
					break;
				case REASON_CODE:
					{
						value = getReasonCode(value, outputField);
					}
					break;
				case RULE_VALUE:
					{
						value = getRuleValue(value, outputField);
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

			// The result of one output field becomes available to other output fields
			frame.put(name, value);
		}

		context.popFrame();

		Map<FieldName, Object> result = new LinkedHashMap<FieldName, Object>(predictions);
		result.putAll(frame);

		return result;
	}

	static
	private Double getProbability(Object object, final OutputField outputField){

		if(!(object instanceof HasProbability)){
			throw new EvaluationException();
		}

		HasProbability hasProbability = (HasProbability)object;

		return hasProbability.getProbability(outputField.getValue());
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
	public String getReasonCode(Object object, final OutputField outputField){

		if(!(object instanceof HasReasonCode)){
			throw new EvaluationException();
		}

		HasReasonCode hasReasonCode = (HasReasonCode)object;

		int rank = outputField.getRank();
		if(rank <= 0){
			throw new InvalidFeatureException(outputField);
		}

		return hasReasonCode.getReasonCode(rank);
	}

	static
	public Object getRuleValue(Object object, final OutputField outputField){

		if(!(object instanceof HasAssociationRules)){
			throw new EvaluationException();
		}

		HasAssociationRules hasAssociationRules = (HasAssociationRules)object;

		List<AssociationRule> associationRules = hasAssociationRules.getAssociationRules(outputField.getAlgorithm());

		Comparator<AssociationRule> comparator = new Comparator<AssociationRule>(){

			private OutputField.RankBasis rankBasis = outputField.getRankBasis();

			private OutputField.RankOrder rankOrder = outputField.getRankOrder();


			public int compare(AssociationRule left, AssociationRule right){
				int order;

				switch(this.rankBasis){
					case CONFIDENCE:
						order = (left.getConfidence()).compareTo(right.getConfidence());
						break;
					case SUPPORT:
						order = (left.getSupport()).compareTo(right.getSupport());
						break;
					case LIFT:
						order = (left.getLift()).compareTo(right.getLift());
						break;
					case LEVERAGE:
						order = (left.getLeverage()).compareTo(right.getLeverage());
						break;
					case AFFINITY:
						order = (left.getAffinity()).compareTo(right.getAffinity());
						break;
					default:
						throw new UnsupportedFeatureException(outputField, this.rankBasis);
				} // End switch

				switch(this.rankOrder){
					case ASCENDING:
						return order;
					case DESCENDING:
						return -order;
					default:
						throw new UnsupportedFeatureException(outputField, this.rankOrder);
				}
			}
		};
		Collections.sort(associationRules, comparator);

		String isMultiValued = outputField.getIsMultiValued();

		// Return a single result
		if("0".equals(isMultiValued)){

			int rank = outputField.getRank();
			if(rank <= 0){
				throw new InvalidFeatureException(outputField);
			}

			int index = (rank - 1);
			if(index < associationRules.size()){
				AssociationRule associationRule = associationRules.get(index);

				return getRuleFeature(associationRule, outputField);
			} else

			{
				return null;
			}
		} else

		// Return multiple results
		if("1".equals(isMultiValued)){
			int size;

			int rank = outputField.getRank();
			if(rank < 0){
				throw new InvalidFeatureException(outputField);
			} else

			// "a zero value indicates that all output values are to be returned"
			if(rank == 0){
				size = associationRules.size();
			} else

			// "a positive value indicates the number of output values to be returned"
			{
				size = Math.min(rank, associationRules.size());
			}

			List<Object> result = new ArrayList<Object>();

			associationRules = associationRules.subList(0, size);
			for(AssociationRule associationRule : associationRules){
				result.add(getRuleFeature(associationRule, outputField));
			}

			return result;
		} else

		{
			throw new InvalidFeatureException(outputField);
		}
	}

	static
	private Object getRuleFeature(AssociationRule associationRule, OutputField outputField){
		RuleFeatureType ruleFeature = outputField.getRuleFeature();

		switch(ruleFeature){
			case RULE_ID:
				// XXX
				return associationRule.getId();
			case CONFIDENCE:
				return associationRule.getConfidence();
			case SUPPORT:
				return associationRule.getSupport();
			case LIFT:
				return associationRule.getLift();
			case LEVERAGE:
				return associationRule.getLeverage();
			case AFFINITY:
				return associationRule.getAffinity();
			default:
				throw new UnsupportedFeatureException(outputField, ruleFeature);
		}
	}
}