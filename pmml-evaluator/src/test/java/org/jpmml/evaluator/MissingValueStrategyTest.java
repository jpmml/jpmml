/*
 * Copyright (c) 2013 University of Tartu
 */
package org.jpmml.evaluator;

import java.util.*;

import org.dmg.pmml.*;

import org.junit.*;

import com.google.common.collect.*;

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

		Map<FieldName, ?> result = evaluator.evaluate(MissingValueStrategyTest.arguments);

		return getEntityId(result.get(evaluator.getTargetField()));
	}

	private static final Map<FieldName, Object> arguments = Maps.newLinkedHashMap();

	static {
		arguments.put(new FieldName("outlook"), "sunny");
		arguments.put(new FieldName("temperature"), null);
		arguments.put(new FieldName("humidity"), null);
	}
}