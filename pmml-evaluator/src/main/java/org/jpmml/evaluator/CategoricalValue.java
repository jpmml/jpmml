/*
 * Copyright (c) 2013 Villu Ruusmann
 */
package org.jpmml.evaluator;

import org.dmg.pmml.*;

public class CategoricalValue extends FieldValue {

	public CategoricalValue(DataType dataType, Object value){
		super(dataType, value);
	}

	@Override
	public OpType getOpType(){
		return OpType.CATEGORICAL;
	}

	@Override
	public int compareToString(String string){
		throw new EvaluationException();
	}

	@Override
	public int compareToValue(FieldValue that){
		throw new EvaluationException();
	}
}