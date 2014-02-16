/*
 * Copyright (c) 2013 Villu Ruusmann
 */
package org.jpmml.evaluator;

import java.io.*;

public interface Batch {

	/**
	 * Model's description in PMML data format
	 */
	InputStream getModel();

	/**
	 * Model input in CSV data format.
	 *
	 * @see Evaluator#getActiveFields()
	 */
	InputStream getInput();

	/**
	 * Model output in CSV data format.
	 *
	 * @see Evaluator#getPredictedFields()
	 * @see Evaluator#getOutputFields()
	 */
	InputStream getOutput();
}