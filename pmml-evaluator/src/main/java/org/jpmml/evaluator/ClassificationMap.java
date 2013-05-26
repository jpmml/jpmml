/*
 * Copyright (c) 2013 University of Tartu
 */
package org.jpmml.evaluator;

import java.util.*;

class ClassificationMap extends LinkedHashMap<String, Double> implements Classification {

	ClassificationMap(){
	}

	public String getResult(){
		Map.Entry<String, Double> result = null;

		Collection<Map.Entry<String, Double>> entries = entrySet();
		for(Map.Entry<String, Double> entry : entries){

			if(result == null || (result.getValue()).compareTo(entry.getValue()) < 0){
				result = entry;
			}
		}

		if(result == null){
			throw new EvaluationException();
		}

		return result.getKey();
	}

	public Double getProbability(String value){
		return get(value);
	}
}