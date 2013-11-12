/*
 * Copyright (c) 2013 University of Tartu
 */
package org.jpmml.evaluator;

import java.util.*;

import org.dmg.pmml.*;

import org.junit.*;

import com.google.common.base.*;
import com.google.common.collect.*;

import org.joda.time.*;

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

		assertEquals(DataType.INTEGER, TypeUtil.getDataType(evaluate("*", 1, 1)));
		assertEquals(DataType.FLOAT, TypeUtil.getDataType(evaluate("*", 1f, 1f)));
		assertEquals(DataType.DOUBLE, TypeUtil.getDataType(evaluate("*", 1d, 1d)));

		assertEquals(DataType.INTEGER, TypeUtil.getDataType(evaluate("/", 1, 1)));

		assertEquals(DataType.FLOAT, TypeUtil.getDataType(evaluate("/", 1, 1f)));
		assertEquals(DataType.FLOAT, TypeUtil.getDataType(evaluate("/", 1f, 1f)));

		assertEquals(DataType.DOUBLE, TypeUtil.getDataType(evaluate("/", 1, 1d)));
		assertEquals(DataType.DOUBLE, TypeUtil.getDataType(evaluate("/", 1f, 1d)));
		assertEquals(DataType.DOUBLE, TypeUtil.getDataType(evaluate("/", 1d, 1d)));
	}

	@Test
	public void evaluateInvalidArithmenticFunctions(){

		try {
			evaluate("/", 1, 0);

			Assert.fail();
		} catch(InvalidResultException ire){
			// Ignored
		}

		try {
			evaluate("/", 1f, 0);
			evaluate("/", 1d, 0);
		} catch(InvalidResultException ire){
			Assert.fail();
		}
	}

	@Test
	public void evaluateAggregateFunctions(){
		List<Integer> values = Arrays.asList(1, 2, 3);

		Object min = evaluate("min", values);
		assertEquals(1, min);
		assertEquals(DataType.INTEGER, TypeUtil.getDataType(min));

		Object max = evaluate("max", values);
		assertEquals(3, max);
		assertEquals(DataType.INTEGER, TypeUtil.getDataType(max));

		Object average = evaluate("avg", values);
		assertEquals(2d, average);
		assertEquals(DataType.DOUBLE, TypeUtil.getDataType(average));

		Object sum = evaluate("sum", values);
		assertEquals(6, sum);
		assertEquals(DataType.INTEGER, TypeUtil.getDataType(sum));

		Object product = evaluate("product", values);
		assertEquals(6, product);
		assertEquals(DataType.INTEGER, TypeUtil.getDataType(product));
	}

	@Test
	public void evaluateMathFunctions(){
		assertEquals(DataType.DOUBLE, TypeUtil.getDataType(evaluate("log10", 1)));
		assertEquals(DataType.FLOAT, TypeUtil.getDataType(evaluate("log10", 1f)));

		assertEquals(DataType.DOUBLE, TypeUtil.getDataType(evaluate("ln", 1)));
		assertEquals(DataType.FLOAT, TypeUtil.getDataType(evaluate("ln", 1f)));

		assertEquals(DataType.DOUBLE, TypeUtil.getDataType(evaluate("exp", 1)));
		assertEquals(DataType.FLOAT, TypeUtil.getDataType(evaluate("exp", 1f)));

		assertEquals(DataType.DOUBLE, TypeUtil.getDataType(evaluate("sqrt", 1)));
		assertEquals(DataType.FLOAT, TypeUtil.getDataType(evaluate("sqrt", 1f)));

		assertEquals(1, evaluate("abs", -1));
		assertEquals(1f, evaluate("abs", -1f));
		assertEquals(1d, evaluate("abs", -1d));

		assertEquals(DataType.INTEGER, TypeUtil.getDataType(evaluate("pow", 1, 1)));
		assertEquals(DataType.FLOAT, TypeUtil.getDataType(evaluate("pow", 1f, 1f)));

		assertEquals(0, evaluate("threshold", 2, 3));
		assertEquals(0, evaluate("threshold", 3, 3));
		assertEquals(1, evaluate("threshold", 3, 2));

		assertEquals(0f, evaluate("threshold", 2f, 3f));
		assertEquals(0f, evaluate("threshold", 3f, 3f));
		assertEquals(1f, evaluate("threshold", 3f, 2f));

		assertEquals(1, evaluate("floor", 1));
		assertEquals(1, evaluate("ceil", 1));

		assertEquals(1f, evaluate("floor", 1.99f));
		assertEquals(2f, evaluate("round", 1.99f));

		assertEquals(1f, evaluate("ceil", 0.01f));
		assertEquals(0f, evaluate("round", 0.01f));
	}

	@Test
	public void evaluateValueFunctions(){
		assertEquals(Boolean.TRUE, evaluate("isMissing", (String)null));
		assertEquals(Boolean.FALSE, evaluate("isMissing", "value"));

		assertEquals(Boolean.TRUE, evaluate("isNotMissing", "value"));
		assertEquals(Boolean.FALSE, evaluate("isNotMissing", (String)null));
	}

	@Test
	public void evaluateEqualityFunctions(){
		assertEquals(Boolean.TRUE, evaluate("equal", 1, 1d));
		assertEquals(Boolean.TRUE, evaluate("equal", 1d, 1d));

		assertEquals(Boolean.TRUE, evaluate("notEqual", 1d, 3d));
		assertEquals(Boolean.TRUE, evaluate("notEqual", 1, 3));
	}

	@Test
	public void evaluateComparisonFunctions(){
		assertEquals(Boolean.TRUE, evaluate("lessThan", 1d, 3d));
		assertEquals(Boolean.TRUE, evaluate("lessThan", 1, 3d));

		assertEquals(Boolean.TRUE, evaluate("lessOrEqual", 1d, 1d));
		assertEquals(Boolean.TRUE, evaluate("lessOrEqual", 1, 1d));

		assertEquals(Boolean.TRUE, evaluate("greaterThan", 3d, 1d));
		assertEquals(Boolean.TRUE, evaluate("greaterThan", 3, 1d));

		assertEquals(Boolean.TRUE, evaluate("greaterOrEqual", 3d, 3d));
		assertEquals(Boolean.TRUE, evaluate("greaterOrEqual", 3, 3d));
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

	@Test
	public void evaluateFormatFunctions(){
		assertEquals("  2", evaluate("formatNumber", 2, "%3d"));

		assertEquals("08/20/04", evaluate("formatDatetime", new LocalDate(2004, 8, 20), "%m/%d/%y"));
	}

	@Test
	public void evaluateDateTimeFunctions(){
		assertEquals(15796, evaluate("dateDaysSinceYear", new LocalDate(2003, 4, 1), 1960));
		assertEquals(15796, evaluate("dateDaysSinceYear", new LocalDateTime(2003, 4, 1, 0, 0, 0), 1960));

		assertEquals(19410, evaluate("dateSecondsSinceMidnight", new LocalTime(5, 23, 30)));
		assertEquals(19410, evaluate("dateSecondsSinceMidnight", new LocalDateTime(1960, 1, 1, 5, 23, 30)));

		assertEquals(185403, evaluate("dateSecondsSinceYear", new LocalDateTime(1960, 1, 3, 3, 30, 3), 1960));
	}

	static
	private Object evaluate(String function, Object... values){
		return evaluate(function, Arrays.asList(values));
	}

	static
	private Object evaluate(String function, List<?> values){
		Function<Object, FieldValue> transformer = new Function<Object, FieldValue>(){

			@Override
			public FieldValue apply(Object object){
				return FieldValueUtil.create(object);
			}
		};

		FieldValue result = apply(function, Lists.newArrayList(Iterables.transform(values, transformer)), new LocalEvaluationContext(Collections.<FieldName, Object>emptyMap()));

		return FieldValueUtil.getValue(result);
	}

	static
	private FieldValue apply(String function, List<FieldValue> values, EvaluationContext context){
		Apply apply = new Apply(function);

		return FunctionUtil.evaluate(apply, values, context);
	}
}