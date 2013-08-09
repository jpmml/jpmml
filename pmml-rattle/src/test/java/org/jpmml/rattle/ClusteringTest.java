/*
 * Copyright (c) 2013 University of Tartu
 */
package org.jpmml.rattle;

import org.jpmml.evaluator.*;

import org.junit.*;

import static org.junit.Assert.*;

public class ClusteringTest {

	@Test
	public void evaluateHierarchicalClusteringIris() throws Exception {
		Batch batch = new RattleBatch("HierarchicalClustering", "Iris");

		assertTrue(BatchUtil.evaluate(batch));
	}

	@Test
	public void evaluateKMeansIris() throws Exception {
		Batch batch = new RattleBatch("KMeans", "Iris");

		assertTrue(BatchUtil.evaluate(batch));
	}
}