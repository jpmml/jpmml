/*
 * Copyright (c) 2013 University of Tartu
 */
package org.jpmml.evaluator;

public interface Computable {

	/**
	 * @throws EvaluationException If the computation fails.
	 */
	Object getResult();
}