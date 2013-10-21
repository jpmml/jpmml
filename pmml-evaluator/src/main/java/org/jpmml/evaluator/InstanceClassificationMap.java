/*
 * Copyright, KNIME.com AG, Zurich, Switzerland
 */
package org.jpmml.evaluator;

import java.util.*;

class InstanceClassificationMap extends ClassificationMap<String> implements HasEntityIdRanking, HasClusterId, HasAffinityRanking, HasClusterAffinity {

	private Object result = null;


	InstanceClassificationMap(Object result){
		super(Type.DISTANCE);

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
		Type type = getType();

		if(!(Type.DISTANCE).equals(type)){
			throw new EvaluationException();
		}

		return getFeature(id);
	}

	@Override
	public List<Double> getAffinityRanking(){
		Type type = getType();

		if(!(Type.DISTANCE).equals(type)){
			throw new EvaluationException();
		}

		return getWinnerValues();
	}

	@Override
	public Double getClusterAffinity(){
		return getAffinity(getClusterId());
	}
}