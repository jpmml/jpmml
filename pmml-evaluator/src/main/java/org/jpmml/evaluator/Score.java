/*
 * Copyright (c) 2013 University of Tartu
 */
package org.jpmml.evaluator;

import java.util.*;

class Score implements Computable, HasReasonCodes {

	private Number value = null;

	private List<String> reasonCodes = null;


	Score(Number value, List<String> reasonCodes){
		setValue(value);
		setReasonCodes(reasonCodes);
	}

	@Override
	public Number getResult(){
		return getValue();
	}

	public Number getValue(){
		return this.value;
	}

	private void setValue(Number value){
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