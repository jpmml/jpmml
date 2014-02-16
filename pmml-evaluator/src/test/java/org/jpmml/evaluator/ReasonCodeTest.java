/*
 * Copyright (c) 2013 Villu Ruusmann
 */
package org.jpmml.evaluator;

import java.util.*;

import org.dmg.pmml.*;

abstract
public class ReasonCodeTest extends ScorecardEvaluatorTest {

	public Map<FieldName, ?> evaluateExample() throws Exception {
		ScorecardEvaluator evaluator = createEvaluator();

		Map<FieldName, ?> arguments = createArguments("department", "engineering", "age", 25, "income", 500d);

		return evaluator.evaluate(arguments);
	}
}