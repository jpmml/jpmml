/*
 * Copyright, KNIME.com AG, Zurich, Switzerland
 */
package org.jpmml.evaluator;

import java.util.*;

import org.dmg.pmml.*;

import org.junit.*;

import com.google.common.collect.*;

import static org.junit.Assert.*;

public class VectorInstanceTest extends SupportVectorMachineModelEvaluatorTest {

	@Test
	public void evaluate() throws Exception {
		SupportVectorMachineModelEvaluator evaluator = createEvaluator();

		assertTrue(VerificationUtil.acceptable(-0.3995764, classify(evaluator, 0.0d, 0.0d)));
		assertTrue(VerificationUtil.acceptable(0.3995764, classify(evaluator, 0.0d, 1.0d)));
		assertTrue(VerificationUtil.acceptable(0.3995764, classify(evaluator, 1.0d, 0.0d)));
		assertTrue(VerificationUtil.acceptable(-0.3995764, classify(evaluator, 1.0d, 1.0d)));
	}

	static
	private double classify(Evaluator evaluator, double x1, double x2){
		Map<FieldName, Object> arguments = Maps.newLinkedHashMap();
		arguments.put(new FieldName("x1"), x1);
		arguments.put(new FieldName("x2"), x2);

		Map<FieldName, ?> result = evaluator.evaluate(arguments);

		FieldName targetField = evaluator.getTargetField();

		ClassificationMap targetValue = (ClassificationMap)result.get(targetField);

		return targetValue.get(targetValue.getResult());
	}
}