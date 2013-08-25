/*
 * Copyright (c) 2012 University of Tartu
 */
package org.jpmml.evaluator;

import java.util.*;

import org.jpmml.manager.*;

import org.dmg.pmml.*;
import org.dmg.pmml.Interval;

import com.google.common.collect.*;

import org.joda.time.*;
import org.joda.time.format.*;

public class ParameterUtil {

	private ParameterUtil(){
	}

	@SuppressWarnings (
		value = {"unused"}
	)
	static
	public Object prepare(DataField dataField, MiningField miningField, Object value){

		if(dataField == null){
			throw new InvalidFeatureException(dataField);
		}

		outlierTreatment:
		if(isOutlier(dataField, value)){

			if(miningField == null){
				throw new InvalidFeatureException(miningField);
			}

			OutlierTreatmentMethodType outlierTreatmentMethod = miningField.getOutlierTreatment();
			switch(outlierTreatmentMethod){
				case AS_IS:
					break;
				case AS_MISSING_VALUES:
					value = null;
					break;
				case AS_EXTREME_VALUES:
					{
						Double lowValue = miningField.getLowValue();
						Double highValue = miningField.getHighValue();

						if(lowValue == null || highValue == null){
							throw new InvalidFeatureException(miningField);
						}

						Double doubleValue = (Double)parseOrCast(DataType.DOUBLE, value);

						if(compare(DataType.DOUBLE, doubleValue, lowValue) < 0){
							value = lowValue;
						} else

						if(compare(DataType.DOUBLE, doubleValue, highValue) > 0){
							value = highValue;
						}
					}
					break;
				default:
					throw new UnsupportedFeatureException(miningField, outlierTreatmentMethod);
			}
		}

		missingValueTreatment:
		if(isMissing(dataField, value)){

			if(miningField == null){
				return null;
			}

			value = miningField.getMissingValueReplacement();
			if(value != null){
				break missingValueTreatment;
			}

			return null;
		} // End if

		invalidValueTreatment:
		if(isInvalid(dataField, value)){

			if(miningField == null){
				throw new InvalidFeatureException(miningField);
			}

			InvalidValueTreatmentMethodType invalidValueTreatmentMethod = miningField.getInvalidValueTreatment();
			switch(invalidValueTreatmentMethod){
				case RETURN_INVALID:
					throw new InvalidResultException(miningField);
				case AS_IS:
					break invalidValueTreatment;
				case AS_MISSING:
					{
						value = miningField.getMissingValueReplacement();
						if(value != null){
							break invalidValueTreatment;
						}

						return null;
					}
				default:
					throw new UnsupportedFeatureException(miningField, invalidValueTreatmentMethod);
			}
		}

		return parseOrCast(dataField.getDataType(), value);
	}

	static
	public boolean isOutlier(DataField dataField, Object value){

		if(value == null){
			return false;
		}

		OpType opType = dataField.getOptype();
		switch(opType){
			case CONTINUOUS:
				{
					List<Double> range = Lists.newArrayList();

					List<Interval> fieldIntervals = dataField.getIntervals();
					for(Interval fieldInterval : fieldIntervals){
						range.add(fieldInterval.getLeftMargin());
						range.add(fieldInterval.getRightMargin());
					}

					List<Value> fieldValues = dataField.getValues();
					for(Value fieldValue : fieldValues){
						Value.Property property = fieldValue.getProperty();

						switch(property){
							case VALID:
								range.add((Double)parseOrCast(DataType.DOUBLE, fieldValue.getValue()));
								break;
							default:
								break;
						}
					}

					if(range.isEmpty()){
						return false;
					}

					Double doubleValue = (Double)parseOrCast(DataType.DOUBLE, value);

					Double minValue = Collections.min(range);
					if(compare(DataType.DOUBLE, doubleValue, minValue) < 0){
						return true;
					}

					Double maxValue = Collections.max(range);
					if(compare(DataType.DOUBLE, doubleValue, maxValue) > 0){
						return true;
					}
				}
				break;
			case CATEGORICAL:
			case ORDINAL:
				break;
			default:
				throw new UnsupportedFeatureException(dataField, opType);
		}

		return false;
	}

