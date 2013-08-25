/*
 * Copyright (c) 2013 University of Tartu
 */
package org.jpmml.evaluator;

import org.dmg.pmml.*;

public class TypeCheckException extends EvaluationException {

	public TypeCheckException(DataType expected, FieldValue value){
		this(expected, FieldValueUtil.getValue(value));
	}

	public TypeCheckException(DataType expected, Object value){
		super(formatMessage(expected, (value != null ? TypeUtil.getDataType(value) : null), value));
	}

	public TypeCheckException(Class<?> expected, FieldValue value){
		this(expected, FieldValueUtil.getValue(value));
	}

	public TypeCheckException(Class<?> expected, Object value){
		super(formatMessage(expected, (value != null ? value.getClass() : null), value));
	}

	static
	private String formatMessage(DataType expected, DataType actual, Object value){
		String message = "Expected: " + expected + ", actual: " + (actual != null ? actual : "null");

		if(value != null){
			message += (" (" + String.valueOf(value) + ")");
		}

		return message;
	}

	static
	private String formatMessage(Class<?> expected, Class<?> actual, Object value){
		String message = "Expected: " + expected.getName() + ", actual: " + (actual != null ? actual.getName() : "null");

		if(value != null){
			message += (" (" + String.valueOf(value) + ")");
		}

		return message;
	}
}