/*
 * Copyright (c) 2012 University of Tartu
 */
package org.jpmml.evaluator;

import org.jpmml.manager.*;

import org.dmg.pmml.*;

public class ParameterUtil {

	private ParameterUtil(){
	}

	static
	public Object parse(DictionaryField field, String string){
		return parse(field.getDataType(), string);
	}

	static
	public Object parse(DataType dataType, String string){

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
				break;
		}

		throw new UnsupportedFeatureException(dataType);
	}

	static
	public DataType getDataType(Object object){

		if(object instanceof String){
			return DataType.STRING;
		} else

		if(object instanceof Integer){
			return DataType.INTEGER;
		} else

		if(object instanceof Float){
			return DataType.FLOAT;
		} else

		if(object instanceof Double){
			return DataType.DOUBLE;
		} else

		throw new UnsupportedOperationException();
	}

	static
	public DataType getDataType(String string){

		try {
			if(string.indexOf('.') > -1){
				Float.parseFloat(string);

				return DataType.FLOAT;
			} else

			{
				Integer.parseInt(string);

				return DataType.INTEGER;
			}
		} catch(NumberFormatException nfe){
			return DataType.STRING;
		}
	}
}