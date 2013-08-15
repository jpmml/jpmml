/*
 * Copyright (c) 2013 University of Tartu
 */
package org.jpmml.evaluator;

import org.dmg.pmml.*;

abstract
public class RuleSetModelEvaluatorTest extends PMMLTest {

	public RuleSetModelEvaluator createEvaluator() throws Exception {
		PMML pmml = loadPMML(getClass());

		RuleSetModelEvaluator evaluator = new RuleSetModelEvaluator(pmml);

		return evaluator;
	}
}