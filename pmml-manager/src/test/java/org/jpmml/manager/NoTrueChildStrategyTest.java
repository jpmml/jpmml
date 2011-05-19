/*
 * Copyright (c) 2011 University of Tartu
 */
package org.jpmml.manager;

import java.util.*;

import org.dmg.pmml.*;

import org.junit.*;

import static org.junit.Assert.*;

public class NoTrueChildStrategyTest {

	@Test
	public void returnNullPrediction(){
		TreeModelManager treeModelManager = prepareModel(NoTrueChildStrategyType.RETURN_NULL_PREDICTION);

		Node node = treeModelManager.scoreModel(prepareParameters(0));

		assertNull(node);

		Node t1 = treeModelManager.scoreModel(prepareParameters(1));

		assertNotNull(t1);
		assertEquals("T1", t1.getId());
	}

	@Test
	public void returnLastPrediction(){
		TreeModelManager treeModelManager = prepareModel(NoTrueChildStrategyType.RETURN_LAST_PREDICTION);

		Node n1 = treeModelManager.scoreModel(prepareParameters(0));

		assertNotNull(n1);
		assertEquals("N1", n1.getId());

		Node t1 = treeModelManager.scoreModel(prepareParameters(1));

		assertNotNull(t1);
		assertEquals("T1", t1.getId());
	}

	static
	private TreeModelManager prepareModel(NoTrueChildStrategyType noTrueChildStrategy){
		TreeModelManager treeModelManager = new TreeModelManager();

		TreeModel treeModel = treeModelManager.getOrCreateModel();
		treeModel.setNoTrueChildStrategy(noTrueChildStrategy);

		FieldName prob1 = new FieldName("prob1");
		treeModelManager.addField(prob1, null, OpTypeType.CONTINUOUS, DataTypeType.DOUBLE, FieldUsageTypeType.ACTIVE);

		Node n1 = treeModelManager.getOrCreateNode();
		n1.setId("N1");
		n1.setScore("0");

		SimplePredicate t1Predicate = new SimplePredicate(prob1, SimplePredicate.Operator.GREATER_THAN);
		t1Predicate.setValue("0.33");

		Node t1 = treeModelManager.addNode(n1, t1Predicate);
		t1.setId("T1");
		t1.setScore("1");

		return treeModelManager;
	}

	static
	private Map<FieldName, Double> prepareParameters(double value){
		return Collections.singletonMap(new FieldName("prob1"), Double.valueOf(value));
	}
}