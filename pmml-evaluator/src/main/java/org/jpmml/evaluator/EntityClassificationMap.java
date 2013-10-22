/*
 * Copyright (c) 2013 University of Tartu
 */
package org.jpmml.evaluator;

import org.dmg.pmml.*;

import com.google.common.annotations.*;

@Beta
abstract
public class EntityClassificationMap<E extends Entity> extends ClassificationMap<String> implements HasEntityId {

	private E entity = null;

	private Double maxValue = null;


	protected EntityClassificationMap(Type type){
		super(type);
	}

	protected EntityClassificationMap(Type type, E entity){
		super(type);

		setEntity(entity);
	}

	@Override
	public String getResult(){
		return (String)super.getResult();
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
		Type type = getType();

		if(this.maxValue == null || type.isMoreOptimal(value, this.maxValue)){
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