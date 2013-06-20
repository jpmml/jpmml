/*
 * Copyright (c) 2012 University of Tartu
 */
package org.jpmml.manager;

import org.dmg.pmml.*;

import com.sun.xml.bind.*;

public class ModelManagerException extends PMMLException {

	public ModelManagerException(){
		super();
	}

	public ModelManagerException(String message){
		super(message);
	}

	public ModelManagerException(Locatable locatable){
		super(locatable);
	}

	public ModelManagerException(String message, Locatable locatable){
		super(message, locatable);
	}
}