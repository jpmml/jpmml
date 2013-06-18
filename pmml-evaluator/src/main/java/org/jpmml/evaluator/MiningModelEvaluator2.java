/*
 * Copyright (c) 2012 University of Tartu
 *
 */
package org.jpmml.evaluator;

import java.util.*;

import org.jpmml.manager.*;

import org.dmg.pmml.*;

public class MiningModelEvaluator2 extends MiningModelManager implements Evaluator {

	public MiningModelEvaluator2(PMML pmml){
		super(pmml);
	}

	public MiningModelEvaluator2(PMML pmml, MiningModel miningModel){
		super(pmml, miningModel);
	}

	public MiningModelEvaluator2(MiningModelManager parent){
		this(parent.getPmml(), parent.getModel());
	}

	public Object prepare(FieldName name, Object value){
		return ParameterUtil.prepare(getDataField(name), getMiningField(name), value);
	}

	/**
	 * @see #evaluateRegression(EvaluationContext)
	 * @see #evaluateClassification(EvaluationContext)
	 */
	public IPMMLResult evaluate(Map<FieldName, ?> parameters){
		MiningModel model = getModel();

		IPMMLResult predictions;

		ModelManagerEvaluationContext context = new ModelManagerEvaluationContext(this, parameters);

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

		PMMLResult res = new PMMLResult();
		res = OutputUtil.evaluate((PMMLResult)predictions, context);
		return res;
	}

	public IPMMLResult evaluateRegression(EvaluationContext context){
		List<SegmentResult> segmentResults = evaluate(context);

		Segmentation segmentation = getSegmentation();

		MultipleModelMethodType multipleModelMethod = segmentation.getMultipleModelMethod();
		switch(multipleModelMethod){
			case SELECT_FIRST:
			case MODEL_CHAIN:
				return dispatchSingleResult(segmentResults);
			case SELECT_ALL:
				throw new UnsupportedFeatureException(multipleModelMethod);
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
				throw new UnsupportedFeatureException(multipleModelMethod);
		}

		PMMLResult res = new PMMLResult();
		res.put(getTarget(), result);

		return res;
	}

	public IPMMLResult evaluateClassification(EvaluationContext context){
		List<SegmentResult> segmentResults = evaluate(context);

		Segmentation segmentation = getSegmentation();

		MultipleModelMethodType multipleModelMethod = segmentation.getMultipleModelMethod();
		switch(multipleModelMethod){
			case SELECT_FIRST:
			case MODEL_CHAIN:
				return dispatchSingleResult(segmentResults);
			case SELECT_ALL:
				throw new UnsupportedFeatureException(multipleModelMethod);
			default:
				break;
		}

		ClassificationMap result = new ClassificationMap();

		for(SegmentResult segmentResult : segmentResults){
			Object predictedValue = EvaluatorUtil.decode(segmentResult.getPrediction());

			String value = ParameterUtil.toString(predictedValue);

			Double vote = result.get(value);
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
					throw new UnsupportedFeatureException(multipleModelMethod);
			}

			result.put(value, vote);
		}

		result.normalizeProbabilities();

		PMMLResult res = new PMMLResult();
		res.put(getTarget(), result);

		return res;

		//return Collections.singletonMap(getTarget(), result);
	}

	private IPMMLResult dispatchSingleResult(List<SegmentResult> results){

		if(results.size() < 1 || results.size() > 1){
			throw new EvaluationException();
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

			IPMMLResult result = evaluator.evaluate(context.getParameters());

			switch(multipleModelMethod){
				case SELECT_FIRST:
					return Collections.singletonList(new SegmentResult(segment, target, result));
				case MODEL_CHAIN:
					{
						List<FieldName> outputFields = evaluator.getOutputFields();

						for(FieldName outputField : outputFields){
							Object outputValue = result.getValue(outputField);
							if(outputValue == null){
								throw new EvaluationException();
							}

							outputValue = EvaluatorUtil.decode(outputValue);

							context.putParameter(outputField, outputValue);
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

		private IPMMLResult result = null;


		public SegmentResult(Segment segment, FieldName predictedField, IPMMLResult result){
			setSegment(segment);
			setPredictedField(predictedField);
			setResult(result);
		}

		public Object getPrediction(){
			return getResult().getValue(getPredictedField());
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

		public IPMMLResult getResult() {
			return result;
		}

		public void setResult(IPMMLResult result) {
			this.result = result;
		}


	}
}

