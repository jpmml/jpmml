/*
 * Copyright (c) 2012 University of Tartu
 */
package org.jpmml.manager;

import org.dmg.pmml.*;

/**
 * Signals that the specified PMML content is not supported (but is probably valid).
 */
public class UnsupportedFeatureException extends PMMLException {

	@Deprecated
	public UnsupportedFeatureException(){
		super();
	}

	@Deprecated
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