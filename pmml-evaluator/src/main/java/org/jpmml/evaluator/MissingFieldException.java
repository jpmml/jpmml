/*
 * Copyright (c) 2013 University of Tartu
 */
package org.jpmml.evaluator;

import org.dmg.pmml.*;

/**
 * Signals that a required field value is missing.
 */
public class MissingFieldException extends EvaluationException {

	@Deprecated
	public MissingFieldException(FieldName name){
		super(name.getValue());
	}

	public MissingFieldException(FieldName name, PMMLObject context){
		super(name.getValue(), context);
	}
}