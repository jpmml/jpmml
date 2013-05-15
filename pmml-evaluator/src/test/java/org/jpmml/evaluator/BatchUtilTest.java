/*
 * Copyright (c) 2013 University of Tartu
 */
package org.jpmml.evaluator;

import org.junit.*;

import static org.junit.Assert.*;

public class BatchUtilTest {

	@Test
	public void checkEquality(){
		assertTrue(BatchUtil.checkEquality(1, 1f));
		assertTrue(BatchUtil.checkEquality(1, 1.0f));

		assertTrue(BatchUtil.checkEquality(1, 1d));
		assertTrue(BatchUtil.checkEquality(1, 1.0d));

		assertTrue(BatchUtil.checkEquality(1f, 1d));
		assertTrue(BatchUtil.checkEquality(1.0f, 1.0d));
	}
}