/*
 * Copyright (c) 2013 Villu Ruusmann
 */
package org.jpmml.evaluator;

import java.util.*;

import org.dmg.pmml.*;

abstract
public class SegmentationTest extends MiningModelEvaluatorTest {

	abstract
	public Map<FieldName, ?> evaluateExample(double petalLength, double petalWidth) throws Exception;
}