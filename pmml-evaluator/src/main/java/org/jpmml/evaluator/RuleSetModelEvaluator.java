/*
 * Copyright (c) 2013 University of Tartu
 */
package org.jpmml.evaluator;

import java.util.*;

import org.jpmml.manager.*;

import org.dmg.pmml.*;

import com.google.common.collect.*;

public class RuleSetModelEvaluator extends RuleSetModelManager implements Evaluator {

	public RuleSetModelEvaluator(PMML pmml){
		super(pmml);
	}

	public RuleSetModelEvaluator(PMML pmml, RuleSetModel ruleSetModel){
		super(pmml, ruleSetModel);
	}

	@Override
	public Object prepare(FieldName name, Object value){
		return ParameterUtil.prepare(getDataField(name), getMiningField(name), value);
	}

	@Override
	public Map<FieldName, ?> evaluate(Map<FieldName, ?> arguments){
		RuleSetModel ruleSetModel = getModel();
		if(!ruleSetModel.isScorable()){
			throw new InvalidResultException(ruleSetModel);
		}

		Map<FieldName, ?> predictions;

		ModelManagerEvaluationContext context = new ModelManagerEvaluationContext(this, arguments);

		MiningFunctionType miningFunction = ruleSetModel.getFunctionName();
		switch(miningFunction){
			case CLASSIFICATION:
				predictions = evaluateRuleSet(context);
				break;
			default:
				throw new UnsupportedFeatureException(ruleSetModel, miningFunction);
		}

		return OutputUtil.evaluate(predictions, context);
	}

	private Map<FieldName, RuleClassificationMap> evaluateRuleSet(EvaluationContext context){
		RuleSet ruleSet = getRuleSet();

		List<RuleSelectionMethod> ruleSelectionMethods = ruleSet.getRuleSelectionMethods();

		RuleSelectionMethod ruleSelectionMethod;

		// "If more than one method is included, the first method is used as the default method for scoring"
		if(ruleSelectionMethods.size() > 0){
			ruleSelectionMethod = ruleSelectionMethods.get(0);
		} else

		{
			throw new InvalidFeatureException(ruleSet);
		}

		// Both the ordering of keys and values is significant.
		// Guava's ListMultimap does not keep track of the insertion order of keys, so it has to be handled separately
		final
		Set<String> keys = Sets.newLinkedHashSet();

		final
		ListMultimap<String, SimpleRule> firedRules = ArrayListMultimap.create();

		RuleCollector collector = new RuleCollector(){

			@Override
			public void add(SimpleRule rule){
				keys.add(rule.getScore());

				firedRules.put(rule.getScore(), rule);
			}
		};

		List<Rule> rules = ruleSet.getRules();
		for(Rule rule : rules){
			collectRules(rule, collector, context);
		}

		RuleClassificationMap result = new RuleClassificationMap();

		RuleSelectionMethod.Criterion criterion = ruleSelectionMethod.getCriterion();

		for(String key : keys){
			List<SimpleRule> keyRules = firedRules.get(key);

			switch(criterion){
				case FIRST_HIT:
					{
						SimpleRule winner = keyRules.get(0);

						// The first value of the first key
						if(result.getEntity() == null){
							result.setEntity(winner);
						}

						result.put(key, winner.getConfidence());
					}
					break;
				case WEIGHTED_SUM:
					{
						SimpleRule winner = null;

						double totalWeight = 0;

						for(SimpleRule keyRule : keyRules){

							if(winner == null || (winner.getWeight() < keyRule.getWeight())){
								winner = keyRule;
							}

							totalWeight += keyRule.getWeight();
						}

						result.put(winner, key, totalWeight / firedRules.size());
					}
					break;
				case WEIGHTED_MAX:
					{
						SimpleRule winner = null;

						for(SimpleRule keyRule : keyRules){

							if(winner == null || (winner.getWeight() < keyRule.getWeight())){
								winner = keyRule;
							}
						}

						result.put(winner, key, winner.getConfidence());
					}
					break;
				default:
					throw new UnsupportedFeatureException(ruleSelectionMethod, criterion);
			}
		}

		return Collections.singletonMap(getTargetField(), result);
	}

	static
	private void collectRules(Rule rule, RuleCollector collector, EvaluationContext context){
		Predicate predicate = rule.getPredicate();
		if(predicate == null){
			throw new InvalidFeatureException(rule);
		}

		Boolean status = PredicateUtil.evaluate(predicate, context);
		if(status == null || !status.booleanValue()){
			return;
		} // End if

		if(rule instanceof SimpleRule){
			SimpleRule simpleRule = (SimpleRule)rule;

			collector.add(simpleRule);
		} else

		if(rule instanceof CompoundRule){
			CompoundRule compoundRule = (CompoundRule)rule;

			List<Rule> childRules = compoundRule.getRules();
			for(Rule childRule : childRules){
				collectRules(childRule, collector, context);
			}
		}
	}

	static
	private interface RuleCollector {

		void add(SimpleRule rule);
	}
}