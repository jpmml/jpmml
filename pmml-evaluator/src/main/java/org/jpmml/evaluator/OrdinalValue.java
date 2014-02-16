/*
 * Copyright (c) 2013 Villu Ruusmann
 */
package org.jpmml.evaluator;

import java.util.*;

import org.dmg.pmml.*;

public class OrdinalValue extends FieldValue {

	private List<?> ordering = null;


	public OrdinalValue(DataType dataType, Object value){
		super(dataType, value);
	}

	@Override
	public OpType getOpType(){
		return OpType.ORDINAL;
	}

	@Override
	public int compareToString(String string){
		List<?> ordering = getOrdering();
		if(ordering == null){
			return super.compareToString(string);
		}

		return compare(ordering, getValue(), parseValue(string));
	}

	@Override
	public int compareToValue(FieldValue value){
		List<?> ordering = getOrdering();
		if(ordering == null){
			return super.compareToValue(value);
		}

		return compare(ordering, getValue(), value.getValue());
	}

	public List<?> getOrdering(){
		return this.ordering;
	}

	public void setOrdering(List<?> ordering){
		this.ordering = ordering;
	}

	static
	private int compare(List<?> ordering, Object left, Object right){
		int leftIndex = ordering.indexOf(left);
		int rightIndex = ordering.indexOf(right);

		if((leftIndex | rightIndex) < 0){
			throw new EvaluationException();
		}

		return (leftIndex - rightIndex);
	}
}