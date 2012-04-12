/*
 * Copyright (c) 2012 University of Tartu
 */
package org.jpmml.manager;

import org.dmg.pmml.*;

public class UnsupportedFeatureException extends ModelManagerException {

	public UnsupportedFeatureException(){
	}

	public UnsupportedFeatureException(String message){
		super(message);
	}

	public UnsupportedFeatureException(PMMLObject element){
		this((element.getClass()).getName());
	}

	public UnsupportedFeatureException(Enum<?> attribute){
		this((attribute.getClass()).getName() + "#" + attribute.name());
	}
}