/*
 * Copyright (c) 2013 University of Tartu
 */
package org.jpmml.evaluator;

import org.dmg.pmml.*;

class NeuronClassificationMap extends EntityClassificationMap<Entity> {

	NeuronClassificationMap(){
		super(Type.PROBABILITY);
	}

	NeuronClassificationMap(Entity entity){
		super(Type.PROBABILITY, entity);
	}
}