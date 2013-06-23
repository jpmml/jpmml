/*
 * Copyright (c) 2013 University of Tartu
 */
package org.jpmml.evaluator;

import java.io.*;

import org.dmg.pmml.*;

abstract
public class TreeModelEvaluatorTest {

	static
	public PMML loadPMML(Class<? extends TreeModelEvaluatorTest> clazz) throws Exception {
		InputStream is = clazz.getResourceAsStream("/pmml/" + clazz.getSimpleName() + ".pmml");

		try {
			return IOUtil.unmarshal(is);
		} finally {
			is.close();
		}
	}
}