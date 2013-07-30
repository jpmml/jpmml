/*
 * Copyright (c) 2013 University of Tartu
 */
package org.jpmml.evaluator;

import java.util.*;

import org.dmg.pmml.*;

import org.junit.*;

import static org.junit.Assert.*;

public class ContinuousResidualTest extends RegressionModelEvaluatorTest {

	@Test
	public void evaluate() throws Exception {
		RegressionModelEvaluator evaluator = createEvaluator();

		Map<FieldName, ?> arguments = Collections.singletonMap(evaluator.getTargetField(), 3.0);

		ModelManagerEvaluationContext context = new ModelManagerEvaluationContext(evaluator, arguments);

		Map<FieldName, ?> prediction = Collections.singletonMap(evaluator.getTargetField(), 1.0);

		Map<FieldName, ?> result = OutputUtil.evaluate(prediction, context);

		assertEquals(2.0, result.get(new FieldName("Residual")));
	}
}