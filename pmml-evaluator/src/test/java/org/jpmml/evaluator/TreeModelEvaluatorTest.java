/*
 * Copyright (c) 2013 Villu Ruusmann
 */
package org.jpmml.evaluator;

import org.dmg.pmml.*;

abstract
public class TreeModelEvaluatorTest extends PMMLTest {

	public TreeModelEvaluator createEvaluator() throws Exception {
		PMML pmml = loadPMML(getClass());

		TreeModelEvaluator evaluator = new TreeModelEvaluator(pmml);

		return evaluator;
	}
}