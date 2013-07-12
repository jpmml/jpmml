/*
 * Copyright (c) 2013 University of Tartu
 */
package org.jpmml.evaluator;

import org.dmg.pmml.*;

abstract
public class RegressionModelEvaluatorTest extends ModelManagerTest {

	public RegressionModelEvaluator createEvaluator() throws Exception {
		PMML pmml = loadPMML(getClass());

		RegressionModelEvaluator evaluator = new RegressionModelEvaluator(pmml);

		return evaluator;
	}
}