/*
 * Copyright (c) 2013 University of Tartu
 */
package org.jpmml.evaluator;

import java.util.*;

import org.dmg.pmml.*;

import org.junit.*;

import static org.junit.Assert.*;

public class ContrastMatrixTest extends GeneralRegressionModelEvaluatorTest {

	@Test
	public void evaluate() throws Exception {
		GeneralRegressionModelEvaluator evaluator = createEvaluator();

		Map<FieldName, ?> arguments = createArguments("gender", "f", "educ", 19d, "jobcat", "3", "salbegin", 45000d);

		Map<FieldName, ?> result = evaluator.evaluate(arguments);

		Number probabilityLow = (Number)result.get(new FieldName("Probability_Low"));
		Number probabilityHigh = (Number)result.get(new FieldName("Probability_High"));

		// Expected values have been calculated by hand
		assertTrue(VerificationUtil.acceptable(0.81956470d, probabilityLow));
		assertTrue(VerificationUtil.acceptable(0.18043530d, probabilityHigh));
	}
}