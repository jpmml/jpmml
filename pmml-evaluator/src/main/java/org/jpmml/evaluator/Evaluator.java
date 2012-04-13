/*
 * Copyright (c) 2012 University of Tartu
 */
package org.jpmml.evaluator;

import java.util.*;

import org.jpmml.manager.*;

import org.dmg.pmml.*;

public interface Evaluator extends Consumer {

	/**
	 * @throws EvaluationException If the evaluation failed.
	 *
	 * @see #getActiveFields()
	 */
	Object evaluate(Map<FieldName, ?> parameters);
}