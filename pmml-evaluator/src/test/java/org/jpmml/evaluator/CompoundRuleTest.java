/*
 * Copyright (c) 2013 University of Tartu
 */
package org.jpmml.evaluator;

import org.dmg.pmml.*;

import org.junit.*;

import static org.junit.Assert.*;

public class CompoundRuleTest extends RuleSetModelEvaluatorTest {

	@Test
	public void evaluate() throws Exception {
		assertEquals("RULE1", getRuleId(RuleSelectionMethod.Criterion.FIRST_HIT));
		assertEquals("RULE2", getRuleId(RuleSelectionMethod.Criterion.WEIGHTED_SUM));
		assertEquals("RULE1", getRuleId(RuleSelectionMethod.Criterion.WEIGHTED_MAX));
	}
}