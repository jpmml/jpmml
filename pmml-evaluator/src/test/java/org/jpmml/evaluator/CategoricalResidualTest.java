/*
 * Copyright (c) 2013 University of Tartu
 */
package org.jpmml.evaluator;

import java.util.*;

import org.dmg.pmml.*;

import org.junit.*;

import static org.junit.Assert.*;

public class CategoricalResidualTest extends RegressionModelEvaluatorTest {

	@Test
	public void evaluate() throws Exception {
		RegressionModelEvaluator evaluator = createEvaluator();

		// "For some row in the test data the expected value may be Y"
		Map<FieldName, ?> arguments = Collections.singletonMap(evaluator.getTargetField(), "Y");

		ModelManagerEvaluationContext context = new ModelManagerEvaluationContext(evaluator, arguments);

		ClassificationMap response = new ClassificationMap(ClassificationMap.Type.PROBABILITY);
		response.put("Y", 0.8d);
		response.put("N", 0.2d);

		Map<FieldName, ?> prediction = Collections.singletonMap(evaluator.getTargetField(), response);

		Map<FieldName, ?> result = OutputUtil.evaluate(prediction, context);

		assertTrue(VerificationUtil.acceptable(0.2d, (Number)result.get(new FieldName("Residual"))));

		// "For some other row the expected value may be N"
		arguments = Collections.singletonMap(evaluator.getTargetField(), "N");

		context = new ModelManagerEvaluationContext(evaluator, arguments);

		result = OutputUtil.evaluate(prediction, context);

		assertTrue(VerificationUtil.acceptable(-0.8d, (Number)result.get(new FieldName("Residual"))));
	}
}