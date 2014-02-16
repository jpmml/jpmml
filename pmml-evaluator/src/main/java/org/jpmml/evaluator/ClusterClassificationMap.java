/*
 * Copyright (c) 2013 Villu Ruusmann
 */
package org.jpmml.evaluator;

import org.dmg.pmml.*;

import com.google.common.annotations.*;

import static com.google.common.base.Preconditions.*;

@Beta
public class ClusterClassificationMap extends EntityClassificationMap<Cluster> implements HasDisplayValue, HasClusterId, HasAffinity, HasClusterAffinity {

	protected ClusterClassificationMap(Type type){
		super(type);

		checkArgument((Type.DISTANCE).equals(type) || (Type.SIMILARITY).equals(type));
	}

	protected ClusterClassificationMap(Type type, Cluster cluster){
		super(type, cluster);

		checkArgument((Type.DISTANCE).equals(type) || (Type.SIMILARITY).equals(type));
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
		return getFeature(value);
	}

	@Override
	public Double getClusterAffinity(){
		return getAffinity(getClusterId());
	}
}