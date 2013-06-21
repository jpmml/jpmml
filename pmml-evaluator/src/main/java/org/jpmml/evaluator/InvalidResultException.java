/*
 * Copyright (c) 2013 University of Tartu
 */
package org.jpmml.evaluator;

import org.dmg.pmml.*;

/**
 * Signals an invalid result.
 */
public class InvalidResultException extends EvaluationException {

	public InvalidResultException(PMMLObject context){
		super(context);
	}

	public InvalidResultException(String message, PMMLObject context){
		super(message, context);
	}
}