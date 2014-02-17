/*
 * Copyright (c) 2013 KNIME.com AG, Zurich, Switzerland
 */
package org.jpmml.evaluator;

import org.dmg.pmml.*;

abstract
public class SupportVectorMachineModelEvaluatorTest extends PMMLTest {

	public SupportVectorMachineModelEvaluator createEvaluator() throws Exception {
		PMML pmml = loadPMML(getClass());

		SupportVectorMachineModelEvaluator evaluator = new SupportVectorMachineModelEvaluator(pmml);

		return evaluator;
	}
}