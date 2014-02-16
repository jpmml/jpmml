/*
 * Copyright (c) 2013 Villu Ruusmann
 */
package org.jpmml.evaluator;

import java.util.*;

import org.dmg.pmml.*;

abstract
public class ModelChainTest extends SegmentationTest {

	@Override
	public Map<FieldName, ?> evaluateExample(double petalLength, double petalWidth) throws Exception {
		MiningModelEvaluator evaluator = createEvaluator();

		Map<FieldName, ?> arguments = createArguments("petal_length", petalLength, "petal_width", petalWidth, "temperature", 0d, "cloudiness", 0d);

		return evaluator.evaluate(arguments);
	}
}