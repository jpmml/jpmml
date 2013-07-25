/*
 * Copyright (c) 2013 University of Tartu
 */
package org.jpmml.evaluator;

import org.dmg.pmml.*;

abstract
class EntityClassificationMap<E extends Entity> extends ClassificationMap implements HasEntityId {

	private E entity = null;

	private Double maxValue = null;


	EntityClassificationMap(){
	}

	EntityClassificationMap(E entity){
		setEntity(entity);
	}

	@Override
	public String getEntityId(){
		E entity = getEntity();

		if(entity != null){
			return entity.getId();
		}

		return null;
	}

	Double put(E entity, String key, Double value){

		if(this.maxValue == null || (value).compareTo(this.maxValue) > 0){
			this.maxValue = value;

			setEntity(entity);
		}

		return super.put(key, value);
	}

	public E getEntity(){
		return this.entity;
	}

	void setEntity(E entity){
		this.entity = entity;
	}
}