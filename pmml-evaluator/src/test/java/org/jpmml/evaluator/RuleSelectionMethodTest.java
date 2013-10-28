/*
 * Copyright (c) 2013 University of Tartu
 */
package org.jpmml.evaluator;

import java.util.*;

import org.dmg.pmml.*;

abstract
public class RuleSelectionMethodTest extends RuleSetModelEvaluatorTest {

	public String getRuleId(RuleSelectionMethod.Criterion criterion) throws Exception {
		RuleSetModelEvaluator evaluator = createEvaluator();

		RuleSetModel ruleSetModel = evaluator.getModel();

		RuleSet ruleSet = ruleSetModel.getRuleSet();

		List<RuleSelectionMethod> ruleSelectionMethods = ruleSet.getRuleSelectionMethods();

		// Move the specified criterion to the first place in the list
		for(Iterator<RuleSelectionMethod> it = ruleSelectionMethods.iterator(); it.hasNext(); ){
			RuleSelectionMethod ruleSelectionMethod = it.next();

			if((ruleSelectionMethod.getCriterion()).equals(criterion)){
				break;
			}

			it.remove();
		}

		Map<FieldName, ?> arguments = createArguments("BP", "HIGH", "K", 0.0621d, "Age", 36, "Na", 0.5023);

		Map<FieldName, ?> result = evaluator.evaluate(arguments);

		return getEntityId(result.get(evaluator.getTargetField()));
	}
}