/*
 * Copyright (c) 2013 University of Tartu
 */
package org.jpmml.rapidminer;

import org.jpmml.evaluator.*;

import org.junit.*;

import static org.junit.Assert.*;

public class RegressionTest {

	@Test
	public void evaluateRegressionOzone() throws Exception {
		Batch batch = new RapidMinerBatch("Regression", "Ozone");

		assertTrue(BatchUtil.evaluate(batch));
	}
}