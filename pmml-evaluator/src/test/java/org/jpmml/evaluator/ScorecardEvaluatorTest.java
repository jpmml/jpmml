/*
 * Copyright (c) 2013 University of Tartu
 */
package org.jpmml.evaluator;

import java.util.*;

import org.dmg.pmml.*;

abstract
public class ScorecardEvaluatorTest extends PMMLTest {

	public ScorecardEvaluator createEvaluator() throws Exception {
		PMML pmml = loadPMML(getClass());

		ScorecardEvaluator evaluator = new ScorecardEvaluator(pmml);

		return evaluator;
	}

	protected static final Map<FieldName, Object> arguments = new LinkedHashMap<FieldName, Object>();

	static {
		arguments.put(new FieldName("department"), "engineering");
		arguments.put(new FieldName("age"), 25);
		arguments.put(new FieldName("income"), 500d);
	}
}