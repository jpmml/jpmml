/*
 * Copyright (c) 2013 University of Tartu
 */
package org.jpmml.evaluator;

import java.util.*;

import org.dmg.pmml.*;
import org.dmg.pmml.Interval.Closure;
import org.dmg.pmml.Value.Property;
import org.dmg.pmml.Interval;

import org.junit.*;

import com.google.common.collect.*;

import org.joda.time.*;

import static org.junit.Assert.*;

public class ParameterUtilTest {

	@Test
	public void prepare(){
		FieldName name = new FieldName("x");

		DataField dataField = new DataField(name, OpType.CONTINUOUS, DataType.DOUBLE);

		List<Value> fieldValues = dataField.getValues();
		List<Interval> fieldIntervals = dataField.getIntervals();

		MiningField miningField = new MiningField(name);

		miningField.setLowValue(1d);
		miningField.setHighValue(3d);

		assertEquals(1d, ParameterUtil.prepare(dataField, miningField, "1"));
		assertEquals(1d, ParameterUtil.prepare(dataField, miningField, 1));
		assertEquals(1d, ParameterUtil.prepare(dataField, miningField, 1f));
		assertEquals(1d, ParameterUtil.prepare(dataField, miningField, 1d));

		Value missingValue = createValue("N/A", Property.MISSING);

		fieldValues.add(missingValue);

		assertEquals(null, ParameterUtil.prepare(dataField, miningField, null));
		assertEquals(null, ParameterUtil.prepare(dataField, miningField, "N/A"));

		miningField.setMissingValueReplacement("0");

		assertEquals(0d, ParameterUtil.prepare(dataField, miningField, null));
		assertEquals(0d, ParameterUtil.prepare(dataField, miningField, "N/A"));

		fieldValues.clear();
		fieldIntervals.clear();

		fieldValues.add(missingValue);

		Interval validInterval = new Interval(Closure.CLOSED_CLOSED);
		validInterval.setLeftMargin(1d);
		validInterval.setRightMargin(3d);

		fieldIntervals.add(validInterval);

		miningField.setOutlierTreatment(OutlierTreatmentMethodType.AS_IS);
		miningField.setInvalidValueTreatment(InvalidValueTreatmentMethodType.AS_IS);

		assertEquals(-1d, ParameterUtil.prepare(dataField, miningField, -1d));
		assertEquals(1d, ParameterUtil.prepare(dataField, miningField, 1d));
		assertEquals(5d, ParameterUtil.prepare(dataField, miningField, 5d));

		miningField.setOutlierTreatment(OutlierTreatmentMethodType.AS_EXTREME_VALUES);
		miningField.setInvalidValueTreatment(InvalidValueTreatmentMethodType.AS_IS);

		assertEquals(1d, ParameterUtil.prepare(dataField, miningField, -1d));
		assertEquals(1d, ParameterUtil.prepare(dataField, miningField, 1d));
		assertEquals(3d, ParameterUtil.prepare(dataField, miningField, 5d));

		miningField.setOutlierTreatment(OutlierTreatmentMethodType.AS_IS);
		miningField.setInvalidValueTreatment(InvalidValueTreatmentMethodType.AS_MISSING);

		assertEquals(1d, ParameterUtil.prepare(dataField, miningField, 1d));
		assertEquals(0d, ParameterUtil.prepare(dataField, miningField, 5d));

		fieldValues.clear();
		fieldIntervals.clear();

		List<Value> validValues = Lists.newArrayList();
		validValues.add(createValue("1", Value.Property.VALID));
		validValues.add(createValue("2", Value.Property.VALID));
		validValues.add(createValue("3", Value.Property.VALID));

		fieldValues.add(missingValue);
		fieldValues.addAll(validValues);

		miningField.setInvalidValueTreatment(InvalidValueTreatmentMethodType.AS_IS);

		assertEquals(1d, ParameterUtil.prepare(dataField, miningField, 1d));
		assertEquals(5d, ParameterUtil.prepare(dataField, miningField, 5d));

		miningField.setInvalidValueTreatment(InvalidValueTreatmentMethodType.AS_MISSING);

		assertEquals(1d, ParameterUtil.prepare(dataField, miningField, 1d));
		assertEquals(0d, ParameterUtil.prepare(dataField, miningField, 5d));

		fieldValues.clear();
		fieldIntervals.clear();

		List<Value> invalidValues = Lists.newArrayList();
		invalidValues.add(createValue("1", Value.Property.INVALID));

		fieldValues.add(missingValue);
		fieldValues.addAll(invalidValues);

		miningField.setInvalidValueTreatment(InvalidValueTreatmentMethodType.AS_IS);

		assertEquals(1d, ParameterUtil.prepare(dataField, miningField, 1d));
		assertEquals(5d, ParameterUtil.prepare(dataField, miningField, 5d));

		miningField.setInvalidValueTreatment(InvalidValueTreatmentMethodType.AS_MISSING);

		assertEquals(0d, ParameterUtil.prepare(dataField, miningField, 1d));
		assertEquals(5d, ParameterUtil.prepare(dataField, miningField, 5d));
	}

	@Test
	public void isInvalid(){
		assertFalse(ParameterUtil.isInvalid(null, null));
	}

