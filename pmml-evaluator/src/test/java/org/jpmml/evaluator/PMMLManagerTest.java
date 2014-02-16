/*
 * Copyright (c) 2013 Villu Ruusmann
 */
package org.jpmml.evaluator;

import org.jpmml.manager.*;

import org.dmg.pmml.*;

abstract
public class PMMLManagerTest extends PMMLTest {

	public PMMLManager createManager() throws Exception {
		PMML pmml = loadPMML(getClass());

		PMMLManager manager = new PMMLManager(pmml);

		return manager;
	}
}