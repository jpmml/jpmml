/*
 * Copyright (c) 2013 Villu Ruusmann
 */
package org.jpmml.evaluator;

import java.util.*;

import com.google.common.annotations.*;
import com.google.common.base.*;
import com.google.common.collect.*;

@Beta
public class ClassificationMap<K> extends LinkedHashMap<K, Double> implements Computable {

	private Type type = null;


	protected ClassificationMap(Type type){
		setType(type);
	}

	@Override
	public Object getResult(){
		Map.Entry<K, Double> entry = getWinner();
		if(entry == null){
			throw new MissingResultException(null);
		}

		return entry.getKey();
	}

	Double getFeature(String value){
		Double result = get(value);

		// The specified value was not encountered during scoring
		if(result == null){
			return 0d;
		}

		return result;
	}

	Map.Entry<K, Double> getWinner(){
		Map.Entry<K, Double> result = null;

		Type type = getType();

		Collection<Map.Entry<K, Double>> entries = entrySet();
		for(Map.Entry<K, Double> entry : entries){

			if(result == null || type.compare(entry.getValue(), result.getValue()) > 0){
				result = entry;
			}
		}

		return result;
	}

	List<Map.Entry<K, Double>> getWinnerList(){
		List<Map.Entry<K, Double>> result = Lists.newArrayList(entrySet());

		final
		Type type = getType();

		Comparator<Map.Entry<K, Double>> comparator = new Comparator<Map.Entry<K, Double>>(){

			@Override
			public int compare(Map.Entry<K, Double> left, Map.Entry<K, Double> right){
				return -1 * type.compare(left.getValue(), right.getValue());
			}
		};
		Collections.sort(result, comparator);

		return result;
	}

	List<K> getWinnerKeys(){
		List<Map.Entry<K, Double>> winners = getWinnerList();

		Function<Map.Entry<K, Double>, K> function = new Function<Map.Entry<K, Double>, K>(){

			@Override
			public K apply(Map.Entry<K, Double> entry){
				return entry.getKey();
			}
		};

		return Lists.transform(winners, function);
	}

	List<Double> getWinnerValues(){
		List<Map.Entry<K, Double>> winners = getWinnerList();

		Function<Map.Entry<K, Double>, Double> function = new Function<Map.Entry<K, Double>, Double>(){

			@Override
			public Double apply(Map.Entry<K, Double> entry){
				return entry.getValue();
			}
		};

		return Lists.transform(winners, function);
	}

	void normalizeValues(){
		double sum = 0;

		Collection<Double> values = values();
		for(Double value : values){
			sum += value.doubleValue();
		}

		Collection<Map.Entry<K, Double>> entries = entrySet();
		for(Map.Entry<K, Double> entry : entries){
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
	public enum Type implements Comparator<Double> {
		PROBABILITY(Ordering.INCREASING),
		CONFIDENCE(Ordering.INCREASING),
		DISTANCE(Ordering.DECREASING),
		SIMILARITY(Ordering.INCREASING),
		VOTE(Ordering.INCREASING),
		;

		private Ordering ordering;


		private Type(Ordering ordering){
			setOrdering(ordering);
		}

		/**
		 * Calculates the order between arguments.
		 *
		 * @param left A value
		 * @param right The reference value
		 */
		@Override
		public int compare(Double left, Double right){
			int order = (left).compareTo(right);

			Ordering ordering = getOrdering();
			switch(ordering){
				case INCREASING:
					return order;
				case DECREASING:
					return -1 * order;
				default:
					throw new IllegalStateException();
			}
		}

		public Ordering getOrdering(){
			return this.ordering;
		}

		private void setOrdering(Ordering ordering){
			this.ordering = ordering;
		}

		static
		private enum Ordering {
			/**
			 * The most positive value represents the optimum.
			 */
			INCREASING,

			/**
			 * The most negative value represents the optimum.
			 */
			DECREASING,
		}
	}
}