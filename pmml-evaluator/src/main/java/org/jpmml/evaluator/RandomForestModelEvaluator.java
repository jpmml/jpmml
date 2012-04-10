/*
 * Copyright (c) 2012 University of Tartu
 */
package org.jpmml.evaluator;

import java.util.*;

import org.jpmml.manager.*;

import org.dmg.pmml.*;

public class RandomForestModelEvaluator extends RandomForestModelManager implements Evaluator {

	public RandomForestModelEvaluator(PMML pmml){
		super(pmml);
	}

	public RandomForestModelEvaluator(PMML pmml, MiningModel miningModel){
		super(pmml, miningModel);
	}

	public RandomForestModelEvaluator(RandomForestModelManager parent){
		this(parent.getPmml(), parent.getModel());
	}

	/**
	 * @see #evaluateRegression(Map)
	 */
	public Object evaluate(Map<FieldName, ?> parameters){
		MiningModel model = getModel();

		MiningFunctionType miningFunction = model.getFunctionName();
		switch(miningFunction){
			case REGRESSION:
				return evaluateRegression(parameters);
			default:
				throw new UnsupportedFeatureException(miningFunction);
		}
	}

	public Double evaluateRegression(Map<FieldName, ?> parameters){
		Segmentation segmentation = getSegmentation();

		double sum = 0;
		double weightedSum = 0;

		int count = 0;

		List<Segment> segments = getSegments();
		for(Segment segment : segments){
			Predicate predicate = segment.getPredicate();

			Boolean selectable = PredicateUtil.evaluatePredicate(predicate, parameters);
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

			String score = treeModelEvaluator.evaluate(parameters);
			if(score == null){
				throw new EvaluationException();
			}

			Double value = Double.valueOf(score);

			sum += value.doubleValue();
			weightedSum += (segment.getWeight() * value.doubleValue());

			count++;
		}

		MultipleModelMethodType multipleModelMethod = segmentation.getMultipleModelMethod();
		switch(multipleModelMethod){
			case SUM:
				return sum;
			case AVERAGE:
				return (sum / count);
			case WEIGHTED_AVERAGE:
				return (weightedSum / count); // XXX
			default:
				throw new UnsupportedFeatureException(multipleModelMethod);
		}
	}
}