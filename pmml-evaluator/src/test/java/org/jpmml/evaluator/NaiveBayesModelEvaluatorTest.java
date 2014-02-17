/*
 * Copyright (c) 2013 KNIME.com AG, Zurich, Switzerland
 */
package org.jpmml.evaluator;

import org.dmg.pmml.*;

abstract
public class NaiveBayesModelEvaluatorTest extends PMMLTest {

	public NaiveBayesModelEvaluator createEvaluator() throws Exception {
		PMML pmml = loadPMML(getClass());

		NaiveBayesModelEvaluator evaluator = new NaiveBayesModelEvaluator(pmml);

		return evaluator;
	}
}