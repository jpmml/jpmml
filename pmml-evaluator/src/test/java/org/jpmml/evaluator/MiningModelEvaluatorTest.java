/*
 * Copyright (c) 2013 University of Tartu
 */
package org.jpmml.evaluator;

import org.dmg.pmml.*;

abstract
public class MiningModelEvaluatorTest extends PMMLTest {

	public MiningModelEvaluator createEvaluator() throws Exception {
		PMML pmml = loadPMML(getClass());

		MiningModelEvaluator evaluator = new MiningModelEvaluator(pmml);

		return evaluator;
	}
}