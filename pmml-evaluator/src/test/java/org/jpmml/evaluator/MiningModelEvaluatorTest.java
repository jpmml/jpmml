/*
 * Copyright (c) 2013 Villu Ruusmann
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
}