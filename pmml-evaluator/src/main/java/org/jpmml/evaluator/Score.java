/*
 * Copyright (c) 2013 University of Tartu
 */
package org.jpmml.evaluator;

import java.util.*;

class Score implements Computable<Double>, HasReasonCodes {

	private Double value = null;

	private List<String> reasonCodes = null;


	Score(Double value, List<String> reasonCodes){
		setValue(value);
		setReasonCodes(reasonCodes);
	}

	@Override
	public Double getResult(){
		return getValue();
	}

	public Double getValue(){
		return this.value;
	}

	private void setValue(Double value){
		this.value = value;
	}

	@Override
	public List<String> getReasonCodes(){
		return this.reasonCodes;
	}

	private void setReasonCodes(List<String> reasonCodes){
		this.reasonCodes = reasonCodes;
	}
}