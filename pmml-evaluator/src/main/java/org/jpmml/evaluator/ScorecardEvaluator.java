/*
 * Copyright (c) 2013 University of Tartu
 */
package org.jpmml.evaluator;

import java.util.*;

import org.jpmml.manager.*;

import org.dmg.pmml.*;

import com.google.common.collect.*;

public class ScorecardEvaluator extends ModelEvaluator<Scorecard> {

	public ScorecardEvaluator(PMML pmml){
		this(pmml, find(pmml.getModels(), Scorecard.class));
	}

	public ScorecardEvaluator(PMML pmml, Scorecard scorecard){
		super(pmml, scorecard);
	}

	@Override
	public String getSummary(){
		return "Scorecard";
	}

	@Override
	public Map<FieldName, ?> evaluate(Map<FieldName, ?> arguments){
		Scorecard scorecard = getModel();
		if(!scorecard.isScorable()){
			throw new InvalidResultException(scorecard);
		}

		Map<FieldName, ?> predictions;

		ModelManagerEvaluationContext context = new ModelManagerEvaluationContext(this);
		context.pushFrame(arguments);

		MiningFunctionType miningFunction = scorecard.getFunctionName();
		switch(miningFunction){
			case REGRESSION:
				predictions = evaluateRegression(context);
				break;
			default:
				throw new UnsupportedFeatureException(scorecard, miningFunction);
		}

		return OutputUtil.evaluate(predictions, context);
	}

	private Map<FieldName, ?> evaluateRegression(ModelManagerEvaluationContext context){
		Scorecard scorecard = getModel();

		double value = 0;

		boolean useReasonCodes = scorecard.isUseReasonCodes();

		VoteCounter<String> reasonCodePoints = new VoteCounter<String>();

		Characteristics characteristics = scorecard.getCharacteristics();
		for(Characteristic characteristic : characteristics){
			Double baselineScore = characteristic.getBaselineScore();
			if(baselineScore == null){
				baselineScore = scorecard.getBaselineScore();
			} // End if

			if(useReasonCodes){

				if(baselineScore == null){
					throw new InvalidFeatureException(characteristic);
				}
			}

			List<Attribute> attributes = characteristic.getAttributes();
			for(Attribute attribute : attributes){
				Predicate predicate = attribute.getPredicate();
				if(predicate == null){
					throw new InvalidFeatureException(attribute);
				}

				Boolean status = PredicateUtil.evaluate(predicate, context);
				if(status == null || !status.booleanValue()){
					continue;
				}

				Double partialScore = attribute.getPartialScore();
				if(partialScore == null){
					throw new InvalidFeatureException(attribute);
				}

				value += partialScore.doubleValue();

				String reasonCode = attribute.getReasonCode();
				if(reasonCode == null){
					reasonCode = characteristic.getReasonCode();
				}

				if(useReasonCodes){

					if(reasonCode == null){
						throw new InvalidFeatureException(attribute);
					}

					Double difference;

					Scorecard.ReasonCodeAlgorithm reasonCodeAlgorithm = scorecard.getReasonCodeAlgorithm();
					switch(reasonCodeAlgorithm){
						case POINTS_ABOVE:
							difference = (partialScore - baselineScore);
							break;
						case POINTS_BELOW:
							difference = (baselineScore - partialScore);
							break;
						default:
							throw new UnsupportedFeatureException(scorecard, reasonCodeAlgorithm);
					}

					reasonCodePoints.increment(reasonCode, difference);
				}

				break;
			}
		}

		Map<FieldName, ? extends Number> result = TargetUtil.evaluateRegression(value, context);

		if(useReasonCodes){
			Map.Entry<FieldName, ? extends Number> resultEntry = Iterables.getOnlyElement(result.entrySet());

			return Collections.singletonMap(resultEntry.getKey(), createScoreMap(resultEntry.getValue(), reasonCodePoints));
		}

		return result;
	}

	static
	private ScoreClassificationMap createScoreMap(Number value, Map<String, Double> reasonCodePoints){
		ScoreClassificationMap result = new ScoreClassificationMap(value);

		// Filter out meaningless (ie. negative values) explanations
		com.google.common.base.Predicate<Map.Entry<String, Double>> predicate = new com.google.common.base.Predicate<Map.Entry<String, Double>>(){

			@Override
			public boolean apply(Map.Entry<String, Double> entry){
				return Double.compare(entry.getValue(), 0) >= 0;
			}
		};
		result.putAll(Maps.filterEntries(reasonCodePoints, predicate));

		return result;
	}
}