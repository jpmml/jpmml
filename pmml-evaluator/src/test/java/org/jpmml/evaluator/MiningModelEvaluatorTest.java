/*
 * Copyright (c) 2013 University of Tartu
 */
package org.jpmml.evaluator;

import java.util.*;

import org.dmg.pmml.*;

import com.google.common.collect.*;

abstract
public class MiningModelEvaluatorTest extends PMMLTest {

	public MiningModelEvaluator createEvaluator() throws Exception {
		PMML pmml = loadPMML(getClass());

		MiningModelEvaluator evaluator = new MiningModelEvaluator(pmml);

		return evaluator;
	}

	Map<FieldName, ?> evaluate(double petalLength, double petalWidth) throws Exception {
		MiningModelEvaluator evaluator = createEvaluator();

		Map<FieldName, Object> arguments = Maps.newLinkedHashMap();

		arguments.put(new FieldName("petal_length"), petalLength);
		arguments.put(new FieldName("petal_width"), petalWidth);
		arguments.put(new FieldName("temperature"), 0d);
		arguments.put(new FieldName("cloudiness"), 0d);

		return evaluator.evaluate(arguments);
	}
}