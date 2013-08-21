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
		assertEquals("3", ExpressionUtil.evaluate(stringThree, null));

		Constant integerThree = new Constant("3");
		integerThree.setDataType(DataType.INTEGER);
		assertEquals(3, ExpressionUtil.evaluate(integerThree, null));

		Constant floatThree = new Constant("3");
		floatThree.setDataType(DataType.FLOAT);
		assertEquals(3f, ExpressionUtil.evaluate(floatThree, null));
	}

	@Test
	public void evaluateFieldRef(){
		FieldName name = new FieldName("x");

		FieldRef expression = new FieldRef(name);
		assertEquals("3", ExpressionUtil.evaluate(expression, createContext(name, "3")));

		assertEquals(null, ExpressionUtil.evaluate(expression, createContext(name, null)));
		expression.setMapMissingTo("Missing");
		assertEquals("Missing", ExpressionUtil.evaluate(expression, createContext(name, null)));
	}

	@Test
	public void evaluateNormContinuous(){
		FieldName name = new FieldName("x");

		NormContinuous expression = new NormContinuous(name);

		expression.setMapMissingTo(5d);

		assertEquals(5d, ExpressionUtil.evaluate(expression, createContext(name, null)));
	}

	@Test
	public void evaluateNormDiscrete(){
		FieldName name = new FieldName("x");

		Double equals = 1d;
		Double notEquals = 0d;

		NormDiscrete stringThree = new NormDiscrete(name, "3");
		assertEquals(equals, ExpressionUtil.evaluate(stringThree, createContext(name, "3")));
		assertEquals(notEquals, ExpressionUtil.evaluate(stringThree, createContext(name, "1")));

		stringThree.setMapMissingTo(5d);

		assertEquals(5d, ExpressionUtil.evaluate(stringThree, createContext(name, null)));

		NormDiscrete integerThree = new NormDiscrete(name, "3");
		assertEquals(equals, ExpressionUtil.evaluate(integerThree, createContext(name, 3)));
		assertEquals(notEquals, ExpressionUtil.evaluate(integerThree, createContext(name, 1)));

		NormDiscrete floatThree = new NormDiscrete(name, "3.0");
		assertEquals(equals, ExpressionUtil.evaluate(floatThree, createContext(name, 3f)));
		assertEquals(notEquals, ExpressionUtil.evaluate(floatThree, createContext(name, 1f)));
	}

	@Test
	public void evaluateDiscretize(){
		FieldName name = new FieldName("x");

		Discretize expression = new Discretize(name);

		assertEquals(null, ExpressionUtil.evaluate(expression, createContext()));
		expression.setMapMissingTo("Missing");
		assertEquals("Missing", ExpressionUtil.evaluate(expression, createContext()));

		assertEquals(null, ExpressionUtil.evaluate(expression, createContext(name, "3")));
		expression.setDefaultValue("Default");
		assertEquals("Default", ExpressionUtil.evaluate(expression, createContext(name, "3")));
	}

	@Test
	public void evaluateMapValues(){
		FieldName name = new FieldName("x");

		MapValues expression = new MapValues(null);
		(expression.getFieldColumnPairs()).add(new FieldColumnPair(name, null));

		assertEquals(null, ExpressionUtil.evaluate(expression, createContext()));
		expression.setMapMissingTo("Missing");
		assertEquals("Missing", ExpressionUtil.evaluate(expression, createContext()));

		assertEquals(null, ExpressionUtil.evaluate(expression, createContext(name, "3")));
		expression.setDefaultValue("Default");
		assertEquals("Default", ExpressionUtil.evaluate(expression, createContext(name, "3")));
	}

	@Test
	public void evaluateAggregate(){
		FieldName name = new FieldName("x");

		List<?> values = Arrays.asList(ParameterUtil.parse(DataType.DATE, "2013-01-01"), ParameterUtil.parse(DataType.DATE, "2013-02-01"), ParameterUtil.parse(DataType.DATE, "2013-03-01"));

		EvaluationContext context = createContext(name, values);

		Aggregate aggregate = new Aggregate(name, Aggregate.Function.COUNT);
		assertEquals(3, ExpressionUtil.evaluate(aggregate, context));

		aggregate.setFunction(Aggregate.Function.MIN);
		assertEquals(values.get(0), ExpressionUtil.evaluate(aggregate, context));

		aggregate.setFunction(Aggregate.Function.MAX);
		assertEquals(values.get(2), ExpressionUtil.evaluate(aggregate, context));
	}

	static
	private EvaluationContext createContext(){
		EvaluationContext context = new LocalEvaluationContext();

		return context;
	}

	static
	private EvaluationContext createContext(FieldName name, Object value){
		EvaluationContext context = new LocalEvaluationContext();
		context.pushFrame(Collections.<FieldName, Object>singletonMap(name, value));

		return context;
	}
}