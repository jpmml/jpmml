/*
 * Copyright (c) 2013 Villu Ruusmann
 */
package org.jpmml.evaluator;

import java.util.*;

import org.dmg.pmml.*;

import org.junit.*;

import static org.junit.Assert.*;

public class ModelChainEfficientCompositionTest extends ModelChainTest {

	@Test
	public void evaluateSetosa() throws Exception {
		Map<FieldName, ?> result = evaluateExample(1.4, 0.2);

		assertEquals(0.3, result.get(new FieldName("Setosa Pollen Index")));
	}

	@Test
	public void evaluateVersicolor() throws Exception {
		Map<FieldName, ?> result = evaluateExample(4.7, 1.4);

		assertEquals(0.2, result.get(new FieldName("Versicolor Pollen Index")));
	}

	@Test
	public void evaluateVirginica() throws Exception {
		Map<FieldName, ?> result = evaluateExample(6, 2.5);

		assertEquals(0.1, result.get(new FieldName("Virginica Pollen Index")));
	}
}