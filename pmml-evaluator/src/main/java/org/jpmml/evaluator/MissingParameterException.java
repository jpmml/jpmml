/*
 * Copyright (c) 2013 University of Tartu
 */
package org.jpmml.evaluator;

import org.dmg.pmml.*;

public class MissingParameterException extends EvaluationException {

	public MissingParameterException(FieldName name){
		super(name != null ? name.getValue() : "(empty)");
	}

	public MissingParameterException(DerivedField derivedField){
		this(derivedField.getName());
	}
}