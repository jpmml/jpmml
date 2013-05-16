/*
 * Copyright (c) 2013 University of Tartu
 */
package org.jpmml.knime;

import org.jpmml.evaluator.*;

import org.junit.*;

import static org.junit.Assert.*;

public class RegressionTest {

	@Test
	public void evaluateNeuralNetwork() throws Exception {
		Batch batch = new KnimeBatch("NeuralNetwork", "Ozone");

		assertTrue(BatchUtil.evaluate(batch));
	}

	@Test
	public void evaluateRegression() throws Exception {
		Batch batch = new KnimeBatch("Regression", "Ozone");

		assertTrue(BatchUtil.evaluate(batch));
	}
}