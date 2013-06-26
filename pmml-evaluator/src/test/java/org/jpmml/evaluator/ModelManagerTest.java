/*
 * Copyright (c) 2013 University of Tartu
 */
package org.jpmml.evaluator;

import java.io.*;
import java.util.*;

import org.dmg.pmml.*;

abstract
public class ModelManagerTest {

	static
	public PMML loadPMML(Class<? extends ModelManagerTest> clazz) throws Exception {
		InputStream is = clazz.getResourceAsStream("/pmml/" + clazz.getSimpleName() + ".pmml");

		try {
			return IOUtil.unmarshal(is);
		} finally {
			is.close();
		}
	}
}