	@Test
	public void isValid(){
		assertFalse(ParameterUtil.isValid(null, null));
	}

	@Test
	public void equals(){
		assertTrue(ParameterUtil.equals("1", "1"));

		assertTrue(ParameterUtil.equals(1, "1"));

		assertTrue(ParameterUtil.equals(1f, "1"));
		assertTrue(ParameterUtil.equals(1.0f, "1"));
		assertTrue(ParameterUtil.equals(1f, "1.0"));

		assertTrue(ParameterUtil.equals(1d, "1"));
		assertTrue(ParameterUtil.equals(1.0d, "1"));
		assertTrue(ParameterUtil.equals(1d, "1.0"));
	}

	@Test
	public void compare(){
		assertTrue(ParameterUtil.compare("1", "1") == 0);

		assertTrue(ParameterUtil.compare(1, "1") == 0);

		assertTrue(ParameterUtil.compare(1f, "1") == 0);
		assertTrue(ParameterUtil.compare(1.0f, "1") == 0);
		assertTrue(ParameterUtil.compare(1f, "1.0") == 0);

		assertTrue(ParameterUtil.compare(1d, "1") == 0);
		assertTrue(ParameterUtil.compare(1.0d, "1") == 0);
		assertTrue(ParameterUtil.compare(1d, "1.0") == 0);
	}

	@Test
	public void compareDateTime(){
		assertTrue(ParameterUtil.compare(DataType.DATE, parseDate("1960-01-01"), parseDate("1960-01-01")) == 0);
		assertTrue(ParameterUtil.compare(DataType.TIME, parseTime("00:00:00"), parseTime("00:00:00")) == 0);
		assertTrue(ParameterUtil.compare(DataType.DATE_TIME, parseDateTime("1960-01-01T00:00:00"), parseDateTime("1960-01-01T00:00:00")) == 0);

		assertTrue(ParameterUtil.compare(DataType.DATE_DAYS_SINCE_1960, parseDaysSince1960("1960-01-01"), parseDaysSince1960("1960-01-01")) == 0);
		assertTrue(ParameterUtil.compare(DataType.TIME_SECONDS, parseSecondsSinceMidnight("00:00:00"), parseSecondsSinceMidnight("00:00:00")) == 0);
		assertTrue(ParameterUtil.compare(DataType.DATE_TIME_SECONDS_SINCE_1960, parseSecondsSince1960("1960-01-01T00:00:00"), parseSecondsSince1960("1960-01-01T00:00:00")) == 0);
	}

	@Test
	public void parseDaysSinceDate(){
		assertEquals(0, countDaysSince1960("1960-01-01"));
		assertEquals(1, countDaysSince1960("1960-01-02"));
		assertEquals(31, countDaysSince1960("1960-02-01"));

		assertEquals(-1, countDaysSince1960("1959-12-31"));
	}

	@Test
	public void parseSecondsSinceMidnight(){
		assertEquals(0, countSecondsSinceMidnight("0:00:00"));
		assertEquals(100, countSecondsSinceMidnight("0:01:40"));
		assertEquals(200, countSecondsSinceMidnight("0:03:20"));
		assertEquals(1000, countSecondsSinceMidnight("0:16:40"));
		assertEquals(86400, countSecondsSinceMidnight("24:00:00"));
		assertEquals(86401, countSecondsSinceMidnight("24:00:01"));
		assertEquals(100000, countSecondsSinceMidnight("27:46:40"));
	}

	@Test
	public void parseSecondsSinceDate(){
		assertEquals(0, countSecondsSince1960("1960-01-01T00:00:00"));
		assertEquals(1, countSecondsSince1960("1960-01-01T00:00:01"));
		assertEquals(60, countSecondsSince1960("1960-01-01T00:01:00"));

		assertEquals(-1, countSecondsSince1960("1959-12-31T23:59:59"));
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

	static
	private LocalDate parseDate(String string){
		return (LocalDate)ParameterUtil.parse(DataType.DATE, string);
	}

	static
	private LocalTime parseTime(String string){
		return (LocalTime)ParameterUtil.parse(DataType.TIME, string);
	}

	static
	private LocalDateTime parseDateTime(String string){
		return (LocalDateTime)ParameterUtil.parse(DataType.DATE_TIME, string);
	}

	static
	private int countDaysSince1960(String string){
		DaysSinceDate period = parseDaysSince1960(string);

		return period.intValue();
	}

	static
	private DaysSinceDate parseDaysSince1960(String string){
		return (DaysSinceDate)ParameterUtil.parse(DataType.DATE_DAYS_SINCE_1960, string);
	}

	static
	private int countSecondsSinceMidnight(String string){
		SecondsSinceMidnight period = parseSecondsSinceMidnight(string);

		return period.intValue();
	}

	static
	private SecondsSinceMidnight parseSecondsSinceMidnight(String string){
		return (SecondsSinceMidnight)ParameterUtil.parse(DataType.TIME_SECONDS, string);
	}

	static
	private int countSecondsSince1960(String string){
		SecondsSinceDate period = parseSecondsSince1960(string);

		return period.intValue();
	}

	static
	private SecondsSinceDate parseSecondsSince1960(String string){
		return (SecondsSinceDate)ParameterUtil.parse(DataType.DATE_TIME_SECONDS_SINCE_1960, string);
	}
}