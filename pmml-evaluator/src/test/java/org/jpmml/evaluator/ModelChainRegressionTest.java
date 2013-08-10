/*
 * Copyright (c) 2013 University of Tartu
 */
package org.jpmml.evaluator;

import java.util.*;

import org.dmg.pmml.*;

import org.junit.*;

import com.google.common.collect.*;

import static org.junit.Assert.*;

public class ModelChainRegressionTest extends MiningModelEvaluatorTest {

	@Test
	public void evaluateSetosa() throws Exception {
		Map<FieldName, ?> result = evaluate(1.4, 0.2);

		assertEquals(0.3, result.get(new FieldName("Setosa Pollen Index")));
	}

	@Test
	public void evaluateVersicolor() throws Exception {
		Map<FieldName, ?> result = evaluate(4.7, 1.4);

		assertEquals(0.2, result.get(new FieldName("Versicolor Pollen Index")));
	}

	@Test
	public void evaluateVirginica() throws Exception {
		Map<FieldName, ?> result = evaluate(6, 2.5);

		assertEquals(0.1, result.get(new FieldName("Virginica Pollen Index")));
	}

	private Map<FieldName, ?> evaluate(double petalLength, double petalWidth) throws Exception {
		MiningModelEvaluator evaluator = createEvaluator();

		Map<FieldName, Object> arguments = Maps.newLinkedHashMap();

		arguments.put(new FieldName("petal_length"), petalLength);
		arguments.put(new FieldName("petal_width"), petalWidth);
		arguments.put(new FieldName("temperature"), 0d);
		arguments.put(new FieldName("cloudiness"), 0d);

		return evaluator.evaluate(arguments);
	}
}