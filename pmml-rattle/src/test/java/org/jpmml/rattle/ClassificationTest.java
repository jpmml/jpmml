/*
 * Copyright (c) 2013 University of Tartu
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
	public void evaluateNeuralNetworkIris() throws Exception {
		Batch batch = new RattleBatch("NeuralNetwork", "Iris");

		assertTrue(BatchUtil.evaluate(batch));
	}

	@Test
	public void evaluateRegressionIris() throws Exception {
		Batch batch = new RattleBatch("Regression", "Iris");

		assertTrue(BatchUtil.evaluate(batch));
	}
}