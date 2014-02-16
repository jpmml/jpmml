/*
 * Copyright (c) 2013 Villu Ruusmann
 */
package org.jpmml.evaluator;

import org.dmg.pmml.*;

import org.junit.*;

import static org.junit.Assert.*;

public class TargetUtilTest {

	@Test
	public void process(){
		Target target = new Target(new FieldName("amount"));
		target.setRescaleFactor(3.14);
		target.setRescaleConstant(10d);

		assertTrue(VerificationUtil.acceptable(35.12d, TargetUtil.process(target, 8d)));

		target.setMin(-10d);
		target.setMax(10.5d);
		target.setCastInteger(Target.CastInteger.ROUND);

		assertEquals(35, TargetUtil.process(target, 8d));
		assertEquals(43, TargetUtil.process(target, 12.97d));
	}
}