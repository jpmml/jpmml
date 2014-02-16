/*
 * Copyright (c) 2013 Villu Ruusmann
 */
package org.jpmml.evaluator;

import java.util.*;

import org.dmg.pmml.*;

import org.junit.*;

import static org.junit.Assert.*;

public class OutputTest extends RegressionModelEvaluatorTest {

	@Test
	public void evaluate() throws Exception {
		RegressionModelEvaluator regressionModelEvaluator = createEvaluator();

		Map<FieldName, ?> predictions = createArguments("result", 8d);

		ModelEvaluationContext context = new ModelEvaluationContext(regressionModelEvaluator);

		Map<FieldName, ?> result = OutputUtil.evaluate(predictions, context);

		assertEquals(8d, result.get(new FieldName("RawResult")));
		assertEquals(35d, result.get(new FieldName("FinalResult")));

		assertEquals("waive", result.get(new FieldName("BusinessDecision")));
	}
}