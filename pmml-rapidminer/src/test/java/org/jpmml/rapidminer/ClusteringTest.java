/*
 * Copyright (c) 2013 University of Tartu
 */
package org.jpmml.rapidminer;

import org.jpmml.evaluator.*;

import org.junit.*;

import static org.junit.Assert.*;

public class ClusteringTest {

	@Test
	public void evaluateKMeansIris() throws Exception {
		Batch batch = new RapidMinerBatch("KMeans", "Iris");

		assertTrue(BatchUtil.evaluate(batch));
	}
}