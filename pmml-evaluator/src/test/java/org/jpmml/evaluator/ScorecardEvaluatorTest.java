/*
 * Copyright (c) 2013 University of Tartu
 */
package org.jpmml.evaluator;

import org.dmg.pmml.*;

abstract
public class ScorecardEvaluatorTest extends PMMLTest {

	public ScorecardEvaluator createEvaluator() throws Exception {
		PMML pmml = loadPMML(getClass());

		ScorecardEvaluator evaluator = new ScorecardEvaluator(pmml);

		return evaluator;
	}
}