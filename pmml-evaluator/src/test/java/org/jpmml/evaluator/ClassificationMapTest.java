/*
 * Copyright, KNIME.com AG, Zurich, Switzerland
 */
package org.jpmml.evaluator;

import org.junit.*;

import static org.junit.Assert.*;

public class ClassificationMapTest {

	@Test
	public void increasingOrder(){
		ClassificationMap.Type type = ClassificationMap.Type.SIMILARITY;

		assertTrue(type.compare(0.5, 0.0) > 0);
		assertTrue(type.compare(0.0, 0.5) < 0);

		assertTrue(type.compare(0.5, 0.5) == 0);

		assertTrue(type.compare(1.0, 0.5) > 0);
		assertTrue(type.compare(0.5, 1.0) < 0);
	}

	@Test
	public void decreasingOrder(){
		ClassificationMap.Type type = ClassificationMap.Type.DISTANCE;

		assertTrue(type.compare(0.5, 0.0) < 0);
		assertTrue(type.compare(0.0, 0.5) > 0);

		assertTrue(type.compare(0.5, 0.5) == 0);

		assertTrue(type.compare(1.0, 0.5) < 0);
		assertTrue(type.compare(0.5, 1.0) > 0);
	}
}