/*
 * Copyright (c) 2013 University of Tartu
 */
package org.jpmml.evaluator;

import java.util.*;

import org.jpmml.manager.*;

import org.dmg.pmml.*;

import com.google.common.collect.*;

public class TargetUtil {

	private TargetUtil(){
	}

	static
	public Map<FieldName, ? extends Number> evaluateRegression(Double value, ModelManagerEvaluationContext context){
		ModelManager<?> modelManager = context.getModelManager();

		FieldName targetField = modelManager.getTargetField();

		return evaluateRegression(Collections.singletonMap(targetField, value), context);
	}

	/**
	 * Evaluates the {@link Targets} element for {@link MiningFunctionType#REGRESSION regression} models.
	 */
	static
	public Map<FieldName, ? extends Number> evaluateRegression(Map<FieldName, Double> predictions, ModelManagerEvaluationContext context){
		ModelManager<?> modelManager = context.getModelManager();

		Targets targets = modelManager.getOrCreateTargets();
		if(Iterables.isEmpty(targets.getTargets())){
			return predictions;
		}

		Map<FieldName, Number> result = Maps.newLinkedHashMap();

		Collection<Map.Entry<FieldName, Double>> entries = predictions.entrySet();
		for(Map.Entry<FieldName, Double> entry : entries){
			FieldName key = entry.getKey();
			Number value = entry.getValue();

			Target target = modelManager.getTarget(key);
			if(target != null){

				if(value != null){
					value = process(target, entry.getValue());
				}
			}

			result.put(key, value);
		}

		return result;
	}

	static
	public Number process(Target target, Double value){
		double result = value.doubleValue();

		Double min = target.getMin();
		if(min != null){
			result = Math.max(result, min.doubleValue());
		}

		Double max = target.getMax();
		if(max != null){
			result = Math.min(result, max.doubleValue());
		}

		result = (result * target.getRescaleFactor() + target.getRescaleConstant());

		Target.CastInteger castInteger = target.getCastInteger();
		if(castInteger == null){
			return result;
		}

		switch(castInteger){
			case ROUND:
				return (int)Math.round(result);
			case CEILING:
				return (int)Math.ceil(result);
			case FLOOR:
				return (int)Math.floor(result);
			default:
				throw new UnsupportedFeatureException(target, castInteger);
		}
	}
}