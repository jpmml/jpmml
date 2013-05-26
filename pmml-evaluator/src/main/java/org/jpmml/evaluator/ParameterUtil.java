/*
 * Copyright (c) 2012 University of Tartu
 */
package org.jpmml.evaluator;

import java.util.*;

import org.jpmml.manager.*;

import org.dmg.pmml.*;

public class ParameterUtil {

	private ParameterUtil(){
	}

	static
	public Object prepare(DataField dataField, MiningField miningField, Object value){

		missingValueHandling:
		if(isMissing(dataField, value)){
			value = miningField.getMissingValueReplacement();
			if(value != null){
				break missingValueHandling;
			}

			return null;
		}

		return cast(dataField.getDataType(), value);
	}

	static
	private boolean isMissing(DataField dataField, Object value){

		if(value == null){
			return true;
		}

		List<Value> fields = dataField.getValues();
		for(Value field : fields){
			Value.Property property = field.getProperty();

			switch(property){
				case MISSING:
					{
						boolean equals = (field.getValue()).equals(toString(value));

						if(equals){
							return true;
						}
					}
					break;
				default:
					break;
			}
		}

		return false;
	}

	/**
	 * Checks the equality between different value representations.
	 *
	 * @param value The {@link #getDataType(Object) runtime data type representation} of the value.
	 * @param string The String representation of the value.
	 */
	static
	public boolean equals(Object value, String string){
		return (value).equals(parse(getDataType(value), string));
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

	/**
	 * @return The data type of the value.
	 */
	static
	public DataType getDataType(Object value){

		if(value instanceof String){
			return DataType.STRING;
		} else

		if(value instanceof Integer){
			return DataType.INTEGER;
		} else

		if(value instanceof Float){
			return DataType.FLOAT;
		} else

		if(value instanceof Double){
			return DataType.DOUBLE;
		}

		throw new EvaluationException();
	}

	/**
	 * @return The least restrictive data type of the data types of two values
	 *
	 * @see #getResultDataType(DataType, DataType)
	 */
	static
	public DataType getResultDataType(Object left, Object right){
		return getResultDataType(getDataType(left), getDataType(right));
	}

	static
	public DataType getResultDataType(DataType left, DataType right){

		if((left).equals(right)){
			return left;
		} // End if

		DataType[] dataTypes = ParameterUtil.precedenceSequence;
		for(DataType dataType : dataTypes){

			if((dataType).equals(left) || (dataType).equals(right)){
				return dataType;
			}
		}

		throw new EvaluationException();
	}

	static
	public Object cast(DataType dataType, Object value){

		switch(dataType){
			case STRING:
				return toString(value);
			case INTEGER:
				return toInteger(value);
			case FLOAT:
				return toFloat(value);
			case DOUBLE:
				return toDouble(value);
			default:
				break;
		}

		throw new EvaluationException();
	}

	/**
	 * Converts the specified value to String data type.
	 *
	 * @see DataType#STRING
	 */
	static
	public String toString(Object value){

		if(value instanceof String){
			return (String)value;
		} else

		if(value instanceof Number){
			Number number = (Number)value;

			return number.toString();
		}

		throw new EvaluationException();
	}

	/**
	 * Converts the specified value to Integer data type.
	 *
	 * @see DataType#INTEGER
	 */
	static
	public Integer toInteger(Object value){

		if(value instanceof String){
			String string = (String)value;

			return Integer.valueOf(string);
		} else

		if(value instanceof Integer){
			return (Integer)value;
		} else

		if(value instanceof Number){
			Number number = (Number)value;

			return Integer.valueOf(number.intValue());
		}

		throw new EvaluationException();
	}

	/**
	 * Converts the specified value to Float data type.
	 *
	 * @see DataType#FLOAT
	 */
	static
	public Float toFloat(Object value){

		if(value instanceof String){
			String string = (String)value;

			return Float.valueOf(string);
		} else

		if(value instanceof Float){
			return (Float)value;
		} else

		if(value instanceof Number){
			Number number = (Number)value;

			return Float.valueOf(number.floatValue());
		}

		throw new EvaluationException();
	}

	/**
	 * Converts the specified value to Double data type.
	 *
	 * @see DataType#DOUBLE
	 */
	static
	public Double toDouble(Object value){

		if(value instanceof String){
			String string = (String)value;

			return Double.valueOf(string);
		} else

		if(value instanceof Double){
			return (Double)value;
		} else

		if(value instanceof Number){
			Number number = (Number)value;

			return Double.valueOf(number.doubleValue());
		}

		throw new EvaluationException();
	}

	static
	public DataType getConstantDataType(String string){

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

	private static final DataType[] precedenceSequence = {DataType.STRING, DataType.DOUBLE, DataType.FLOAT, DataType.INTEGER};
}