/*
 * Copyright (c) 2013 University of Tartu
 */
package org.jpmml.evaluator;

import java.util.*;

class Score implements Computable<Double>, HasReasonCode {

	private Double value = null;

	private List<String> reasonCodeRanking = null;


	Score(Double value, List<String> reasonCodeRanking){
		setValue(value);
		setReasonCodeRanking(reasonCodeRanking);
	}

	public Double getResult(){
		return getValue();
	}

	public String getReasonCode(int rank){

		if(rank < 1){
			throw new IllegalArgumentException();
		}

		int index = (rank - 1);

		List<String> reasonCodeRanking = getReasonCodeRanking();

		if(reasonCodeRanking.size() > 0){

			if(index < reasonCodeRanking.size()){
				return reasonCodeRanking.get(index);
			}

			// The last meaningful explanation
			return reasonCodeRanking.get(reasonCodeRanking.size() - 1);
		}

		return null;
	}

	public Double getValue(){
		return this.value;
	}

	private void setValue(Double value){
		this.value = value;
	}

	public List<String> getReasonCodeRanking(){
		return this.reasonCodeRanking;
	}

	private void setReasonCodeRanking(List<String> reasonCodeRanking){
		this.reasonCodeRanking = reasonCodeRanking;
	}
}