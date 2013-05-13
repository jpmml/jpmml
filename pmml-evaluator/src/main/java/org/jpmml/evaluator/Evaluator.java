/*
 * Copyright (c) 2012 University of Tartu
 */
package org.jpmml.evaluator;

import java.util.*;

import org.jpmml.manager.*;

import org.dmg.pmml.*;

public interface Evaluator extends Consumer {

	/**
	 * @param parameters Map of {@link #getActiveFields() active field values}.
	 *
	 * @return Map of {@link #getPredictedFields() predicted field values}.
	 * Simple values should be represented using the Java equivalents of PMML data types (eg. String, Integer, Float, Double etc.).
	 * Complex values should be represented as instances of {@link Computable} that return simple values.
	 *
	 * @throws EvaluationException If the evaluation failed.
	 *
	 * @see Computable
	 */
	Map<FieldName, ?> evaluate(Map<FieldName, ?> parameters);
}