/*
 * Copyright (c) 2013 University of Tartu
 */
package org.jpmml.evaluator;

import org.dmg.pmml.*;

import com.google.common.annotations.*;

@Beta
public class NeuronClassificationMap extends EntityClassificationMap<Entity> implements HasProbability {

	protected NeuronClassificationMap(){
		super(Type.PROBABILITY);
	}

	protected NeuronClassificationMap(Entity entity){
		super(Type.PROBABILITY, entity);
	}

	@Override
	public Double getProbability(String value){
		return getFeature(value);
	}
}