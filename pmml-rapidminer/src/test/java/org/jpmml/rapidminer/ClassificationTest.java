/*
 * Copyright (c) 2013 University of Tartu
 */
package org.jpmml.rapidminer;

import org.jpmml.evaluator.*;

import org.junit.*;

import static org.junit.Assert.*;

public class ClassificationTest {

	@Test
	public void evaluateDecisionTreeIris() throws Exception {
		Batch batch = new RapidMinerBatch("DecisionTree", "Iris");

		assertTrue(BatchUtil.evaluate(batch));
	}

	@Test
	public void evaluateNeuralNetworkIris() throws Exception {
		Batch batch = new RapidMinerBatch("NeuralNetwork", "Iris");

		assertTrue(BatchUtil.evaluate(batch));
	}

	@Test
	public void evaluateRuleSetIris() throws Exception {
		Batch batch = new RapidMinerBatch("RuleSet", "Iris");

		assertTrue(BatchUtil.evaluate(batch));
	}

	@Test
	public void evaluateDecisionTreeAudit() throws Exception {
		Batch batch = new RapidMinerBatch("DecisionTree", "Audit");

		assertTrue(BatchUtil.evaluate(batch));
	}

	@Test
	public void evaluateRuleSetAudit() throws Exception {
		Batch batch = new RapidMinerBatch("RuleSet", "Audit");

		assertTrue(BatchUtil.evaluate(batch));
	}
}