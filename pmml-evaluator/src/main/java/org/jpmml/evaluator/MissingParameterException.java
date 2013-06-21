/*
 * Copyright (c) 2013 University of Tartu
 */
package org.jpmml.evaluator;

import org.dmg.pmml.*;

public class MissingParameterException extends EvaluationException {

	@Deprecated
	public MissingParameterException(FieldName name){
		super(name.getValue());
	}

	public MissingParameterException(FieldName name, PMMLObject context){
		super(name.getValue(), context);
	}
}