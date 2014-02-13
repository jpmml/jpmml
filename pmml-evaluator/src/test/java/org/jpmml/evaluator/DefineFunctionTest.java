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

		PMMLEvaluationContext context = new PMMLEvaluationContext(pmmlManager);

		assertValueEquals("AM", evaluateAmPm(34742, context));

		Map<FieldName, ?> arguments = createArguments("StartTime", 34742);

		assertValueEquals("AM", evaluateField(new FieldName("Shift"), arguments, context));
	}

	@Test
	public void evaluateStategroup() throws Exception {
		PMMLManager pmmlManager = createManager();

		PMMLEvaluationContext context = new PMMLEvaluationContext(pmmlManager);

		assertValueEquals("West", evaluateStategroup("CA", context));
		assertValueEquals("West", evaluateStategroup("OR", context));
		assertValueEquals("East", evaluateStategroup("NC", context));

		Map<FieldName, ?> arguments = createArguments("State", "CA");

		assertValueEquals("West", evaluateField(new FieldName("Group"), arguments, context));
	}

	static
	private void assertValueEquals(Object expected, FieldValue actual){
		assertEquals(expected, FieldValueUtil.getValue(actual));
	}

	static
	private FieldValue evaluateAmPm(Integer time, EvaluationContext context){
		List<FieldValue> values = Collections.singletonList(FieldValueUtil.create(time));

		return evaluateFunction("AMPM", values, context);
	}

	static
	private FieldValue evaluateStategroup(String state, EvaluationContext context){
		List<FieldValue> values = Collections.singletonList(FieldValueUtil.create(state));

		return evaluateFunction("STATEGROUP", values, context);
	}

	static
	private FieldValue evaluateField(FieldName name, Map<FieldName, ?> arguments, EvaluationContext context){
		context.pushFrame(arguments);

		try {
			return ExpressionUtil.evaluate(name, context);
		} finally {
			context.popFrame();
		}
	}

	static
	private FieldValue evaluateFunction(String function, List<FieldValue> values, EvaluationContext context){
		Apply apply = new Apply(function);

		return FunctionUtil.evaluate(apply, values, context);
	}
}