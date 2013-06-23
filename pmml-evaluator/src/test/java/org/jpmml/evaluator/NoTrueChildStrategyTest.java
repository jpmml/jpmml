/*
 * Copyright (c) 2011 University of Tartu
 */
package org.jpmml.evaluator;

import org.dmg.pmml.*;

import org.junit.*;

import static org.junit.Assert.*;

public class NoTrueChildStrategyTest extends TreeModelEvaluatorTest {

	@Test
	public void returnNullPrediction() throws Exception {
		TreeModelEvaluator treeModelEvaluator = createEvaluator(NoTrueChildStrategyType.RETURN_NULL_PREDICTION);

		FieldName name = new FieldName("probability");

		Node node = treeModelEvaluator.evaluateTree(new LocalEvaluationContext(name, 0d));

		assertNull(node);

		node = treeModelEvaluator.evaluateTree(new LocalEvaluationContext(name, 1d));

		assertEquals("T1", node.getId());
	}

	@Test
	public void returnLastPrediction() throws Exception {
		TreeModelEvaluator treeModelEvaluator = createEvaluator(NoTrueChildStrategyType.RETURN_LAST_PREDICTION);

		FieldName name = new FieldName("probability");

		Node node = treeModelEvaluator.evaluateTree(new LocalEvaluationContext(name, 0d));

		assertEquals("N1", node.getId());

		node = treeModelEvaluator.evaluateTree(new LocalEvaluationContext(name, 1d));

		assertEquals("T1", node.getId());
	}

	static
	private TreeModelEvaluator createEvaluator(NoTrueChildStrategyType noTrueChildStrategy) throws Exception {
		PMML pmml = loadPMML(NoTrueChildStrategyTest.class);

		TreeModelEvaluator treeModelEvaluator = new TreeModelEvaluator(pmml);

		TreeModel treeModel = treeModelEvaluator.getModel();
		treeModel.setNoTrueChildStrategy(noTrueChildStrategy);

		return treeModelEvaluator;
	}
}