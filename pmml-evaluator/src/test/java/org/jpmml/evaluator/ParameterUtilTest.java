/*
 * Copyright (c) 2013 University of Tartu
 */
package org.jpmml.evaluator;

import java.util.*;

import org.dmg.pmml.*;
import org.dmg.pmml.Interval.Closure;
import org.dmg.pmml.Value.*;

import org.junit.*;

import static org.junit.Assert.*;

public class ParameterUtilTest {

	@Test
	public void prepare(){
		FieldName name = new FieldName("x");

		DataField dataField = new DataField(name, OpType.CONTINUOUS, DataType.DOUBLE);
		MiningField miningField = new MiningField(name);

		assertEquals(1d, ParameterUtil.prepare(dataField, miningField, "1"));
		assertEquals(1d, ParameterUtil.prepare(dataField, miningField, 1));
		assertEquals(1d, ParameterUtil.prepare(dataField, miningField, 1f));
		assertEquals(1d, ParameterUtil.prepare(dataField, miningField, 1d));

		List<Value> fieldValues = dataField.getValues();

		Value missingValue = createValue("N/A", Property.MISSING);
		fieldValues.add(missingValue);

		assertEquals(null, ParameterUtil.prepare(dataField, miningField, null));
		assertEquals(null, ParameterUtil.prepare(dataField, miningField, "N/A"));

		miningField.setMissingValueReplacement("0");

		assertEquals(0d, ParameterUtil.prepare(dataField, miningField, null));
		assertEquals(0d, ParameterUtil.prepare(dataField, miningField, "N/A"));

		miningField.setInvalidValueTreatment(InvalidValueTreatmentMethodType.AS_MISSING);

		List<Interval> fieldIntervals = dataField.getIntervals();

		Interval validInterval = new Interval(Closure.CLOSED_CLOSED);
		validInterval.setLeftMargin(1d);
		validInterval.setRightMargin(3d);
		fieldIntervals.add(validInterval);

		assertEquals(1d, ParameterUtil.prepare(dataField, miningField, 1d));
		assertEquals(0d, ParameterUtil.prepare(dataField, miningField, 5d));

		fieldValues.clear();
		fieldIntervals.clear();

		fieldValues.add(missingValue);
		fieldValues.add(createValue("1", Value.Property.VALID));
		fieldValues.add(createValue("2", Value.Property.VALID));
		fieldValues.add(createValue("3", Value.Property.VALID));

		assertEquals(1d, ParameterUtil.prepare(dataField, miningField, 1d));
		assertEquals(0d, ParameterUtil.prepare(dataField, miningField, 5d));

		fieldValues.clear();

		fieldValues.add(missingValue);
		fieldValues.add(createValue("1", Value.Property.INVALID));

		assertEquals(0d, ParameterUtil.prepare(dataField, miningField, 1d));
		assertEquals(5d, ParameterUtil.prepare(dataField, miningField, 5d));
	}

	@Test
	public void getDataType(){
		assertEquals(DataType.STRING, ParameterUtil.getDataType("value"));

		assertEquals(DataType.INTEGER, ParameterUtil.getDataType(1));
		assertEquals(DataType.FLOAT, ParameterUtil.getDataType(1f));
		assertEquals(DataType.DOUBLE, ParameterUtil.getDataType(1d));
	}

	@Test
	public void getResultDataType(){
		assertEquals(DataType.DOUBLE, ParameterUtil.getResultDataType(1d, 1f));
		assertEquals(DataType.DOUBLE, ParameterUtil.getResultDataType(1d, 1));

		assertEquals(DataType.DOUBLE, ParameterUtil.getResultDataType(1f, 1d));
		assertEquals(DataType.FLOAT, ParameterUtil.getResultDataType(1f, 1));

		assertEquals(DataType.DOUBLE, ParameterUtil.getResultDataType(1, 1d));
		assertEquals(DataType.FLOAT, ParameterUtil.getResultDataType(1, 1f));
	}

	@Test
	public void getConstantDataType(){
		assertEquals(DataType.FLOAT, ParameterUtil.getConstantDataType("1.0"));
		assertEquals(DataType.FLOAT, ParameterUtil.getConstantDataType("1.0E0"));
		assertEquals(DataType.STRING, ParameterUtil.getConstantDataType("1.0X"));

		assertEquals(DataType.INTEGER, ParameterUtil.getConstantDataType("1"));
		assertEquals(DataType.STRING, ParameterUtil.getConstantDataType("1E0"));
		assertEquals(DataType.STRING, ParameterUtil.getConstantDataType("1X"));
	}

	static
	private Value createValue(String value, Value.Property property){
		Value result = new Value(value);
		result.setProperty(property);

		return result;
	}
}