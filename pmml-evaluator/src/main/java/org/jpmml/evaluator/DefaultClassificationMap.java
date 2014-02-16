/*
 * Copyright (c) 2013 Villu Ruusmann
 */
package org.jpmml.evaluator;

import com.google.common.annotations.*;

@Beta
public class DefaultClassificationMap<K> extends ClassificationMap<K> implements HasProbability {

	protected DefaultClassificationMap(){
		super(Type.PROBABILITY);
	}

	@Override
	public Double getProbability(String value){
		return getFeature(value);
	}
}