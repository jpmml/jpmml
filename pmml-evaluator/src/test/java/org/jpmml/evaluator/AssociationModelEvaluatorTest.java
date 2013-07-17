/*
 * Copyright (c) 2013 University of Tartu
 */
package org.jpmml.evaluator;

import org.dmg.pmml.*;

abstract
public class AssociationModelEvaluatorTest extends PMMLTest {

	public AssociationModelEvaluator createEvaluator() throws Exception {
		PMML pmml = loadPMML(getClass());

		AssociationModelEvaluator evaluator = new AssociationModelEvaluator(pmml);

		return evaluator;
	}
}