/*
 * Copyright (c) 2013 University of Tartu
 */
package org.jpmml.evaluator;

import java.util.*;

class ClassificationMap extends LinkedHashMap<String, Double> implements Computable<String>, HasConfidence, HasProbability {

	private Type type = null;


	ClassificationMap(Type type){
		setType(type);
	}

	@Override
	public String getResult(){
		Map.Entry<String, Double> result = null;

		Collection<Map.Entry<String, Double>> entries = entrySet();
		for(Map.Entry<String, Double> entry : entries){

			if(result == null || (entry.getValue()).compareTo(result.getValue()) > 0){
				result = entry;
			}
		}

		if(result == null){
			throw new MissingResultException(null);
		}

		return result.getKey();
	}

	@Override
	public Double getConfidence(String value){
		Type type = getType();

		if(!(Type.CONFIDENCE).equals(type)){
			throw new EvaluationException();
		}

		return getFeature(value);
	}

	@Override
	public Double getProbability(String value){
		Type type = getType();

		if(!(Type.PROBABILITY).equals(type)){
			throw new EvaluationException();
		}

		return getFeature(value);
	}

	private Double getFeature(String value){
		Double result = get(value);

		// The specified value was not encountered during scoring
		if(result == null){
			result = 0d;
		}

		return result;
	}

	void normalizeValues(){
		double sum = 0;

		Collection<Double> values = values();
		for(Double value : values){
			sum += value.doubleValue();
		}

		Collection<Map.Entry<String, Double>> entries = entrySet();
		for(Map.Entry<String, Double> entry : entries){
			entry.setValue(entry.getValue() / sum);
		}
	}

	public Type getType(){
		return this.type;
	}

	private void setType(Type type){
		this.type = type;
	}

	static
	public enum Type {
		PROBABILITY,
		CONFIDENCE,
		;
	}
}