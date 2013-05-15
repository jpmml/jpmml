/*
 * Copyright (c) 2013 University of Tartu
 */
package org.jpmml.rattle;

import java.io.*;

import org.jpmml.evaluator.*;

public class RattleBatch extends LocalBatch {

	public RattleBatch(String name, String dataset){
		super(name, dataset);
	}

	@Override
	public InputStream open(String path){
		return (RattleBatch.class).getResourceAsStream(path);
	}
}