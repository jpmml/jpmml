/*
 * Copyright (c) 2012 University of Tartu
 */
package org.jpmml.manager;

import org.dmg.pmml.*;

public class UnsupportedFeatureException extends ModelManagerException {

	public UnsupportedFeatureException(){
		super();
	}

	public UnsupportedFeatureException(String message){
		super(message);
	}

	public UnsupportedFeatureException(PMMLObject element){
		super((element.getClass()).getName(), element);
	}

	public UnsupportedFeatureException(PMMLObject element, Enum<?> attribute){
		super((attribute.getClass()).getName() + "#" + attribute.name(), element);
	}
}