/*
 * Copyright (c) 2012 University of Tartu
 */
package org.jpmml.evaluator;

import java.util.*;

import org.jpmml.manager.*;

import org.dmg.pmml.*;

public interface Evaluator {

	/**
	 * Gets the independent (ie. input) fields of a {@link Model} from its {@link MiningSchema}.
	 *
	 * @see ModelManager#getActiveFields()
	 */
	List<FieldName> getActiveFields();

	/**
	 * Gets the dependent (ie. output) field(s) of a {@link Model} from its {@link MiningSchema}.
	 *
	 * @see ModelManager#getPredictedFields()
	 */
	List<FieldName> getPredictedFields();

	/**
	 * Gets the data definition of a field from the {@link DataDictionary}.
	 *
	 * @see PMMLManager#getDataField(FieldName)
	 */
	DataField getDataField(FieldName fieldName);

	/**
	 * @throws EvaluationException If the evaluation failed.
	 *
	 * @see #getActiveFields()
	 */
	Object evaluate(Map<FieldName, ?> parameters);
}