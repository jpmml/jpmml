/*
 * Copyright (c) 2013 KNIME.com AG, Zurich, Switzerland
 */
package org.jpmml.evaluator;

import org.dmg.pmml.*;

abstract
public class NearestNeighborModelEvaluatorTest extends PMMLTest {

	public NearestNeighborModelEvaluator createEvaluator() throws Exception {
		PMML pmml = loadPMML(getClass());

		NearestNeighborModelEvaluator evaluator = new NearestNeighborModelEvaluator(pmml);

		return evaluator;
	}
}