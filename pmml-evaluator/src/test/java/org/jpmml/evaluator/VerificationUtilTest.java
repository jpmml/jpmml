/*
 * Copyright (c) 2013 University of Tartu
 */
package org.jpmml.evaluator;

import org.junit.*;

import static org.junit.Assert.*;

public class VerificationUtilTest {

	@Test
	public void acceptable(){
		double precision = 0.001;

		// Avoid hitting exact boundary values
		double epsilon = (precision * precision);

		assertTrue(VerificationUtil.acceptable(1.0, 1.0, precision));

		assertTrue(VerificationUtil.acceptable(1.0, 0.999 + epsilon, precision));
		assertFalse(VerificationUtil.acceptable(1.0, 0.99895, precision));

		assertTrue(VerificationUtil.acceptable(1.0, 1.001 - epsilon, precision));
		assertFalse(VerificationUtil.acceptable(1.0, 1.00105, precision));

		assertTrue(VerificationUtil.acceptable(-1.0, -1.0, precision));

		assertTrue(VerificationUtil.acceptable(-1.0, -1.001 + epsilon, precision));
		assertFalse(VerificationUtil.acceptable(-1.0, -1.00105, precision));

		assertTrue(VerificationUtil.acceptable(-1.0, -0.999 - epsilon, precision));
		assertFalse(VerificationUtil.acceptable(-1.0, -0.99895, precision));
	}
}