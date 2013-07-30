/*
 * Copyright (c) 2013 University of Tartu
 */
package org.jpmml.evaluator;

import java.util.*;

import org.dmg.pmml.*;

import org.junit.*;

import static org.junit.Assert.*;

public class PriorProbabilitiesTest extends RegressionModelEvaluatorTest {

	@Test
	public void evaluate() throws Exception {
		RegressionModelEvaluator evaluator = createEvaluator();

		ModelManagerEvaluationContext context = new ModelManagerEvaluationContext(evaluator);

		Map<FieldName, ? extends ClassificationMap> predictions = TargetUtil.evaluateClassification((ClassificationMap)null, context);

		assertEquals(1, predictions.size());

		ClassificationMap response = predictions.get(evaluator.getTargetField());

		assertEquals((Double)0.02d, response.getProbability("YES"));
		assertEquals((Double)0.98d, response.getProbability("NO"));

		Map<FieldName, ?> result = OutputUtil.evaluate(predictions, context);

		assertEquals(0.02d, result.get(new FieldName("P_responseYes")));
		assertEquals(0.98d, result.get(new FieldName("P_responseNo")));

		assertEquals("NO", result.get(new FieldName("I_response")));
		assertEquals("No", result.get(new FieldName("U_response")));
	}
}