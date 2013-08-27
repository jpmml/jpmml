/*
 * Copyright (c) 2013 University of Tartu
 */
package org.jpmml.evaluator;

import java.io.*;

import org.dmg.pmml.*;

import org.junit.*;

public class SerializationTest extends PMMLTest {

	@Test
	public void nullifyAndClone() throws Exception {
		PMML pmml = loadPMML(ModelChainCompositionTest.class);

		try {
			SerializationUtil.clone(pmml);

			Assert.fail();
		} catch(NotSerializableException nse){
		}

		pmml.accept(new SourceLocationNullifier());

		SerializationUtil.clone(pmml);
	}

	@Test
	public void transformAndClone() throws Exception {
		PMML pmml = loadPMML(ModelChainCompositionTest.class);

		try {
			SerializationUtil.clone(pmml);

			Assert.fail();
		} catch(NotSerializableException nse){
		}

		pmml.accept(new SourceLocationTransformer());

		SerializationUtil.clone(pmml);
	}
}