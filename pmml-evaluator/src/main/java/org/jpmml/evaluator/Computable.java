/*
 * Copyright (c) 2013 Villu Ruusmann
 */
package org.jpmml.evaluator;

public interface Computable {

	/**
	 * @throws EvaluationException If the computation fails.
	 */
	Object getResult();
}