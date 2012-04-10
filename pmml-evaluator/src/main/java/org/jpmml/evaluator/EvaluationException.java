/*
 * Copyright (c) 2010 University of Tartu
 */
package org.jpmml.evaluator;

public class EvaluationException extends RuntimeException {

	public EvaluationException(){
	}

	public EvaluationException(String message){
		super(message);
	}
}