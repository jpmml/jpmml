/*
 * Copyright (c) 2011 University of Tartu
 */
package org.jpmml.evaluator;

import java.util.*;

import org.dmg.pmml.*;

import org.junit.*;

import static org.junit.Assert.*;

public class NoTrueChildStrategyTest extends TreeModelEvaluatorTest {

	@Test
	public void returnNullPrediction() throws Exception {
		assertEquals(null, getNodeId(NoTrueChildStrategyType.RETURN_NULL_PREDICTION, 0d));
		assertEquals("T1", getNodeId(NoTrueChildStrategyType.RETURN_NULL_PREDICTION, 1d));
	}

	@Test
	public void returnLastPrediction() throws Exception {
		assertEquals("N1", getNodeId(NoTrueChildStrategyType.RETURN_LAST_PREDICTION, 0d));
		assertEquals("T1", getNodeId(NoTrueChildStrategyType.RETURN_LAST_PREDICTION, 1d));
	}

	private String getNodeId(NoTrueChildStrategyType noTrueChildStrategy, Double value) throws Exception {
		TreeModelEvaluator evaluator = createEvaluator();

		TreeModel treeModel = evaluator.getModel();
		treeModel.setNoTrueChildStrategy(noTrueChildStrategy);

		Map<FieldName, Double> arguments = Collections.singletonMap(new FieldName("probability"), value);

		Map<FieldName, ?> result = evaluator.evaluate(arguments);

		return getEntityId(result.get(evaluator.getTargetField()));
	}
}