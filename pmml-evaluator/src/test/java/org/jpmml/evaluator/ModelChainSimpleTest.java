/*
 * Copyright (c) 2013 Villu Ruusmann
 */
package org.jpmml.evaluator;

import java.util.*;

import org.dmg.pmml.*;

import org.junit.*;

import static org.junit.Assert.*;

public class ModelChainSimpleTest extends ModelChainTest {

	@Test
	public void evaluate() throws Exception {
		Map<FieldName, ?> result = evaluateExample(1.4, 0.2);

		assertNotNull(result.get(new FieldName("PollenIndex")));
	}
}