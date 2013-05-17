/*
 * Copyright (c) 2013 University of Tartu
 */
package org.jpmml.evaluator;

import java.util.*;

import org.junit.*;

import static org.junit.Assert.*;

public class FunctionUtilTest {

	@Test
	public void evaluateArithmeticFunctions(){
		Double one = Double.valueOf(1d);
		Double three = Double.valueOf(3d);

		assertEquals(Double.valueOf(1d + 3d), evaluate("+", one, three));
		assertEquals(Double.valueOf(1d - 3d), evaluate("-", one, three));
		assertEquals(Double.valueOf(1d * 3d), evaluate("*", one, three));
		assertEquals(Double.valueOf(1d / 3d), evaluate("/", one, three));

		assertEquals(null, evaluate("+", one, null));
		assertEquals(null, evaluate("+", null, three));
	}

	@Test
	public void evaluateMathFunctions(){
		assertEquals(Integer.valueOf(1), evaluate("abs", Integer.valueOf(-1)));
		assertEquals(Float.valueOf(1), evaluate("abs", Float.valueOf(-1f)));
		assertEquals(Double.valueOf(1), evaluate("abs", Double.valueOf(-1)));

		assertEquals(0, evaluate("threshold", 2, 3));
		assertEquals(0, evaluate("threshold", 3, 3));
		assertEquals(1, evaluate("threshold", 3, 2));

		assertEquals(1, evaluate("floor", 1.99d));
		assertEquals(2, evaluate("round", 1.99d));

		assertEquals(1, evaluate("ceil", 0.01d));
		assertEquals(0, evaluate("round", 0.01d));
	}

	@Test
	public void evaluateValueFunctions(){
		assertEquals(Boolean.TRUE, evaluate("isMissing", (String)null));
		assertEquals(Boolean.FALSE, evaluate("isMissing", "value"));

		assertEquals(Boolean.TRUE, evaluate("isNotMissing", "value"));
		assertEquals(Boolean.FALSE, evaluate("isNotMissing", (String)null));
	}

	@Test
	public void evaluateComparisonFunctions(){
		Double one = Double.valueOf(1d);
		Double three = Double.valueOf(3d);

		assertEquals(Boolean.TRUE, evaluate("equal", one, one));
		assertEquals(Boolean.TRUE, evaluate("notEqual", one, three));

		assertEquals(Boolean.TRUE, evaluate("lessThan", one, three));
		assertEquals(Boolean.TRUE, evaluate("lessOrEqual", one, one));

		assertEquals(Boolean.TRUE, evaluate("greaterThan", three, one));
		assertEquals(Boolean.TRUE, evaluate("greaterOrEqual", three, three));
	}

	@Test
	public void evaluateBinaryFunctions(){
		assertEquals(Boolean.TRUE, evaluate("and", Boolean.TRUE, Boolean.TRUE));
		assertEquals(Boolean.TRUE, evaluate("and", Boolean.TRUE, Boolean.TRUE, Boolean.TRUE));

		assertEquals(Boolean.FALSE, evaluate("and", Boolean.TRUE, Boolean.FALSE));
		assertEquals(Boolean.FALSE, evaluate("and", Boolean.FALSE, Boolean.TRUE));

		assertEquals(Boolean.TRUE, evaluate("or", Boolean.FALSE, Boolean.TRUE));
		assertEquals(Boolean.TRUE, evaluate("or", Boolean.FALSE, Boolean.FALSE, Boolean.TRUE));

		assertEquals(Boolean.FALSE, evaluate("or", Boolean.FALSE, Boolean.FALSE));
	}

	@Test
	public void evaluateUnaryFunction(){
		assertEquals(Boolean.TRUE, evaluate("not", Boolean.FALSE));
		assertEquals(Boolean.FALSE, evaluate("not", Boolean.TRUE));
	}

	@Test
	public void evaluateValueListFunctions(){
		assertEquals(Boolean.TRUE, evaluate("isIn", "3", "1", "2", "3"));
		assertEquals(Boolean.TRUE, evaluate("isNotIn", "0", "1", "2", "3"));

		assertEquals(Boolean.TRUE, evaluate("isIn", 3, 1, 2, 3));
		assertEquals(Boolean.TRUE, evaluate("isNotIn", 0, 1, 2, 3));

		assertEquals(Boolean.TRUE, evaluate("isIn", 3d, 1d, 2d, 3d));
		assertEquals(Boolean.TRUE, evaluate("isNotIn", 0d, 1d, 2d, 3d));
	}

	@Test
	public void evaluateIfFunction(){
		assertEquals("left", evaluate("if", Boolean.TRUE, "left"));
		assertEquals("left", evaluate("if", Boolean.TRUE, "left", "right"));

		assertEquals(null, evaluate("if", Boolean.FALSE, "left"));
		assertEquals("right", evaluate("if", Boolean.FALSE, "left", "right"));
	}

	static
	private Object evaluate(String name, Object... values){
		return FunctionUtil.evaluate(name, Arrays.asList(values));
	}
}