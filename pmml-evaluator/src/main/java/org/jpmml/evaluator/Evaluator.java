/*
 * Copyright (c) 2012 University of Tartu
 */
package org.jpmml.evaluator;

import java.util.*;

import org.jpmml.manager.*;

import org.dmg.pmml.*;

/**
 * <p>
 * Performs the evaluation of a {@link Model} in "interpreted mode".
 * </p>
 *
 * Obtaining {@link Evaluator} instance:
 * <pre>
 * PMML pmml = ...;
 * PMMLManager pmmlManager = new PMMLManager(pmml);
 * Evaluator evaluator = (Evaluator)pmmlManager.getModelManager(null, ModelEvaluatorFactory.getInstance());
 * </pre>
 *
 * Preparing {@link Evaluator#getActiveFields() active fields}:
 * <pre>
 * Map&lt;FieldName, Object&gt; parameters = new LinkedHashMap&lt;FieldName, Object&gt;();
 * List&lt;FieldName&gt; activeFields = evaluator.getActiveFields();
 * for(FieldName activeField : activeFields){
 *   parameters.put(activeField, ...);
 * }
 * </pre>
 *
 * Performing the {@link Evaluator#evaluate(Map) evaluation}:
 * <pre>
 * Map&lt;FieldName, ?&gt; result = evaluator.evaluate(parameters);
 * </pre>
 *
 * Retrieving the value of the {@link Evaluator#getTarget() predicted field} and {@link Evaluator#getOutputFields() output fields}:
 * <pre>
 * FieldName targetField = evaluator.getTarget();
 * Object targetValue = result.get(targetField);
 *
 * List&lt;FieldName&gt; outputFields = evaluator.getOutputFields();
 * for(FieldName outputField : outputFields){
 *   Object outputValue = result.get(outputField);
 * }
 * </pre>
 *
 * Decoding {@link Computable complex value} to simple value:
 * <pre>
 * Object value = ...;
 * if(value instanceof Computable){
 *   Computable&lt;?&gt; computable = (Computable&lt;?&gt;)value;
 *
 *   value = computable.getResult();
 * }
 * </pre>
 *
 * @see EvaluatorUtil
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
	 * @param parameters Map of {@link #getActiveFields() active field} values.
	 *
	 * @return Map of {@link #getPredictedFields() predicted field} values.
	 * Simple values should be represented using the Java equivalents of PMML data types (eg. String, Integer, Float, Double etc.).
	 * Complex values should be represented as instances of {@link Computable} that return simple values.
	 *
	 * @throws EvaluationException If the evaluation fails
	 *
	 * @see Computable
	 */
	Map<FieldName, ?> evaluate(Map<FieldName, ?> parameters);
}