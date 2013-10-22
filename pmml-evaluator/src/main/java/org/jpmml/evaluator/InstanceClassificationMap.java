/*
 * Copyright, KNIME.com AG, Zurich, Switzerland
 */
package org.jpmml.evaluator;

import java.util.*;

import com.google.common.annotations.*;

import static com.google.common.base.Preconditions.*;

@Beta
public class InstanceClassificationMap extends ClassificationMap<String> implements HasEntityIdRanking, HasClusterId, HasAffinityRanking, HasClusterAffinity {

	private Object result = null;


	protected InstanceClassificationMap(Type type, Object result){
		super(type);

		checkArgument((Type.DISTANCE).equals(type) || (Type.SIMILARITY).equals(type));

		setResult(result);
	}

	@Override
	public Object getResult(){

		if(this.result == null){
			throw new MissingResultException(null);
		}

		return this.result;
	}

	private void setResult(Object result){
		this.result = result;
	}

	@Override
	public String getEntityId(){
		Map.Entry<String, Double> entry = getWinner();
		if(entry == null){
			return null;
		}

		return entry.getKey();
	}

	@Override
	public List<String> getEntityIdRanking(){
		return getWinnerKeys();
	}

	@Override
	public String getClusterId(){
		return getEntityId();
	}

	@Override
	public Double getAffinity(String id){
		return getFeature(id);
	}

	@Override
	public List<Double> getAffinityRanking(){
		return getWinnerValues();
	}

	@Override
	public Double getClusterAffinity(){
		return getAffinity(getClusterId());
	}
}