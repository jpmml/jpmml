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
	 * @see #evaluateClassification(EvaluationContext)
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
			case CLASSIFICATION:
				predictions = evaluateClassification(context);
				break;
			default:
				throw new UnsupportedFeatureException(miningFunction);
		}

		return OutputUtil.evaluate(this, parameters, predictions);
	}

	public Map<FieldName, Double> evaluateRegression(EvaluationContext context){
		Segmentation segmentation = getSegmentation();

		MultipleModelMethodType multipleModelMethod = segmentation.getMultipleModelMethod();

		Map<Segment, ?> resultMap = evaluate(context);

		double sum = 0d;
		double weightedSum = 0d;

		Collection<? extends Map.Entry<Segment, ?>> entries = resultMap.entrySet();
		for(Map.Entry<Segment, ?> entry : entries){
			Segment segment = entry.getKey();

			Double value = ParameterUtil.toDouble(entry.getValue());

			sum += value.doubleValue();
			weightedSum += (segment.getWeight() * value.doubleValue());
		}

		Double result;

		switch(multipleModelMethod){
			case SUM:
				result = sum;
				break;
			case AVERAGE:
				result = (sum / resultMap.size());
				break;
			case WEIGHTED_AVERAGE:
				result = (weightedSum / resultMap.size());
				break;
			default:
				throw new UnsupportedFeatureException(multipleModelMethod);
		}

		return Collections.singletonMap(getTarget(), result);
	}

	public Map<FieldName, ClassificationMap> evaluateClassification(EvaluationContext context){
		Segmentation segmentation = getSegmentation();

		MultipleModelMethodType multipleModelMethod = segmentation.getMultipleModelMethod();

		Map<Segment, ?> resultMap = evaluate(context);

		ClassificationMap result = new ClassificationMap();

		Collection<? extends Map.Entry<Segment, ?>> entries = resultMap.entrySet();
		for(Map.Entry<Segment, ?> entry : entries){
			Segment segment = entry.getKey();

			String value = ParameterUtil.toString(entry.getValue());

			Double vote = result.get(value);
			if(vote == null){
				vote = 0d;
			}

			switch(multipleModelMethod){
				case MAJORITY_VOTE:
					vote += 1d;
					break;
				case WEIGHTED_MAJORITY_VOTE:
					vote += (segment.getWeight() * 1d);
					break;
				default:
					throw new UnsupportedFeatureException(multipleModelMethod);
			}

			result.put(value, vote);
		}

		result.normalizeProbabilities();

		return Collections.singletonMap(getTarget(), result);
	}

	private Map<Segment, ?> evaluate(EvaluationContext context){
		Segmentation segmentation = getSegmentation();

		MultipleModelMethodType multipleModelMethod = segmentation.getMultipleModelMethod();

		Map<Segment, Object> resultMap = new LinkedHashMap<Segment, Object>();

		ModelEvaluatorFactory evaluatorFactory = ModelEvaluatorFactory.getInstance();

		List<Segment> segments = segmentation.getSegments();
		for(Segment segment : segments){
			Predicate predicate = segment.getPredicate();

			Boolean selectable = PredicateUtil.evaluate(predicate, context);
			if(selectable == null){
				throw new EvaluationException();
			} // End if

			if(!selectable.booleanValue()){
				continue;
			}

			Model model = segment.getModel();

			Evaluator evaluator = (Evaluator)evaluatorFactory.getModelManager(getPmml(), model);

			FieldName target = evaluator.getTarget();

			Map<FieldName, ?> result = evaluator.evaluate(context.getParameters());

			Object targetValue = result.get(target);
			if(targetValue == null){
				throw new EvaluationException();
			}

			// XXX
			targetValue = EvaluatorUtil.decode(targetValue);

			switch(multipleModelMethod){
				case SELECT_FIRST:
					return Collections.singletonMap(segment, targetValue);
				case MODEL_CHAIN:
					throw new UnsupportedFeatureException(multipleModelMethod);
				default:
					resultMap.put(segment, targetValue);
					break;
			}
		}

		return resultMap;
	}
}