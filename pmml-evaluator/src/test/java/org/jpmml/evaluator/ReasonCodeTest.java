/*
 * Copyright (c) 2013 University of Tartu
 */
package org.jpmml.evaluator;

import java.util.*;

import org.dmg.pmml.*;

import com.google.common.collect.*;

abstract
public class ReasonCodeTest extends ScorecardEvaluatorTest {

	public Map<FieldName, ?> evaluateExample() throws Exception {
		ScorecardEvaluator evaluator = createEvaluator();

		Map<FieldName, Object> arguments = Maps.newLinkedHashMap();
		arguments.put(new FieldName("department"), "engineering");
		arguments.put(new FieldName("age"), 25);
		arguments.put(new FieldName("income"), 500d);

		return evaluator.evaluate(arguments);
	}
}