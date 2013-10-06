/*
 * Copyright, KNIME.com AG, Zurich, Switzerland
 */
package org.jpmml.evaluator;

import java.util.*;

import org.jpmml.manager.*;

import org.apache.commons.math3.util.*;

import org.dmg.pmml.*;

import com.google.common.cache.*;
import com.google.common.collect.*;

public class NaiveBayesModelEvaluator extends NaiveBayesModelManager implements Evaluator {

	public NaiveBayesModelEvaluator(PMML pmml){
		super(pmml);
	}

	public NaiveBayesModelEvaluator(PMML pmml, NaiveBayesModel naiveBayesModel){
		super(pmml, naiveBayesModel);
	}

	@Override
	public FieldValue prepare(FieldName name, Object value){
		return ArgumentUtil.prepare(getDataField(name), getMiningField(name), value);
	}

	@Override
	public Map<FieldName, ?> evaluate(Map<FieldName, ?> arguments){
		NaiveBayesModel naiveBayesModel = getModel();
		if(!naiveBayesModel.isScorable()){
			throw new InvalidResultException(naiveBayesModel);
		}

		Map<FieldName, ?> predictions;

		ModelManagerEvaluationContext context = new ModelManagerEvaluationContext(this);
		context.pushFrame(arguments);

		MiningFunctionType miningFunction = naiveBayesModel.getFunctionName();
		switch(miningFunction){
			case CLASSIFICATION:
				predictions = evaluateClassification(context);
				break;
			default:
				throw new UnsupportedFeatureException(naiveBayesModel, miningFunction);
		}

		return OutputUtil.evaluate(predictions, context);
	}

	private Map<FieldName, ? extends ClassificationMap> evaluateClassification(ModelManagerEvaluationContext context){
		NaiveBayesModel naiveBayesModel = getModel();

		// Probability calculations use logarithmic scale for greater numerical stability
		ClassificationMap result = new ClassificationMap(ClassificationMap.Type.PROBABILITY);

		Map<FieldName, Map<String, Double>> countsMap = getCountsMap();

		List<BayesInput> bayesInputs = getBayesInputs();
		for(BayesInput bayesInput : bayesInputs){
			FieldName name = FieldName.create(bayesInput.getFieldName());

			FieldValue value = ExpressionUtil.evaluate(name, context);

			// "Missing values are ignored"
			if(value == null){
				continue;
			}

			TargetValueStats targetValueStats = getTargetValueStats(bayesInput);
			if(targetValueStats != null){
				calculateContinuousProbabilities(value, targetValueStats, result);

				continue;
			}

			Map<String, Double> counts = countsMap.get(name);

			DerivedField derivedField = bayesInput.getDerivedField();
			if(derivedField != null){
				Expression expression = derivedField.getExpression();
				if(!(expression instanceof Discretize)){
					throw new InvalidFeatureException(derivedField);
				}

				Discretize discretize = (Discretize)expression;

				value = DiscretizationUtil.discretize(discretize, value);
				if(value == null){
					throw new EvaluationException();
				}

				value = FieldValueUtil.refine(derivedField, value);
			}

			TargetValueCounts targetValueCounts = getTargetValueCounts(bayesInput, value);
			if(targetValueCounts != null){
				calculateDiscreteProbabilities(counts, targetValueCounts, naiveBayesModel.getThreshold(), result);
			}
		}

		BayesOutput bayesOutput = getBayesOutput();

		calculatePriorProbabilities(bayesOutput.getTargetValueCounts(), result);

		final Double max = Collections.max(result.values());

		// Convert from logarithmic scale to normal scale
		Collection<Map.Entry<String, Double>> entries = result.entrySet();
		for(Map.Entry<String, Double> entry : entries){
			entry.setValue(Math.exp(entry.getValue() - max));
		}

		result.normalizeValues();

		return TargetUtil.evaluateClassification(result, context);
	}

