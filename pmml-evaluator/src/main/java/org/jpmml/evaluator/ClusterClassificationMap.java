/*
 * Copyright (c) 2013 University of Tartu
 */
package org.jpmml.evaluator;

import org.dmg.pmml.*;

public class ClusterClassificationMap extends EntityClassificationMap<Cluster> implements HasDisplayValue, HasClusterId, HasAffinity, HasClusterAffinity {

	ClusterClassificationMap(Type type){
		super(type);
	}

	ClusterClassificationMap(Type type, Cluster cluster){
		super(type, cluster);
	}

	@Override
	public String getDisplayValue(){
		Cluster cluster = getEntity();

		return cluster.getName();
	}

	@Override
	public String getClusterId(){
		return getEntityId();
	}

	@Override
	public Double getAffinity(String value){
		Type type = getType();

		if(!((Type.DISTANCE).equals(type) || (Type.SIMILARITY).equals(type))){
			throw new EvaluationException();
		}

		return getFeature(value);
	}

	@Override
	public Double getClusterAffinity(){
		return getAffinity(getClusterId());
	}
}