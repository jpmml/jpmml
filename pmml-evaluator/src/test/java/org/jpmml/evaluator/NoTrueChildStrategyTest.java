/*
 * Copyright (c) 2011 University of Tartu
 */
package org.jpmml.evaluator;

import org.jpmml.manager.*;

import org.dmg.pmml.*;

import org.junit.*;

import static org.junit.Assert.*;

public class NoTrueChildStrategyTest {

	@Test
	public void returnNullPrediction(){
		TreeModelEvaluator treeModelManager = prepareModel(NoTrueChildStrategyType.RETURN_NULL_PREDICTION);

		FieldName name = new FieldName("prob1");

		Node node = treeModelManager.evaluateTree(new LocalEvaluationContext(name, 0d));

		assertNull(node);

		Node t1 = treeModelManager.evaluateTree(new LocalEvaluationContext(name, 1d));

		assertNotNull(t1);
		assertEquals("T1", t1.getId());
	}

	@Test
	public void returnLastPrediction(){
		TreeModelEvaluator treeModelManager = prepareModel(NoTrueChildStrategyType.RETURN_LAST_PREDICTION);

		FieldName name = new FieldName("prob1");

		Node n1 = treeModelManager.evaluateTree(new LocalEvaluationContext(name, 0d));

		assertNotNull(n1);
		assertEquals("N1", n1.getId());

		Node t1 = treeModelManager.evaluateTree(new LocalEvaluationContext(name, 1d));

		assertNotNull(t1);
		assertEquals("T1", t1.getId());
	}

	static
	private TreeModelEvaluator prepareModel(NoTrueChildStrategyType noTrueChildStrategy){
		TreeModelManager treeModelManager = new TreeModelManager();

		TreeModel treeModel = treeModelManager.createClassificationModel();
		treeModel.setNoTrueChildStrategy(noTrueChildStrategy);

		FieldName prob1 = new FieldName("prob1");
		treeModelManager.addField(prob1, null, OpType.CONTINUOUS, DataType.DOUBLE, FieldUsageType.ACTIVE);

		Node n1 = treeModelManager.getOrCreateRoot();
		n1.setId("N1");
		n1.setScore("0");

		SimplePredicate t1Predicate = new SimplePredicate(prob1, SimplePredicate.Operator.GREATER_THAN);
		t1Predicate.setValue("0.33");

		Node t1 = treeModelManager.addNode(n1, t1Predicate);
		t1.setId("T1");
		t1.setScore("1");

		return new TreeModelEvaluator(treeModelManager);
	}
}