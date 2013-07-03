/*
 * Copyright (c) 2013 University of Tartu
 */
package org.jpmml.evaluator;

import java.util.*;

import org.jpmml.manager.*;

import org.dmg.pmml.*;

public class ScorecardEvaluator extends ScorecardManager implements Evaluator {

	public ScorecardEvaluator(PMML pmml){
		super(pmml);
	}

	public ScorecardEvaluator(PMML pmml, Scorecard scorecard){
		super(pmml, scorecard);
	}

	public ScorecardEvaluator(ScorecardManager parent){
		super(parent.getPmml(), parent.getModel());
	}

	public Object prepare(FieldName name, Object value){
		return ParameterUtil.prepare(getDataField(name), getMiningField(name), value);
	}

	public Map<FieldName, ?> evaluate(Map<FieldName, ?> arguments){
		Scorecard scorecard = getModel();
		if(!scorecard.isScorable()){
			throw new InvalidResultException(scorecard);
		}

		Map<FieldName, ?> predictions;

		ModelManagerEvaluationContext context = new ModelManagerEvaluationContext(this, arguments);

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

	Map<FieldName, ?> evaluateRegression(EvaluationContext context){
		Scorecard scorecard = getModel();

		double score = 0;

		boolean useReasonCodes = scorecard.isUseReasonCodes();

		Map<String, Double> reasonCodePoints = new LinkedHashMap<String, Double>();

		List<Characteristic> characteristics = getCharacteristics();
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

				score += partialScore.doubleValue();

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

					Double points = reasonCodePoints.get(reasonCode);
					if(points == null){
						points = 0d;
					}

					reasonCodePoints.put(reasonCode, (points + difference));
				}

				break;
			}
		}

		Object result = score;

		if(useReasonCodes){
			List<Map.Entry<String, Double>> entries = new ArrayList<Map.Entry<String, Double>>(reasonCodePoints.entrySet());

			// Sort highest score entries first, lowest score entries last
			Comparator<Map.Entry<String, Double>> comparator = new Comparator<Map.Entry<String, Double>>(){

				public int compare(Map.Entry<String, Double> left, Map.Entry<String, Double> right){
					return -(left.getValue()).compareTo(right.getValue());
				}
			};
			Collections.sort(entries, comparator);

			List<String> reasonCodeRanking = new ArrayList<String>();

			for(Map.Entry<String, Double> entry : entries){

				// Ignore meaningless explanations
				if(entry.getValue() < 0){
					break;
				}

				reasonCodeRanking.add(entry.getKey());
			}

			result = new Score(score, reasonCodeRanking);
		}

		return Collections.singletonMap(getTargetField(), result);
	}
}