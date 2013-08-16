/*
 * Copyright (c) 2012 University of Tartu
 */
package org.jpmml.evaluator;

import java.util.*;

import org.jpmml.manager.*;

import org.dmg.pmml.*;

import com.google.common.collect.*;

public class MiningModelEvaluator extends MiningModelManager implements Evaluator {

	public MiningModelEvaluator(PMML pmml){
		super(pmml);
	}

	public MiningModelEvaluator(PMML pmml, MiningModel miningModel){
		super(pmml, miningModel);
	}

	@Override
	public Object prepare(FieldName name, Object value){
		return ParameterUtil.prepare(getDataField(name), getMiningField(name), value);
	}

	@Override
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

	private Map<FieldName, ?> evaluateRegression(ModelManagerEvaluationContext context){
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

		double sum = 0d;
		double weightedSum = 0d;

		for(SegmentResult segmentResult : segmentResults){
			Object targetValue = EvaluatorUtil.decode(segmentResult.getTargetValue());

			Double value = (Double)ParameterUtil.cast(DataType.DOUBLE, targetValue);

			sum += value.doubleValue();
			weightedSum += ((segmentResult.getSegment()).getWeight() * value.doubleValue());
		}

		Double result;

		switch(multipleModelMethod){
			case SUM:
				result = sum;
				break;
			case AVERAGE:
				result = (sum / segmentResults.size());
				break;
			case WEIGHTED_AVERAGE:
				result = (weightedSum / segmentResults.size());
				break;
			default:
				throw new UnsupportedFeatureException(segmentation, multipleModelMethod);
		}

		return TargetUtil.evaluateRegression(result, context);
	}

	private Map<FieldName, ?> evaluateClassification(ModelManagerEvaluationContext context){
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

		ClassificationMap result = new ClassificationMap(ClassificationMap.Type.PROBABILITY);

		for(SegmentResult segmentResult : segmentResults){
			Object targetValue = EvaluatorUtil.decode(segmentResult.getTargetValue());

			String category = (String)ParameterUtil.cast(DataType.STRING, targetValue);

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

		result.normalizeValues();

		return TargetUtil.evaluateClassification(result, context);
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
		MiningModel miningModel = getModel();

		List<SegmentResult> results = Lists.newArrayList();

		Segmentation segmentation = getSegmentation();

		MultipleModelMethodType multipleModelMethod = segmentation.getMultipleModelMethod();

		Model lastModel = null;

		MiningFunctionType miningFunction = miningModel.getFunctionName();

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
			if(model == null){
				throw new InvalidFeatureException(segment);
			}

			// "With the exception of modelChain models, all model elements used inside Segment elements in one MiningModel must have the same MINING-FUNCTION"
			switch(multipleModelMethod){
				case MODEL_CHAIN:
					lastModel = model;
					break;
				default:
					if(!(miningFunction).equals(model.getFunctionName())){
						throw new InvalidFeatureException(model);
					}
					break;
			}

			Evaluator evaluator = createEvaluator(model);

			FieldName targetField = evaluator.getTargetField();

			Map<FieldName, ?> result = evaluator.evaluate(context.getArguments());

			switch(multipleModelMethod){
				case SELECT_FIRST:
					return Collections.singletonList(new SegmentResult(segment, targetField, result));
				case MODEL_CHAIN:
					{
						Map<FieldName, Object> frame = Maps.newLinkedHashMap();

						List<FieldName> outputFields = evaluator.getOutputFields();

						for(FieldName outputField : outputFields){
							Object outputValue = result.get(outputField);
							if(outputValue == null){
								throw new MissingFieldException(outputField, segment);
							}

							outputValue = EvaluatorUtil.decode(outputValue);

							frame.put(outputField, outputValue);
						}

						// "The OutputFields from one model element can be passed as input to the MiningSchema of subsequent models"
						context.pushFrame(frame);

						results.clear();
					}
					// Falls through
				default:
					results.add(new SegmentResult(segment, targetField, result));
					break;
			}
		}

		// "The model element used inside the last Segment element executed must have the same MINING-FUNCTION"
		switch(multipleModelMethod){
			case MODEL_CHAIN:
				if(lastModel != null && !(miningFunction).equals(lastModel.getFunctionName())){
					throw new InvalidFeatureException(lastModel);
				}
				break;
			default:
				break;
		}

		return results;
	}

	private Evaluator createEvaluator(Model model){
		ModelManager<?> modelManager = MiningModelEvaluator.evaluatorFactory.getModelManager(getPmml(), model);

		return (Evaluator)modelManager;
	}

	private static final ModelEvaluatorFactory evaluatorFactory = ModelEvaluatorFactory.getInstance();

	static
	private class SegmentResult {

		private Segment segment = null;

		private FieldName targetField = null;

		private Map<FieldName, ?> result = null;


		public SegmentResult(Segment segment, FieldName targetField, Map<FieldName, ?> result){
			setSegment(segment);
			setTargetField(targetField);
			setResult(result);
		}

		public Object getTargetValue(){
			return getResult().get(getTargetField());
		}

		public Segment getSegment(){
			return this.segment;
		}

		private void setSegment(Segment segment){
			this.segment = segment;
		}

		public FieldName getTargetField(){
			return this.targetField;
		}

		private void setTargetField(FieldName targetField){
			this.targetField = targetField;
		}

		public Map<FieldName, ?> getResult(){
			return this.result;
		}

		private void setResult(Map<FieldName, ?> result){
			this.result = result;
		}
	}
}