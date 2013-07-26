/*
 * Copyright (c) 2013 University of Tartu
 */
package org.jpmml.evaluator;

import java.io.*;

import org.dmg.pmml.*;

abstract
public class PMMLTest {

	static
	public PMML loadPMML(Class<? extends PMMLTest> clazz) throws Exception {
		InputStream is = clazz.getResourceAsStream("/pmml/" + clazz.getSimpleName() + ".pmml");

		try {
			return IOUtil.unmarshal(is);
		} finally {
			is.close();
		}
	}

	static
	public String getEntityId(Object object){

		if(object instanceof HasEntityId){
			HasEntityId hasEntityId = (HasEntityId)object;

			return hasEntityId.getEntityId();
		}

		return null;
	}
}