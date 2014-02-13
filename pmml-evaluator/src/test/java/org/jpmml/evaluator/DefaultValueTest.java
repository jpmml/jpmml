/*
 * Copyright (c) 2013 University of Tartu
 */
package org.jpmml.evaluator;

import java.util.*;

import org.dmg.pmml.*;

import org.junit.*;

import static org.junit.Assert.*;

public class DefaultValueTest extends RegressionModelEvaluatorTest {

	@Test
	public void evaluate() throws Exception {
		RegressionModelEvaluator evaluator = createEvaluator();

		ModelEvaluationContext context = new ModelEvaluationContext(evaluator);

		Map<FieldName, ? extends Number> predictions = TargetUtil.evaluateRegression((Double)null, context);

		assertEquals(1, predictions.size());

		Number amount = predictions.get(evaluator.getTargetField());

		assertEquals(432.21d, amount);
	}
}