	private void calculateContinuousProbabilities(FieldValue value, TargetValueStats targetValueStats, Map<String, Double> probabilities){
		double x = (value.asNumber()).doubleValue();

		for(TargetValueStat targetValueStat : targetValueStats){
			String targetValue = targetValueStat.getValue();

			ContinuousDistribution distribution = targetValueStat.getContinuousDistribution();
			if(!(distribution instanceof GaussianDistribution)){
				throw new InvalidFeatureException(targetValueStat);
			}

			GaussianDistribution gaussianDistribution = (GaussianDistribution)distribution;

			double mean = gaussianDistribution.getMean();
			double variance = gaussianDistribution.getVariance();

			double probability = Math.exp(-Math.pow(x - mean, 2) / (2d * variance)) / Math.sqrt(2d * Math.PI * variance);

			updateSum(targetValue, Math.log(probability), probabilities);
		}
	}

	private void calculateDiscreteProbabilities(Map<String, Double> counts, TargetValueCounts targetValueCounts, double threshold, Map<String, Double> probabilities){

		for(TargetValueCount targetValueCount : targetValueCounts){
			String targetValue = targetValueCount.getValue();

			Double count = counts.get(targetValue);

			double probability = (targetValueCount.getCount() / count);

			// Replace zero probability with the default (usually very small) probability
			if(VerificationUtil.isZero(probability, Precision.EPSILON)){
				probability = threshold;
			}

			updateSum(targetValue, Math.log(probability), probabilities);
		}
	}

	private void calculatePriorProbabilities(TargetValueCounts targetValueCounts, Map<String, Double> probabilities){

		for(TargetValueCount targetValueCount : targetValueCounts){
			String targetValue = targetValueCount.getValue();

			updateSum(targetValue, Math.log(targetValueCount.getCount()), probabilities);
		}
	}

	protected Map<FieldName, Map<String, Double>> getCountsMap(){
		return getValue(NaiveBayesModelEvaluator.countCache);
	}

	static
	private Map<FieldName, Map<String, Double>> calculateCounts(NaiveBayesModel naiveBayesModel){
		Map<FieldName, Map<String, Double>> result = Maps.newLinkedHashMap();

		List<BayesInput> bayesInputs = CacheUtil.getValue(naiveBayesModel, NaiveBayesModelManager.bayesInputCache);
		for(BayesInput bayesInput : bayesInputs){
			FieldName name = FieldName.create(bayesInput.getFieldName());

			Map<String, Double> counts = Maps.newLinkedHashMap();

			List<PairCounts> pairCounts = bayesInput.getPairCounts();
			for(PairCounts pairCount : pairCounts){
				TargetValueCounts targetValueCounts = pairCount.getTargetValueCounts();

				for(TargetValueCount targetValueCount : targetValueCounts){
					updateSum(targetValueCount.getValue(), targetValueCount.getCount(), counts);
				}
			}

			result.put(name, counts);
		}

		return result;
	}

	static
	private void updateSum(String key, Double value, Map<String, Double> counts){
		Double count = counts.get(key);
		if(count == null){
			count = 0d;
		}

		counts.put(key, count + value);
	}

	static
	private TargetValueStats getTargetValueStats(BayesInput bayesInput){
		return bayesInput.getTargetValueStats();
	}

	static
	private TargetValueCounts getTargetValueCounts(BayesInput bayesInput, FieldValue value){
		List<PairCounts> pairCounts = bayesInput.getPairCounts();
		for(PairCounts pairCount : pairCounts){

			if((value).equalsString(pairCount.getValue())){
				return pairCount.getTargetValueCounts();
			}
		}

		return null;
	}

	private static final LoadingCache<NaiveBayesModel, Map<FieldName, Map<String, Double>>> countCache = CacheBuilder.newBuilder()
		.weakKeys()
		.build(new CacheLoader<NaiveBayesModel, Map<FieldName, Map<String, Double>>>(){

			@Override
			public Map<FieldName, Map<String, Double>> load(NaiveBayesModel naiveBayesModel){
				return calculateCounts(naiveBayesModel);
			}
		});
}