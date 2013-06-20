/*
 * Copyright (c) 2012 University of Tartu
 */
package org.jpmml.manager;

import org.dmg.pmml.*;

import com.sun.xml.bind.*;

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

	public InvalidFeatureException(Locatable locatable){
		super(locatable);
	}

	public InvalidFeatureException(String message, Locatable locatable){
		super(message, locatable);
	}
}