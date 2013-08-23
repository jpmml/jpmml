/*
 * Copyright (c) 2013 University of Tartu
 */
package org.jpmml.evaluator;

import java.util.*;

import org.dmg.pmml.*;

public class FieldValueUtil {

	private FieldValueUtil(){
	}

	static
    public FieldValue create(Object value){
    	return create(null, null, value);
    }

	static
    public FieldValue create(Field field, Object value){
    	return create(field.getDataType(), field.getOptype(), value);
    }

	static
	public FieldValue create(DataType dataType, OpType opType, Object value){

		if(value == null){
			return null;
		} // End if

		if(value instanceof Collection){

			if(dataType == null){
				dataType = DataType.STRING;
			} // End if

			if(opType == null){
				opType = OpType.CATEGORICAL;
			}
		} else

		{
			if(dataType == null){
				dataType = ParameterUtil.getDataType(value);
			} else

			{
				value = ParameterUtil.cast(dataType, value);
			} // End if

			if(opType == null){
				opType = ParameterUtil.getOpType(dataType);
			}
		}

		switch(opType){
			case CONTINUOUS:
				return new ContinuousValue(dataType, value);
			case CATEGORICAL:
				return new CategoricalValue(dataType, value);
			case ORDINAL:
				return new OrdinalValue(dataType, value);
			default:
				throw new EvaluationException();
		}
	}

	static
	public FieldValue refine(Field field, FieldValue value){
		return refine(field.getDataType(), field.getOptype(), value);
	}

	static
	public FieldValue refine(DataType dataType, OpType opType, FieldValue value){

		if(value == null){
			return null;
		}

		DataType refinedDataType = null;
		if(dataType != null && !(dataType).equals(value.getDataType())){
			refinedDataType = dataType;
		}

		OpType refinedOpType = null;
		if(opType != null && !(opType).equals(value.getOpType())){
			refinedOpType = opType;
		}

		boolean refined = (refinedDataType != null) || (refinedOpType != null);
		if(refined){
			return create(refinedDataType, refinedOpType, value.getValue());
		}

		return value;
	}

	static
	public Object getValue(FieldValue value){
		return (value != null ? value.getValue() : null);
	}
}