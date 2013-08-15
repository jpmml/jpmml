/*
 * Copyright (c) 2013 University of Tartu
 */
package org.jpmml.evaluator;

import java.util.*;

import org.dmg.pmml.*;

import com.google.common.collect.*;

abstract
public class RuleSelectionMethodTest extends RuleSetModelEvaluatorTest {

	public String getRuleId(RuleSelectionMethod.Criterion criterion) throws Exception {
		RuleSetModelEvaluator evaluator = createEvaluator();

		RuleSet ruleSet = evaluator.getRuleSet();

		List<RuleSelectionMethod> ruleSelectionMethods = ruleSet.getRuleSelectionMethods();

		// Move the specified criterion to the first place in the list
		for(Iterator<RuleSelectionMethod> it = ruleSelectionMethods.iterator(); it.hasNext(); ){
			RuleSelectionMethod ruleSelectionMethod = it.next();

			if((ruleSelectionMethod.getCriterion()).equals(criterion)){
				break;
			}

			it.remove();
		}

		Map<FieldName, Object> arguments = Maps.newLinkedHashMap();
		arguments.put(new FieldName("BP"), "HIGH");
		arguments.put(new FieldName("K"), 0.0621);
		arguments.put(new FieldName("Age"), 36);
		arguments.put(new FieldName("Na"), 0.5023);

		Map<FieldName, ?> result = evaluator.evaluate(arguments);

		return getEntityId(result.get(evaluator.getTargetField()));
	}
}