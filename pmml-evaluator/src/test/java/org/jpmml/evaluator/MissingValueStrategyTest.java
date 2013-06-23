/*
 * Copyright (c) 2013 University of Tartu
 */
package org.jpmml.evaluator;

import java.util.*;

import org.dmg.pmml.*;

import org.junit.*;

import static org.junit.Assert.*;

public class MissingValueStrategyTest extends TreeModelEvaluatorTest {

	@Test
	public void nullPrediction() throws Exception {
		TreeModelEvaluator treeModelEvaluator = createEvaluator(MissingValueStrategyType.NULL_PREDICTION);

		Node node = treeModelEvaluator.evaluateTree(createEvaluationContext());

		assertNull(node);
	}

	@Test
	public void lastPrediction() throws Exception {
		TreeModelEvaluator treeModelEvaluator = createEvaluator(MissingValueStrategyType.LAST_PREDICTION);

		Node node = treeModelEvaluator.evaluateTree(createEvaluationContext());

		assertEquals("2", node.getId());
	}

	static
	private EvaluationContext createEvaluationContext(){
		Map<FieldName, Object> parameters = new LinkedHashMap<FieldName, Object>();
		parameters.put(new FieldName("outlook"), "sunny");
		parameters.put(new FieldName("temperature"), null);
		parameters.put(new FieldName("humidity"), null);

		return new LocalEvaluationContext(parameters);
	}

	static
	private TreeModelEvaluator createEvaluator(MissingValueStrategyType missingValueStrategy) throws Exception {
		PMML pmml = loadPMML(MissingValueStrategyTest.class);

		TreeModelEvaluator treeModelEvaluator = new TreeModelEvaluator(pmml);

		TreeModel treeModel = treeModelEvaluator.getModel();
		treeModel.setMissingValueStrategy(missingValueStrategy);

		return treeModelEvaluator;
	}
}