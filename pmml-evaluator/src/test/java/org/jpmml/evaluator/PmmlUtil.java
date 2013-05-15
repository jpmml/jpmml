/*
 * Copyright (c) 2013 University of Tartu
 */
package org.jpmml.evaluator;

import java.io.*;

import org.jpmml.manager.*;

import org.dmg.pmml.*;

public class PmmlUtil {

	private PmmlUtil(){
	}

	static
	public PMML load(InputStream is) throws Exception {

		try {
			return IOUtil.unmarshal(is);
		} finally {
			is.close();
		}
	}
}