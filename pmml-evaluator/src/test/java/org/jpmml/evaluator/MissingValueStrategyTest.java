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
		TreeModelEvaluator treeModelEvaluator = createEvaluator(MissingValueStrategyType.NULL_PREDICTION);

		Node node = treeModelEvaluator.evaluateTree(new LocalEvaluationContext(arguments));

		assertNull(node);
	}

	@Test
	public void lastPrediction() throws Exception {
		TreeModelEvaluator treeModelEvaluator = createEvaluator(MissingValueStrategyType.LAST_PREDICTION);

		Node node = treeModelEvaluator.evaluateTree(new LocalEvaluationContext(arguments));

		assertEquals("2", node.getId());
	}

	private TreeModelEvaluator createEvaluator(MissingValueStrategyType missingValueStrategy) throws Exception {
		TreeModelEvaluator treeModelEvaluator = createEvaluator();

		TreeModel treeModel = treeModelEvaluator.getModel();
		treeModel.setMissingValueStrategy(missingValueStrategy);

		return treeModelEvaluator;
	}

	protected static final Map<FieldName, Object> arguments = Maps.newLinkedHashMap();

	static {
		arguments.put(new FieldName("outlook"), "sunny");
		arguments.put(new FieldName("temperature"), null);
		arguments.put(new FieldName("humidity"), null);
	}
}