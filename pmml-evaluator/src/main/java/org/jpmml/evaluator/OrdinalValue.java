/*
 * Copyright (c) 2013 University of Tartu
 */
package org.jpmml.evaluator;

import org.dmg.pmml.*;

public class OrdinalValue extends FieldValue {

	public OrdinalValue(DataType dataType, Object value){
		super(dataType, value);
	}

	@Override
	public OpType getOpType(){
		return OpType.ORDINAL;
	}

	@Override
	public int compareToString(String string){
		return super.compareToString(string);
	}

	@Override
	public int compareToValue(FieldValue that){
		return super.compareToValue(that);
	}
}