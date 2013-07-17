/*
 * Copyright (c) 2013 University of Tartu
 */
package org.jpmml.rattle;

import org.jpmml.evaluator.*;

import org.junit.*;

import static org.junit.Assert.*;

public class AssociationRulesTest {

	@Test
	public void evaluateAssociationRulesShopping() throws Exception {
		Batch batch = new RattleBatch("AssociationRules", "Shopping");

		assertTrue(BatchUtil.evaluate(batch));
	}
}