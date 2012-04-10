/*
 * Copyright (c) 2012 University of Tartu
 */
package org.jpmml.evaluator;

public class UnsupportedFeatureException extends EvaluationException {

	public UnsupportedFeatureException(){
	}

	public UnsupportedFeatureException(String message){
		super(message);
	}

	public UnsupportedFeatureException(Object object){
		this(String.valueOf(object));
	}
}