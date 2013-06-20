/*
 * Copyright (c) 2010 University of Tartu
 */
package org.jpmml.evaluator;

import org.dmg.pmml.*;

import com.sun.xml.bind.*;

public class EvaluationException extends PMMLException {

	public EvaluationException(){
		super();
	}

	public EvaluationException(String message){
		super(message);
	}

	public EvaluationException(Locatable locatable){
		super(locatable);
	}

	public EvaluationException(String message, Locatable locatable){
		super(message, locatable);
	}
}