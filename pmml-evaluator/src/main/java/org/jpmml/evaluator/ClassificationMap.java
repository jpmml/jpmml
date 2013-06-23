/*
 * Copyright (c) 2013 University of Tartu
 */
package org.jpmml.evaluator;

import java.util.*;

class ClassificationMap extends LinkedHashMap<String, Double> implements Computable<String>, HasProbability {

	ClassificationMap(){
	}

	public String getResult(){
		Map.Entry<String, Double> result = null;

		Collection<Map.Entry<String, Double>> entries = entrySet();
		for(Map.Entry<String, Double> entry : entries){

			if(entry.getValue() == null){
				continue;
			} // End if

			if(result == null || (entry.getValue()).compareTo(result.getValue()) > 0){
				result = entry;
			}
		}

		if(result == null){
			throw new MissingResultException(null);
		}

		return result.getKey();
	}

	public Double getProbability(String value){
		Double result = get(value);

		// The specified value was not encountered during scoring
		if(result == null){
			result = 0d;
		}

		return result;
	}

	void normalizeProbabilities(){
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
}