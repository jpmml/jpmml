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

	public Map<FieldName, ?> evaluate(Map<FieldName, ?> arguments){
		MiningModel miningModel = getModel();
		if(!miningModel.isScorable()){
			throw new InvalidResultException(miningModel);
		}

		Map<FieldName, ?> predictions;

		ModelManagerEvaluationContext context = new ModelManagerEvaluationContext(this, arguments);

		MiningFunctionType miningFunction = miningModel.getFunctionName();
		switch(miningFunction){
			case REGRESSION:
				predictions = evaluateRegression(context);
				break;
			case CLASSIFICATION:
				predictions = evaluateClassification(context);
				break;
			default:
				throw new UnsupportedFeatureException(miningModel, miningFunction);
		}

		return OutputUtil.evaluate(predictions, context);
	}

	Map<FieldName, ?> evaluateRegression(EvaluationContext context){
		List<SegmentResult> segmentResults = evaluate(context);

		Segmentation segmentation = getSegmentation();

		MultipleModelMethodType multipleModelMethod = segmentation.getMultipleModelMethod();
		switch(multipleModelMethod){
			case SELECT_FIRST:
			case MODEL_CHAIN:
				return dispatchSingleResult(segmentation, segmentResults);
			case SELECT_ALL:
				throw new UnsupportedFeatureException(segmentation, multipleModelMethod);
			default:
				break;
		}

		Double result;

		double sum = 0d;
		double weightedSum = 0d;

		for(SegmentResult segmentResult : segmentResults){
			Object predictedValue = EvaluatorUtil.decode(segmentResult.getPrediction());

			Double value = ParameterUtil.toDouble(predictedValue);

			sum += value.doubleValue();
			weightedSum += ((segmentResult.getSegment()).getWeight() * value.doubleValue());
		}

		int count = segmentResults.size();

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
				throw new UnsupportedFeatureException(segmentation, multipleModelMethod);
		}

		return Collections.singletonMap(getTarget(), result);
	}

	Map<FieldName, ?> evaluateClassification(EvaluationContext context){
		List<SegmentResult> segmentResults = evaluate(context);

		Segmentation segmentation = getSegmentation();

		MultipleModelMethodType multipleModelMethod = segmentation.getMultipleModelMethod();
		switch(multipleModelMethod){
			case SELECT_FIRST:
			case MODEL_CHAIN:
				return dispatchSingleResult(segmentation, segmentResults);
			case SELECT_ALL:
				throw new UnsupportedFeatureException(segmentation, multipleModelMethod);
			default:
				break;
		}

		ClassificationMap result = new ClassificationMap();

		for(SegmentResult segmentResult : segmentResults){
			Object predictedValue = EvaluatorUtil.decode(segmentResult.getPrediction());

			String category = ParameterUtil.toString(predictedValue);

			Double vote = result.get(category);
			if(vote == null){
				vote = 0d;
			}

			switch(multipleModelMethod){
				case MAJORITY_VOTE:
					vote += 1d;
					break;
				case WEIGHTED_MAJORITY_VOTE:
					vote += ((segmentResult.getSegment()).getWeight() * 1d);
					break;
				default:
					throw new UnsupportedFeatureException(segmentation, multipleModelMethod);
			}

			result.put(category, vote);
		}

		result.normalizeProbabilities();

		return Collections.singletonMap(getTarget(), result);
	}

	private Map<FieldName, ?> dispatchSingleResult(Segmentation segmentation, List<SegmentResult> results){

		if(results.size() < 1 || results.size() > 1){
			throw new MissingResultException(segmentation);
		}

		SegmentResult result = results.get(0);

		return result.getResult();
	}

	@SuppressWarnings (
		value = "fallthrough"
	)
	private List<SegmentResult> evaluate(EvaluationContext context){
		List<SegmentResult> results = new ArrayList<SegmentResult>();

		Segmentation segmentation = getSegmentation();

		MultipleModelMethodType multipleModelMethod = segmentation.getMultipleModelMethod();

		List<Segment> segments = segmentation.getSegments();
		for(Segment segment : segments){
			Predicate predicate = segment.getPredicate();
			if(predicate == null){
				throw new InvalidFeatureException(segment);
			}

			Boolean status = PredicateUtil.evaluate(predicate, context);
			if(status == null || !status.booleanValue()){
				continue;
			}

			Model model = segment.getModel();

			Evaluator evaluator = (Evaluator)evaluatorFactory.getModelManager(getPmml(), model);

			FieldName target = evaluator.getTarget();

			Map<FieldName, ?> result = evaluator.evaluate(context.getArguments());

			switch(multipleModelMethod){
				case SELECT_FIRST:
					return Collections.singletonList(new SegmentResult(segment, target, result));
				case MODEL_CHAIN:
					{
						List<FieldName> outputFields = evaluator.getOutputFields();

						for(FieldName outputField : outputFields){
							Object outputValue = result.get(outputField);
							if(outputValue == null){
								throw new MissingFieldException(outputField, segment);
							}

							outputValue = EvaluatorUtil.decode(outputValue);

							context.putArgument(outputField, outputValue);
						}

						results.clear();
					}
					// Falls through
				default:
					results.add(new SegmentResult(segment, target, result));
					break;
			}
		}

		return results;
	}

	private static final ModelEvaluatorFactory evaluatorFactory = ModelEvaluatorFactory.getInstance();

	static
	private class SegmentResult {

		private Segment segment = null;

		private FieldName predictedField = null;

		private Map<FieldName, ?> result = null;


		public SegmentResult(Segment segment, FieldName predictedField, Map<FieldName, ?> result){
			setSegment(segment);
			setPredictedField(predictedField);
			setResult(result);
		}

		public Object getPrediction(){
			return getResult().get(getPredictedField());
		}

		public Segment getSegment(){
			return this.segment;
		}

		private void setSegment(Segment segment){
			this.segment = segment;
		}

		public FieldName getPredictedField(){
			return this.predictedField;
		}

		private void setPredictedField(FieldName predictedField){
			this.predictedField = predictedField;
		}

		public Map<FieldName, ?> getResult(){
			return this.result;
		}

		private void setResult(Map<FieldName, ?> result){
			this.result = result;
		}
	}
}