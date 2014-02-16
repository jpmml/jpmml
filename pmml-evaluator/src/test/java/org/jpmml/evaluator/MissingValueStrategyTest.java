/*
 * Copyright (c) 2013 Villu Ruusmann
 */
package org.jpmml.evaluator;

import java.util.*;

import org.dmg.pmml.*;

import org.junit.*;

import static org.junit.Assert.*;

public class MissingValueStrategyTest extends TreeModelEvaluatorTest {

	@Test
	public void nullPrediction() throws Exception {
		assertEquals(null, getNodeId(MissingValueStrategyType.NULL_PREDICTION));
	}

	@Test
	public void lastPrediction() throws Exception {
		assertEquals("2", getNodeId(MissingValueStrategyType.LAST_PREDICTION));
	}

	private String getNodeId(MissingValueStrategyType missingValueStrategy) throws Exception {
		TreeModelEvaluator evaluator = createEvaluator();

		TreeModel treeModel = evaluator.getModel();
		treeModel.setMissingValueStrategy(missingValueStrategy);

		Map<FieldName, ?> arguments = createArguments("outlook", "sunny", "temperature", null, "humidity", null);

		Map<FieldName, ?> result = evaluator.evaluate(arguments);

		return getEntityId(result.get(evaluator.getTargetField()));
	}


}