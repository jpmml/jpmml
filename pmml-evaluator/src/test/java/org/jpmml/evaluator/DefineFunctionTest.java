/*
 * Copyright (c) 2013 University of Tartu
 */
package org.jpmml.evaluator;

import java.util.*;

import org.jpmml.manager.*;

import org.dmg.pmml.*;

import org.junit.*;

import static org.junit.Assert.*;

public class DefineFunctionTest extends PMMLManagerTest {

	@Test
	public void evaluateAmPm() throws Exception {
		PMMLManager pmmlManager = createManager();

		PMMLManagerEvaluationContext context = new PMMLManagerEvaluationContext(pmmlManager);

		assertEquals("AM", evaluateAmPm(34742, context));

		Map<FieldName, ?> arguments = Collections.singletonMap(new FieldName("StartTime"), 34742);

		assertEquals("AM", evaluateField(new FieldName("Shift"), arguments, context));
	}

	@Test
	public void evaluateStategroup() throws Exception {
		PMMLManager pmmlManager = createManager();

		PMMLManagerEvaluationContext context = new PMMLManagerEvaluationContext(pmmlManager);

		assertEquals("West", evaluateStategroup("CA", context));
		assertEquals("West", evaluateStategroup("OR", context));
		assertEquals("East", evaluateStategroup("NC", context));

		Map<FieldName, ?> arguments = Collections.singletonMap(new FieldName("State"), "CA");

		assertEquals("West", evaluateField(new FieldName("Group"), arguments, context));
	}

	static
	private Object evaluateAmPm(Integer time, EvaluationContext context){
		return evaluateFunction("AMPM", Collections.singletonList(time), context);
	}

	static
	private Object evaluateStategroup(String state, EvaluationContext context){
		return evaluateFunction("STATEGROUP", Collections.singletonList(state), context);
	}

	static
	private Object evaluateField(FieldName name, Map<FieldName, ?> arguments, EvaluationContext context){
		context.pushFrame(arguments);

		try {
			return ExpressionUtil.evaluate(name, context);
		} finally {
			context.popFrame();
		}
	}

	static
	private Object evaluateFunction(String function, List<?> values, EvaluationContext context){
		Apply apply = new Apply(function);

		return FunctionUtil.evaluate(apply, values, context);
	}
}