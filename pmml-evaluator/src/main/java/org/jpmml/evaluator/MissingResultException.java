/*
 * Copyright (c) 2013 University of Tartu
 */
package org.jpmml.evaluator;

import org.dmg.pmml.*;

/**
 * Signals a missing result.
 */
public class MissingResultException extends EvaluationException {

	public MissingResultException(PMMLObject context){
		super(context);
	}

	public MissingResultException(String message, PMMLObject context){
		super(message, context);
	}
}