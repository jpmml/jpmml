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
	 * @return Map of {@link #getPredictedFields() predicted field values}
	 *
	 * @throws EvaluationException If the evaluation failed.
	 */
	Map<FieldName, ?> evaluate(Map<FieldName, ?> parameters);
}