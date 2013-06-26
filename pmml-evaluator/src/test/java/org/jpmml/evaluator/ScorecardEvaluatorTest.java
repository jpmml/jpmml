/*
 * Copyright (c) 2013 University of Tartu
 */
package org.jpmml.evaluator;

import java.util.*;

import org.dmg.pmml.*;

abstract
public class ScorecardEvaluatorTest extends ModelManagerTest {

	public Map<FieldName, ?> evaluateExample() throws Exception {
		Map<FieldName, Object> parameters = new LinkedHashMap<FieldName, Object>();
		parameters.put(new FieldName("department"), "engineering");
		parameters.put(new FieldName("age"), 25);
		parameters.put(new FieldName("income"), 500d);

		Evaluator evaluator = createEvaluator();

		return evaluator.evaluate(parameters);
	}

	public ScorecardEvaluator createEvaluator() throws Exception {
		PMML pmml = loadPMML(getClass());

		ScorecardEvaluator evaluator = new ScorecardEvaluator(pmml);

		return evaluator;
	}
}