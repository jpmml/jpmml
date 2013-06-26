/*
 * Copyright (c) 2013 University of Tartu
 */
package org.jpmml.evaluator;

import java.util.*;

import org.dmg.pmml.*;

import org.junit.*;

import static org.junit.Assert.*;

public class AttributeReasonCodeTest extends ScorecardEvaluatorTest {

	@Test
	public void evaluate() throws Exception {
		Map<FieldName, ?> result = evaluateExample();

		assertEquals(29d, result.get(new FieldName("Final Score")));

		assertEquals("RC2_3", result.get(new FieldName("Reason Code 1")));
		assertEquals("RC1", result.get(new FieldName("Reason Code 2")));
		assertEquals("RC1", result.get(new FieldName("Reason Code 3")));
	}
}