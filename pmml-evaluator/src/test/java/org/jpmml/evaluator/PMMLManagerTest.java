/*
 * Copyright (c) 2013 University of Tartu
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