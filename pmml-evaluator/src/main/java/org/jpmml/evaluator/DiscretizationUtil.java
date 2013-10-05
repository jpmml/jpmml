/*
 * Copyright (c) 2013 University of Tartu
 */
package org.jpmml.evaluator;

import java.util.*;

import org.jpmml.manager.*;

import org.dmg.pmml.*;

import com.google.common.collect.*;

public class DiscretizationUtil {

	private DiscretizationUtil(){
	}

	static
	public FieldValue discretize(Discretize discretize, FieldValue value){
		String result = discretize(discretize, (value.asNumber()).doubleValue());

		return FieldValueUtil.create(discretize.getDataType(), null, result);
	}

	static
	public String discretize(Discretize discretize, double value){
		List<DiscretizeBin> bins = discretize.getDiscretizeBins();

		for(DiscretizeBin bin : bins){
			Interval interval = bin.getInterval();

			if(contains(interval, value)){
				return bin.getBinValue();
			}
		}

		return discretize.getDefaultValue();
	}

	static
	public boolean contains(Interval interval, double value){
		Double left = interval.getLeftMargin();
		Double right = interval.getRightMargin();

		Interval.Closure closure = interval.getClosure();
		switch(closure){
			case OPEN_CLOSED:
				return greaterThan(value, left) && lessOrEqual(value, right);
			case OPEN_OPEN:
				return greaterThan(value, left) && lessThan(value, right);
			case CLOSED_OPEN:
				return greaterOrEqual(value, left) && lessThan(value, right);
			case CLOSED_CLOSED:
				return greaterOrEqual(value, left) && lessOrEqual(value, right);
			default:
				throw new UnsupportedFeatureException(interval, closure);
		}
	}

	static
	private boolean lessThan(double value, Double reference){
		return reference == null || Double.compare(value, reference) < 0;
	}

	static
	private boolean lessOrEqual(double value, Double reference){
		return reference == null || Double.compare(value, reference) <= 0;
	}

	static
	private boolean greaterThan(double value, Double reference){
		return reference == null || Double.compare(value, reference) > 0;
	}

	static
	private boolean greaterOrEqual(double value, Double reference){
		return reference == null || Double.compare(value, reference) >= 0;
	}

	static
	public FieldValue mapValue(MapValues mapValues, Map<String, FieldValue> values){
		DataType dataType = mapValues.getDataType();

		InlineTable inlineTable = mapValues.getInlineTable();
		if(inlineTable != null){
			Table<Integer, String, String> table = TableUtil.parse(inlineTable);

			Map<String, String> row = TableUtil.match(table, values);
			if(row != null){
				String result = row.get(mapValues.getOutputColumn());
				if(result == null){
					throw new EvaluationException(mapValues);
				}

				return FieldValueUtil.create(dataType, null, result);
			}
		}

		TableLocator tableLocator = mapValues.getTableLocator();
		if(tableLocator != null){
			throw new UnsupportedFeatureException(tableLocator);
		}

		return FieldValueUtil.create(dataType, null, mapValues.getDefaultValue());
	}
}