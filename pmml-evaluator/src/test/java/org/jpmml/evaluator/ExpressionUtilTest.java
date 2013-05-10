/*
 * Copyright (c) 2013 University of Tartu
 */
package org.jpmml.evaluator;

import java.util.*;

import org.dmg.pmml.*;

import org.junit.*;

import static org.junit.Assert.*;

public class ExpressionUtilTest {

	@Test
	public void evaluateConstant(){
		Constant stringThree = new Constant("3");
		stringThree.setDataType(DataType.STRING);
		assertEquals("3", ExpressionUtil.getValue(stringThree, null, null));

		Constant integerThree = new Constant("3");
		integerThree.setDataType(DataType.INTEGER);
		assertEquals(Integer.valueOf(3), ExpressionUtil.getValue(integerThree, null, null));

		Constant floatThree = new Constant("3");
		floatThree.setDataType(DataType.FLOAT);
		assertEquals(Float.valueOf(3f), ExpressionUtil.getValue(floatThree, null, null));
	}

	@Test
	public void evaluateFieldRef(){
		FieldName name = new FieldName("x");

		FieldRef nameRef = new FieldRef(name);
		assertEquals("3", ExpressionUtil.getValue(nameRef, null, Collections.singletonMap(name, "3")));
	}

	@Test
	public void evaluateNormContinuous(){
		FieldName name = new FieldName("x");

		NormContinuous expression = new NormContinuous(name);

		expression.setMapMissingTo(5d);

		assertEquals(5d, ExpressionUtil.getValue(expression, null, Collections.singletonMap(name, null)));
	}

	@Test
	public void evaluateNormDiscrete(){
		FieldName name = new FieldName("x");

		Double equals = Double.valueOf(1.0d);
		Double notEquals = Double.valueOf(0.0d);

		NormDiscrete stringThree = new NormDiscrete(name, "3");
		assertEquals(equals, ExpressionUtil.getValue(stringThree, null, Collections.singletonMap(name, "3")));
		assertEquals(notEquals, ExpressionUtil.getValue(stringThree, null, Collections.singletonMap(name, "1")));

		stringThree.setMapMissingTo(5d);

		assertEquals(5d, ExpressionUtil.getValue(stringThree, null, Collections.singletonMap(name, null)));

		NormDiscrete integerThree = new NormDiscrete(name, "3");
		assertEquals(equals, ExpressionUtil.getValue(integerThree, null, Collections.singletonMap(name, Integer.valueOf(3))));
		assertEquals(notEquals, ExpressionUtil.getValue(integerThree, null, Collections.singletonMap(name, Integer.valueOf(1))));

		NormDiscrete floatThree = new NormDiscrete(name, "3.0");
		assertEquals(equals, ExpressionUtil.getValue(floatThree, null, Collections.singletonMap(name, Float.valueOf(3.0f))));
		assertEquals(notEquals, ExpressionUtil.getValue(floatThree, null, Collections.singletonMap(name, Float.valueOf(1.0f))));
	}
}