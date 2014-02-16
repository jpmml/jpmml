/*
 * Copyright (c) 2013 Villu Ruusmann
 */
package org.jpmml.knime;

import org.jpmml.evaluator.*;

import org.junit.*;

import static org.junit.Assert.*;

public class ClusteringTest {

	@Test
	public void evaluateKmeansIris() throws Exception {
		Batch batch = new KnimeBatch("KMeans", "Iris");

		assertTrue(BatchUtil.evaluate(batch));
	}
}