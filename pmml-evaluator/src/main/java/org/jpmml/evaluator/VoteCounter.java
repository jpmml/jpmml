/*
 * Copyright (c) 2013 KNIME.com AG, Zurich, Switzerland
 */
package org.jpmml.evaluator;

import java.util.*;

import com.google.common.collect.*;

public class VoteCounter<K> extends LinkedHashMap<K, Double> {

	public VoteCounter(){
	}

	public Set<K> getWinners(){
		Set<K> result = Sets.newLinkedHashSet();

		Double max = Collections.max(values());

		Collection<Map.Entry<K, Double>> entries = entrySet();
		for(Map.Entry<K, Double> entry : entries){

			if((max).equals(entry.getValue())){
				result.add(entry.getKey());
			}
		}

		return result;
	}

	public void increment(K key){
		increment(key, 1d);
	}

	public void increment(K key, Double value){
		Double sum = get(key);
		if(sum == null){
			sum = 0d;
		}

		put(key, (sum + value));
	}
}