	static
	public boolean isMissing(DataField dataField, Object value){

		if(value == null){
			return true;
		}

		// Compare as String. Missing values are often represented as String constants that cannot be parsed to runtime data type (eg. N/A).
		String stringValue = format(value);

		List<Value> fieldValues = dataField.getValues();
		for(Value fieldValue : fieldValues){
			Value.Property property = fieldValue.getProperty();

			switch(property){
				case MISSING:
					{
						boolean equals = equals(DataType.STRING, stringValue, fieldValue.getValue());
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

	static
	public boolean isInvalid(DataField dataField, Object value){

		if(value == null){
			return false;
		}

		return !isValid(dataField, value);
	}

	@SuppressWarnings (
		value = "fallthrough"
	)
	static
	public boolean isValid(DataField dataField, Object value){

		if(value == null){
			return false;
		}

		DataType dataType = dataField.getDataType();

		// Compare as runtime data type
		value = parseOrCast(dataType, value);

		OpType opType = dataField.getOptype();
		switch(opType){
			case CONTINUOUS:
				{
					Double doubleValue = (Double)ParameterUtil.cast(DataType.DOUBLE, value);

					int intervalCount = 0;

					List<Interval> fieldIntervals = dataField.getIntervals();
					for(Interval fieldInterval : fieldIntervals){
						intervalCount += 1;

						if(DiscretizationUtil.contains(fieldInterval, doubleValue)){
							return true;
						}
					}

					if(intervalCount > 0){
						return false;
					}
				}
				// Falls through
			case CATEGORICAL:
			case ORDINAL:
				{
					int validValueCount = 0;

					List<Value> fieldValues = dataField.getValues();
					for(Value fieldValue : fieldValues){
						Value.Property property = fieldValue.getProperty();

						switch(property){
							case VALID:
								{
									validValueCount += 1;

									boolean equals = equals(dataType, value, parseOrCast(dataType, fieldValue.getValue()));
									if(equals){
										return true;
									}
								}
								break;
							case INVALID:
								{
									boolean equals = equals(dataType, value, parseOrCast(dataType, fieldValue.getValue()));
									if(equals){
										return false;
									}
								}
								break;
							case MISSING:
								break;
							default:
								throw new UnsupportedFeatureException(fieldValue, property);
						}
					}

					if(validValueCount > 0){
						return false;
					}
				}
				break;
			default:
				throw new UnsupportedFeatureException(dataField, opType);
		}

		return true;
	}

	static
	public List<String> getValidValues(TypeDefinitionField field){
		List<Value> fieldValues = field.getValues();
		if(fieldValues.isEmpty()){
			return Collections.emptyList();
		}

		List<String> result = Lists.newArrayList();

		for(Value fieldValue : fieldValues){
			Value.Property property = fieldValue.getProperty();

			switch(property){
				case VALID:
					result.add(fieldValue.getValue());
					break;
				default:
					break;
			}
		}

		return result;
	}

	static
	public boolean equals(DataType dataType, Object left, Object right){
		return (cast(dataType, left)).equals(cast(dataType, right));
	}

	@SuppressWarnings (
		value = {"rawtypes", "unchecked"}
	)
	static
	public int compare(DataType dataType, Object left, Object right){
		return ((Comparable)cast(dataType, left)).compareTo(cast(dataType, right));
	}

	static
	public Object parseOrCast(DataType dataType, Object value){

		if(value instanceof String){
			String string = (String)value;

			return parse(dataType, string);
		}

		return cast(dataType, value);
	}

	static
	public Object parse(DataType dataType, String value){

		switch(dataType){
			case STRING:
				return value;
			case INTEGER:
				return Integer.valueOf(value);
			case FLOAT:
				return Float.valueOf(value);
			case DOUBLE:
				return Double.valueOf(value);
			case BOOLEAN:
				return Boolean.valueOf(value);
			case DATE:
				return parseDate(value);
			case TIME:
				return parseTime(value);
			case DATE_TIME:
				return parseDateTime(value);
			case DATE_DAYS_SINCE_1960:
				return new DaysSinceDate(YEAR_1960, parseDate(value));
			case DATE_DAYS_SINCE_1970:
				return new DaysSinceDate(YEAR_1970, parseDate(value));
			case DATE_DAYS_SINCE_1980:
				return new DaysSinceDate(YEAR_1980, parseDate(value));
			case TIME_SECONDS:
				return new SecondsSinceMidnight(parseSeconds(value));
			case DATE_TIME_SECONDS_SINCE_1960:
				return new SecondsSinceDate(YEAR_1960, parseDateTime(value));
			case DATE_TIME_SECONDS_SINCE_1970:
				return new SecondsSinceDate(YEAR_1970, parseDateTime(value));
			case DATE_TIME_SECONDS_SINCE_1980:
				return new SecondsSinceDate(YEAR_1980, parseDateTime(value));
			default:
				break;
		}

		throw new EvaluationException();
	}

	static
	private LocalDate parseDate(String value){
		return LocalDate.parse(value);
	}

	static
	private LocalTime parseTime(String value){
		return LocalTime.parse(value);
	}

	static
	private LocalDateTime parseDateTime(String value){
		return LocalDateTime.parse(value);
	}

	@SuppressWarnings (
		value = {"deprecation"}
	)
	static
	private Seconds parseSeconds(String value){
		DateTimeFormatter format = SecondsSinceMidnight.getFormat();

		DateTimeParser parser = format.getParser();

		DateTimeParserBucket bucket = new DateTimeParserBucket(0, null, null);
		bucket.setZone(null);

		int result = parser.parseInto(bucket, value, 0);
		if(result >= 0 && result >= value.length()){
			long millis = bucket.computeMillis(true);

			return Seconds.seconds((int)(millis / 1000L));
		}

		throw new IllegalArgumentException(value);
	}

	static
	public String format(Object value){

		if(value instanceof String){
			return (String)value;
		} // End if

		if(value != null){
			return String.valueOf(value);
		}

		throw new EvaluationException();
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
		} else

		if(value instanceof Boolean){
			return DataType.BOOLEAN;
		} else

		if(value instanceof LocalDate){
			return DataType.DATE;
		} else

		if(value instanceof LocalTime){
			return DataType.TIME;
		} else

		if(value instanceof LocalDateTime){
			return DataType.DATE_TIME;
		} else

		if(value instanceof DaysSinceDate){
			DaysSinceDate period = (DaysSinceDate)value;

			LocalDate epoch = period.getEpoch();

			if((epoch).equals(YEAR_1960)){
				return DataType.DATE_DAYS_SINCE_1960;
			} else

			if((epoch).equals(YEAR_1970)){
				return DataType.DATE_DAYS_SINCE_1970;
			} else

			if((epoch).equals(YEAR_1980)){
				return DataType.DATE_DAYS_SINCE_1980;
			}
		} else

		if(value instanceof SecondsSinceMidnight){
			return DataType.TIME_SECONDS;
		} else

		if(value instanceof SecondsSinceDate){
			SecondsSinceDate period = (SecondsSinceDate)value;

			LocalDate epoch = period.getEpoch();

			if((epoch).equals(YEAR_1960)){
				return DataType.DATE_TIME_SECONDS_SINCE_1960;
			} else

			if((epoch).equals(YEAR_1970)){
				return DataType.DATE_TIME_SECONDS_SINCE_1970;
			} else

			if((epoch).equals(YEAR_1980)){
				return DataType.DATE_TIME_SECONDS_SINCE_1980;
			}
		}

		throw new EvaluationException();
	}

	/**
	 * @return The least restrictive data type of the data types of two values
	 */
	static
	public DataType getResultDataType(DataType left, DataType right){

		if((left).equals(right)){
			return left;
		} // End if

		List<DataType> dataTypes = ParameterUtil.precedenceSequence;
		for(DataType dataType : dataTypes){

			if((dataType).equals(left) || (dataType).equals(right)){
				return dataType;
			}
		}

		throw new EvaluationException();
	}

	static
	public OpType getOpType(DataType dataType){

		switch(dataType){
			case STRING:
				return OpType.CATEGORICAL;
			case INTEGER:
			case FLOAT:
			case DOUBLE:
				return OpType.CONTINUOUS;
			case BOOLEAN:
				return OpType.CATEGORICAL;
			case DATE:
			case TIME:
			case DATE_TIME:
			case DATE_DAYS_SINCE_0:
			case DATE_DAYS_SINCE_1960:
			case DATE_DAYS_SINCE_1970:
			case DATE_DAYS_SINCE_1980:
			case DATE_TIME_SECONDS_SINCE_0:
			case DATE_TIME_SECONDS_SINCE_1960:
			case DATE_TIME_SECONDS_SINCE_1970:
			case DATE_TIME_SECONDS_SINCE_1980:
				return OpType.ORDINAL;
			default:
				break;
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
			case BOOLEAN:
				return toBoolean(value);
			case DATE:
				return toDate(value);
			case TIME:
				return toTime(value);
			case DATE_TIME:
				return toDateTime(value);
			case DATE_DAYS_SINCE_1960:
				return toDaysSinceDate(value, YEAR_1960);
			case DATE_DAYS_SINCE_1970:
				return toDaysSinceDate(value, YEAR_1970);
			case DATE_DAYS_SINCE_1980:
				return toDaysSinceDate(value, YEAR_1980);
			case TIME_SECONDS:
				return toSecondsSinceMidnight(value);
			case DATE_TIME_SECONDS_SINCE_1960:
				return toSecondsSinceDate(value, YEAR_1960);
			case DATE_TIME_SECONDS_SINCE_1970:
				return toSecondsSinceDate(value, YEAR_1970);
			case DATE_TIME_SECONDS_SINCE_1980:
				return toSecondsSinceDate(value, YEAR_1980);
			default:
				break;
		}

		throw new EvaluationException();
	}

	/**
	 * Casts the specified value to String data type.
	 *
	 * @see DataType#STRING
	 */
	static
	private String toString(Object value){

		if(value instanceof String){
			return (String)value;
		} else

		if((value instanceof Double) || (value instanceof Float) || (value instanceof Integer)){
			Number number = (Number)value;

			return number.toString();
		}

		throw new EvaluationException();
	}

	/**
	 * Casts the specified value to Integer data type.
	 *
	 * @see DataType#INTEGER
	 */
	static
	private Integer toInteger(Object value){

		if(value instanceof Integer){
			return (Integer)value;
		}

		throw new EvaluationException();
	}

	/**
	 * Casts the specified value to Float data type.
	 *
	 * @see DataType#FLOAT
	 */
	static
	private Float toFloat(Object value){

		if(value instanceof Float){
			return (Float)value;
		} else

		if(value instanceof Integer){
			Number number = (Number)value;

			return Float.valueOf(number.floatValue());
		}

		throw new EvaluationException();
	}

	/**
	 * Casts the specified value to Double data type.
	 *
	 * @see DataType#DOUBLE
	 */
	static
	private Double toDouble(Object value){

		if(value instanceof Double){
			return (Double)value;
		} else

		if((value instanceof Float) || (value instanceof Integer)){
			Number number = (Number)value;

			return Double.valueOf(number.doubleValue());
		}

		throw new EvaluationException();
	}

	/**
	 * @see DataType#BOOLEAN
	 */
	static
	private Boolean toBoolean(Object value){

		if(value instanceof Boolean){
			return (Boolean)value;
		}

		throw new EvaluationException();
	}

	/**
	 * @see DataType#DATE
	 */
	static
	private LocalDate toDate(Object value){

		if(value instanceof LocalDate){
			return (LocalDate)value;
		}

		throw new EvaluationException();
	}

	/**
	 * @see DataType#TIME
	 */
	static
	private LocalTime toTime(Object value){

		if(value instanceof LocalTime){
			return (LocalTime)value;
		}

		throw new EvaluationException();
	}

	/**
	 * @see DataType#DATE_TIME
	 */
	static
	private LocalDateTime toDateTime(Object value){

		if(value instanceof LocalDateTime){
			return (LocalDateTime)value;
		}

		throw new EvaluationException();
	}

	/**
	 * @see DataType#DATE_DAYS_SINCE_1960
	 * @see DataType#DATE_DAYS_SINCE_1970
	 * @see DataType#DATE_DAYS_SINCE_1980
	 */
	static
	private DaysSinceDate toDaysSinceDate(Object value, LocalDate epoch){

		if(value instanceof DaysSinceDate){
			DaysSinceDate period = (DaysSinceDate)value;

			if((period.getEpoch()).equals(epoch)){
				return period;
			}
		}

		throw new EvaluationException();
	}

	/**
	 * @see DataType#TIME_SECONDS
	 */
	static
	private SecondsSinceMidnight toSecondsSinceMidnight(Object value){

		if(value instanceof SecondsSinceMidnight){
			return (SecondsSinceMidnight)value;
		}

		throw new EvaluationException();
	}

	/**
	 * @see DataType#DATE_TIME_SECONDS_SINCE_1960
	 * @see DataType#DATE_TIME_SECONDS_SINCE_1970
	 * @see DataType#DATE_TIME_SECONDS_SINCE_1980
	 */
	static
	private SecondsSinceDate toSecondsSinceDate(Object value, LocalDate epoch){

		if(value instanceof SecondsSinceDate){
			SecondsSinceDate period = (SecondsSinceDate)value;

			if((period.getEpoch()).equals(epoch)){
				return period;
			}
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

	private static final List<DataType> precedenceSequence = Arrays.asList(DataType.STRING, DataType.DOUBLE, DataType.FLOAT, DataType.INTEGER);

	private static final LocalDate YEAR_1960 = new LocalDate(1960, 1, 1);
	private static final LocalDate YEAR_1970 = new LocalDate(1970, 1, 1);
	private static final LocalDate YEAR_1980 = new LocalDate(1980, 1, 1);
}