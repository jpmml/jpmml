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
		assertEquals(4d, evaluate("+", 1d, 3d));
		assertEquals(-2d, evaluate("-", 1d, 3d));
		assertEquals(3d, evaluate("*", 1d, 3d));
		assertEquals((1d / 3d), evaluate("/", 1d, 3d));

		assertEquals(null, evaluate("+", 1d, null));
		assertEquals(null, evaluate("+", null, 1d));
	}

	@Test
	public void evaluateAggregateFunctions(){
		List<Double> values = Arrays.asList(2.5d, 5d, 1.5d);

		assertEquals(1.5d, evaluate("min", values));
		assertEquals(5d, evaluate("max", values));

		assertEquals(3d, evaluate("avg", values));

		assertEquals(9d, evaluate("sum", values));
		assertEquals(18.75d, evaluate("product", values));
	}

	@Test
	public void evaluateMathFunctions(){
		assertEquals(Integer.valueOf(1), evaluate("abs", Integer.valueOf(-1)));
		assertEquals(Float.valueOf(1f), evaluate("abs", Float.valueOf(-1f)));
		assertEquals(Double.valueOf(1d), evaluate("abs", Double.valueOf(-1)));

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
		assertEquals(Boolean.TRUE, evaluate("equal", 1d, 1d));
		assertEquals(Boolean.TRUE, evaluate("notEqual", 1d, 3d));

		assertEquals(Boolean.TRUE, evaluate("lessThan", 1d, 3d));
		assertEquals(Boolean.TRUE, evaluate("lessOrEqual", 1d, 1d));

		assertEquals(Boolean.TRUE, evaluate("greaterThan", 3d, 1d));
		assertEquals(Boolean.TRUE, evaluate("greaterOrEqual", 3d, 3d));
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

	@Test
	public void evaluateStringFunctions(){
		assertEquals("VALUE", evaluate("uppercase", "Value"));
		assertEquals("value", evaluate("lowercase", "Value"));

		assertEquals("", evaluate("substring", "value", 1, 0));
		assertEquals("value", evaluate("substring", "value", 1, 5));

		assertEquals("alue", evaluate("substring", "value", 2, 4));
		assertEquals("valu", evaluate("substring", "value", 1, 4));

		assertEquals("value", evaluate("trimBlanks", "\tvalue\t"));
	}

	static
	private Object evaluate(String name, Object... values){
		return evaluate(name, Arrays.asList(values));
	}

	static
	private Object evaluate(String name, List<?> values){
		return FunctionUtil.evaluate(name, values);
	}
}