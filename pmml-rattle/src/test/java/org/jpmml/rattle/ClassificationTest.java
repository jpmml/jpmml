/*
 * Copyright (c) 2013 Villu Ruusmann
 */
package org.jpmml.rattle;

import org.jpmml.evaluator.*;

import org.junit.*;

import static org.junit.Assert.*;

public class ClassificationTest {

	@Test
	public void evaluateDecisionTreeIris() throws Exception {
		Batch batch = new RattleBatch("DecisionTree", "Iris");

		assertTrue(BatchUtil.evaluate(batch));
	}

	@Test
	public void evaluateGeneralRegressionIris() throws Exception {
		Batch batch = new RattleBatch("GeneralRegression", "Iris");

		assertTrue(BatchUtil.evaluate(batch));
	}

	@Test
	public void evaluateNaiveBayesIris() throws Exception {
		Batch batch = new RattleBatch("NaiveBayes", "Iris");

		assertTrue(BatchUtil.evaluate(batch));
	}

	@Test
	public void evaluateNeuralNetworkIris() throws Exception {
		Batch batch = new RattleBatch("NeuralNetwork", "Iris");

		assertTrue(BatchUtil.evaluate(batch));
	}

	@Test
	public void evaluateRandomForestIris() throws Exception {
		Batch batch = new RattleBatch("RandomForest", "Iris");

		assertTrue(BatchUtil.evaluate(batch));
	}

	@Test
	public void evaluateRegressionIris() throws Exception {
		Batch batch = new RattleBatch("Regression", "Iris");

		assertTrue(BatchUtil.evaluate(batch));
	}

	@Test
	public void evaluateSupportVectorMachineIris() throws Exception {
		Batch batch = new RattleBatch("SupportVectorMachine", "Iris");

		assertTrue(BatchUtil.evaluate(batch));
	}

	@Test
	public void evaluateDecisionTreeAudit() throws Exception {
		Batch batch = new RattleBatch("DecisionTree", "Audit");

		assertTrue(BatchUtil.evaluate(batch));
	}

	@Test
	public void evaluateGeneralRegressionAudit() throws Exception {
		Batch batch = new RattleBatch("GeneralRegression", "Audit");

		assertTrue(BatchUtil.evaluate(batch));
	}

	@Test
	public void evaluateNaiveBayesAudit() throws Exception {
		Batch batch = new RattleBatch("NaiveBayes", "Audit");

		assertTrue(BatchUtil.evaluate(batch));
	}

	@Test
	public void evaluateNeuralNetworkAudit() throws Exception {
		Batch batch = new RattleBatch("NeuralNetwork", "Audit");

		assertTrue(BatchUtil.evaluate(batch));
	}

	@Test
	public void evaluateRandomForestAudit() throws Exception {
		Batch batch = new RattleBatch("RandomForest", "Audit");

		assertTrue(BatchUtil.evaluate(batch));
	}

	@Test
	public void evaluateSupportVectorMachineAudit() throws Exception {
		Batch batch = new RattleBatch("SupportVectorMachine", "Audit");

		assertTrue(BatchUtil.evaluate(batch));
	}
}