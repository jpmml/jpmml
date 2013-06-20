/*
 * Copyright (c) 2012 University of Tartu
 */
package org.jpmml.manager;

import org.dmg.pmml.*;

/**
 * Signals that the specified PMML content is invalid.
 */
public class InvalidFeatureException extends PMMLException {

	@Deprecated
	public InvalidFeatureException(){
		super();
	}

	@Deprecated
	public InvalidFeatureException(String message){
		super(message);
	}

	public InvalidFeatureException(PMMLObject context){
		super(context);
	}

	public InvalidFeatureException(String message, PMMLObject context){
		super(message, context);
	}
}