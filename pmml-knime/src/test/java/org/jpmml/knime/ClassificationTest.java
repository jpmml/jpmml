/*
 * Copyright (c) 2013 University of Tartu
 */
package org.jpmml.knime;

import org.jpmml.evaluator.*;

import org.junit.*;

import static org.junit.Assert.*;

public class ClassificationTest {

	@Test
	public void evaluateDecisionTreeIris() throws Exception {
		Batch batch = new KnimeBatch("DecisionTree", "Iris");

		assertTrue(BatchUtil.evaluate(batch));
	}

	@Test
	public void evaluateGeneralRegressionIris() throws Exception {
		Batch batch = new KnimeBatch("GeneralRegression", "Iris");

		assertTrue(BatchUtil.evaluate(batch));
	}

	@Test
	public void evaluateNeuralNetworkIris() throws Exception {
		Batch batch = new KnimeBatch("NeuralNetwork", "Iris");

		assertTrue(BatchUtil.evaluate(batch));
	}

	@Test
	public void evaluateSupportVectorMachineIris() throws Exception {
		Batch batch = new KnimeBatch("SupportVectorMachine", "Iris");

		assertTrue(BatchUtil.evaluate(batch));
	}

	@Test
	public void evaluateGeneralRegressionAudit() throws Exception {
		Batch batch = new KnimeBatch("GeneralRegression", "Audit");

		assertTrue(BatchUtil.evaluate(batch));
	}
}