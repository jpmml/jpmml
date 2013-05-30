/*
 * Copyright (c) 2012 University of Tartu
 */
package org.jpmml.evaluator;

import java.util.*;

import org.jpmml.manager.*;

import org.dmg.pmml.*;

public class MiningModelEvaluator extends MiningModelManager implements Evaluator {

	public MiningModelEvaluator(PMML pmml){
		super(pmml);
	}

	public MiningModelEvaluator(PMML pmml, MiningModel miningModel){
		super(pmml, miningModel);
	}

	public MiningModelEvaluator(MiningModelManager parent){
		this(parent.getPmml(), parent.getModel());
	}

	public Object prepare(FieldName name, Object value){
		return ParameterUtil.prepare(getDataField(name), getMiningField(name), value);
	}

	/**
	 * @see #evaluateRegression(EvaluationContext)
	 */
	public Map<FieldName, ?> evaluate(Map<FieldName, ?> parameters){
		MiningModel model = getModel();

		Map<FieldName, ?> predictions;

		EvaluationContext context = new ModelManagerEvaluationContext(this, parameters);

		MiningFunctionType miningFunction = model.getFunctionName();
		switch(miningFunction){
			case REGRESSION:
				predictions = evaluateRegression(context);
				break;
			default:
				throw new UnsupportedFeatureException(miningFunction);
		}

		return OutputUtil.evaluate(this, parameters, predictions);
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

			Model model = segment.getModel();
			if(model == null){
				throw new EvaluationException();
			}

			Evaluator evaluator = (Evaluator)MiningModelEvaluator.evaluatorFactory.getModelManager(getPmml(), model);

			Map<FieldName, ?> result = evaluator.evaluate(context.getParameters());

			Object score = result.get(target);
			if(score == null){
				throw new EvaluationException();
			}

			score = EvaluatorUtil.decode(score);

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

	private static final ModelEvaluatorFactory evaluatorFactory = ModelEvaluatorFactory.getInstance();
}