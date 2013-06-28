/*
 * Copyright (c) 2013 University of Tartu
 */
package org.jpmml.evaluator;

import org.dmg.pmml.*;

class NeuronClassificationMap extends EntityClassificationMap<Entity> {

	private Double maxValue = null;


	NeuronClassificationMap(){
	}

	public Double put(Entity entity, String key, Double value){

		if(this.maxValue == null || (value).compareTo(this.maxValue) > 0){
			this.maxValue = value;

			setEntity(entity);
		}

		return super.put(key, value);
	}
}