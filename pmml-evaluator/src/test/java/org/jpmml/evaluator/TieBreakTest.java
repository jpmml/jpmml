/*
 * Copyright, KNIME.com AG, Zurich, Switzerland
 */
package org.jpmml.evaluator;

import java.util.*;

import org.dmg.pmml.*;

import org.junit.*;

import static org.junit.Assert.*;

public class TieBreakTest extends NearestNeighborModelEvaluatorTest {

	@Test
	public void firstLevel() throws Exception {
		NearestNeighborModelEvaluator evaluator = createEvaluator();

		Map<FieldName, ?> arguments = createArguments("input", 1.5d);

		Map<FieldName, ?> result = evaluator.evaluate(arguments);

		Object prediction = result.get(new FieldName("output"));

		assertEquals("medium", EvaluatorUtil.decode(prediction));
	}

	@Test
	public void secondLevel() throws Exception {
		NearestNeighborModelEvaluator evaluator = createEvaluator();

		Map<FieldName, ?> arguments = createArguments("input", 3.5d);

		Map<FieldName, ?> result = evaluator.evaluate(arguments);

		Object prediction = result.get(new FieldName("output"));

		assertEquals("high", EvaluatorUtil.decode(prediction));
	}
}