/*
 * Copyright (c) 2013 University of Tartu
 */
package org.jpmml.knime;

import java.io.*;

import org.jpmml.evaluator.*;

public class KnimeBatch extends LocalBatch {

	public KnimeBatch(String name, String dataset){
		super(name, dataset);
	}

	@Override
	public InputStream open(String path){
		return (KnimeBatch.class).getResourceAsStream(path);
	}
}