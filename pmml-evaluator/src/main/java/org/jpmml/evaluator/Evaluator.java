/*
 * Copyright (c) 2012 University of Tartu
 */
package org.jpmml.evaluator;

import java.util.*;

import org.dmg.pmml.*;

public interface Evaluator {

	/**
	 * @throws EvaluationException If the evaluation failed.
	 */
	Object evaluate(Map<FieldName, ?> parameters);
}