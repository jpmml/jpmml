/*
 * Copyright (c) 2013 University of Tartu
 */
package org.jpmml.rattle;

import org.jpmml.evaluator.*;

import org.junit.*;

import static org.junit.Assert.*;

public class RegressionTest {

	@Test
	public void evaluateNeuralNetwork() throws Exception {
		Batch batch = new RattleBatch("NeuralNetwork", "Ozone");

		assertTrue(BatchUtil.evaluate(batch));
	}

	@Test
	public void evaluateRegression() throws Exception {
		Batch batch = new RattleBatch("Regression", "Ozone");

		assertTrue(BatchUtil.evaluate(batch));
	}
}