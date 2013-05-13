/*
 * Copyright (c) 2012 University of Tartu
 */
package org.jpmml.evaluator;

import java.util.*;

import org.jpmml.manager.*;

import org.dmg.pmml.*;

public class RandomForestEvaluator extends RandomForestManager implements Evaluator {

	public RandomForestEvaluator(PMML pmml){
		super(pmml);
	}

	public RandomForestEvaluator(PMML pmml, MiningModel miningModel){
		super(pmml, miningModel);
	}

	public RandomForestEvaluator(RandomForestManager parent){
		this(parent.getPmml(), parent.getModel());
	}

	/**
	 * @see #evaluateRegression(EvaluationContext)
	 */
	public Map<FieldName, ?> evaluate(Map<FieldName, ?> parameters){
		MiningModel model = getModel();

		EvaluationContext context = new ModelManagerEvaluationContext(this, parameters);

		MiningFunctionType miningFunction = model.getFunctionName();
		switch(miningFunction){
			case REGRESSION:
				return evaluateRegression(context);
			default:
				throw new UnsupportedFeatureException(miningFunction);
		}
	}

	public Map<FieldName, Double> evaluateRegression(EvaluationContext context){
		Segmentation segmentation = getSegmentation();

		double sum = 0;
		double weightedSum = 0;

		int count = 0;

		FieldName target = getTarget();

		List<Segment> segments = getSegments();
		for(Segment segment : segments){
			Predicate predicate = segment.getPredicate();

			Boolean selectable = PredicateUtil.evaluatePredicate(predicate, context);
			if(selectable == null){
				throw new EvaluationException();
			} // End if

			if(!selectable.booleanValue()){
				continue;
			}

			TreeModel treeModel = (TreeModel)segment.getModel();
			if(treeModel == null){
				throw new EvaluationException();
			}

			TreeModelEvaluator treeModelEvaluator = new TreeModelEvaluator(getPmml(), treeModel);

			Map<FieldName, String> result = treeModelEvaluator.evaluate(context.getParameters());

			String score = result.get(target);
			if(score == null){
				throw new EvaluationException();
			}

			Double value = ParameterUtil.toDouble(score);

			sum += value.doubleValue();
			weightedSum += (segment.getWeight() * value.doubleValue());

			count++;
		}

		double result;

		MultipleModelMethodType multipleModelMethod = segmentation.getMultipleModelMethod();
		switch(multipleModelMethod){
			case SUM:
				result = sum;
				break;
			case AVERAGE:
				result = (sum / count);
				break;
			case WEIGHTED_AVERAGE:
				result = (weightedSum / count);
				break;
			default:
				throw new UnsupportedFeatureException(multipleModelMethod);
		}

		return Collections.singletonMap(target, result);
	}
}