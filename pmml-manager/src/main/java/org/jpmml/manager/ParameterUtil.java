/*
 * Copyright (c) 2012 University of Tartu
 */
package org.jpmml.manager;

import java.util.*;

import org.dmg.pmml.*;

public class ParameterUtil {

	private ParameterUtil(){
	}

	static
	public Object getValue(Map<FieldName, ?> parameters, FieldName name){
		return getValue(parameters, name, false);
	}

	static
	public Object getValue(Map<FieldName, ?> parameters, FieldName name, boolean nullable){
		Object value = parameters.get(name);

		if(value == null && !nullable){
			throw new EvaluationException("Missing parameter " + name.getValue());
		}

		return value;
	}

	static
	public Object parse(DataField dataField, String string){
		DataType dataType = dataField.getDataType();

		switch(dataType){
			case STRING:
				return string;
			case INTEGER:
				return new Integer(string);
			case FLOAT:
				return new Float(string);
			case DOUBLE:
				return new Double(string);
			default:
				throw new IllegalArgumentException();
		}
	}
}