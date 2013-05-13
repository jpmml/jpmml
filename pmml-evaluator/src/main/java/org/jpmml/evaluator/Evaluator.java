/*
 * Copyright (c) 2012 University of Tartu
 */
package org.jpmml.evaluator;

import java.util.*;

import org.jpmml.manager.*;

import org.dmg.pmml.*;

/**
 * <pre>
 * PMML pmml = ...;
 * PMMLManager pmmlManager = new PMMLManager(pmml);
 * Evaluator evaluator = (Evaluator)pmmlManager.getModelManager(null, ModelEvaluatorFactory.getInstance());
 *
 * Map<FieldName, Object> parameters = new LinkedHashMap<FieldName, Object>();
 * List<FieldName> activeFields = evaluator.getActiveFields();
 * for(FieldName activeField : activeFields){
 *   parameters.put(activeField, ...);
 * }
 *
 * Map<FieldName, ?> result = evaluator.evaluate(parameters);
 *
 * FieldName target = evaluator.getTarget();
 * Object value = result.get(target);
 *
 * // Decode complex value to simple value
 * if(value instanceof Computable){
 *   Computable<?> computable = (Computable<?>)value;
 *
 *   value = computable.getResult();
 * }
 * </pre>
 */
public interface Evaluator extends Consumer {

	/**
	 * Convenience method for retrieving the predicted field.
	 *
	 * @return The predicted field
	 *
	 * @throws ModelManagerException If the number of predicted fields is not exactly one
	 *
	 * @see Consumer#getPredictedFields()
	 */
	FieldName getTarget();

	/**
	 * @param parameters Map of {@link #getActiveFields() active field values}.
	 *
	 * @return Map of {@link #getPredictedFields() predicted field values}.
	 * Simple values should be represented using the Java equivalents of PMML data types (eg. String, Integer, Float, Double etc.).
	 * Complex values should be represented as instances of {@link Computable} that return simple values.
	 *
	 * @throws EvaluationException If the evaluation fails
	 *
	 * @see Computable
	 */
	Map<FieldName, ?> evaluate(Map<FieldName, ?> parameters);
}