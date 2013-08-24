/*
 * Copyright (c) 2013 University of Tartu
 */
package org.jpmml.evaluator;

import java.util.*;

import org.dmg.pmml.*;

import com.google.common.base.*;
import com.google.common.collect.*;

public class FieldValueUtil {

	private FieldValueUtil(){
	}

	static
    public FieldValue create(Object value){
    	return create(null, null, value);
    }

	static
    public FieldValue create(Field field, Object value){
		FieldValue result = create(field.getDataType(), field.getOptype(), value);

		if(field instanceof TypeDefinitionField){
			return enhance((TypeDefinitionField)field, result);
		}

		return result;
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
		FieldValue result = refine(field.getDataType(), field.getOptype(), value);

		if(field instanceof TypeDefinitionField){
			return enhance((TypeDefinitionField)field, result);
		}

		return result;
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
	public FieldValue enhance(TypeDefinitionField field, FieldValue value){

		if(value == null){
			return null;
		} // End if

		if(value instanceof OrdinalValue){
			OrdinalValue ordinalValue = (OrdinalValue)value;
			ordinalValue.setOrdering(getOrdering(field, ordinalValue.getDataType()));
		}

		return value;
	}

	static
	public Object getValue(FieldValue value){
		return (value != null ? value.getValue() : null);
	}

	static
	private List<?> getOrdering(TypeDefinitionField field, final DataType dataType){
		List<String> values = ParameterUtil.getValidValues(field);
		if(values.isEmpty()){
			return null;
		}

		Function<String, Object> function = new Function<String, Object>(){

			@Override
			public Object apply(String string){
				return ParameterUtil.parse(dataType, string);
			}
		};

		return Lists.newArrayList(Iterables.transform(values, function));
	}
}