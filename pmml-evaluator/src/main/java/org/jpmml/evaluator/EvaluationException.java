/*
 * Copyright (c) 2010 University of Tartu
 */
package org.jpmml.evaluator;

import org.dmg.pmml.*;

public class EvaluationException extends PMMLException {

	public EvaluationException(){
		super();
	}

	public EvaluationException(String message){
		super(message);
	}

	public EvaluationException(PMMLObject context){
		super(context);
	}

	public EvaluationException(String message, PMMLObject context){
		super(message, context);
	}
}