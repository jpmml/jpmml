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

		Type type = getType();

		Collection<Map.Entry<String, Double>> entries = entrySet();
		for(Map.Entry<String, Double> entry : entries){

			if(result == null || type.isMoreOptimal(entry.getValue(), result.getValue())){
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

	Double getFeature(String value){
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
		PROBABILITY(Ordering.INCREASING),
		CONFIDENCE(Ordering.INCREASING),
		DISTANCE(Ordering.DECREASING),
		SIMILARITY(Ordering.INCREASING),
		;

		private Ordering ordering;


		private Type(Ordering ordering){
			setOrdering(ordering);
		}

		/**
		 * Indicates if the first argument is more optimal than the second argument.
		 *
		 * @param left A value
		 * @param right The reference value
		 *
		 * @see Comparable
		 */
		public <C extends Comparable<C>> boolean isMoreOptimal(C left, C right){
			int order = (left).compareTo(right);

			Ordering ordering = getOrdering();
			switch(ordering){
				case INCREASING:
					return (order > 0);
				case DECREASING:
					return (order < 0);
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