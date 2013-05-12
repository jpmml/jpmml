/*
 * Copyright (c) 2013 University of Tartu
 */
package org.jpmml.evaluator;

import java.util.*;

import org.jpmml.manager.*;

import org.dmg.pmml.*;

public class DiscretizationUtil {

	private DiscretizationUtil(){
	}

	static
	public String discretize(Discretize discretize, Object value){
		Double doubleValue = ParameterUtil.toDouble(value);

		List<DiscretizeBin> bins = discretize.getDiscretizeBins();
		for(DiscretizeBin bin : bins){
			Interval interval = bin.getInterval();

			if(contains(interval, doubleValue)){
				return bin.getBinValue();
			}
		}

		return discretize.getDefaultValue();
	}

	static
	public boolean contains(Interval interval, Double value){
		Double left = interval.getLeftMargin();
		Double right = interval.getRightMargin();

		Interval.Closure closure = interval.getClosure();
		switch(closure){
			case OPEN_CLOSED:
				return greaterThan(left, value) && lessOrEqual(right, value);
			case OPEN_OPEN:
				return greaterThan(left, value) && lessThan(right, value);
			case CLOSED_OPEN:
				return greaterOrEqual(left, value) && lessThan(right, value);
			case CLOSED_CLOSED:
				return greaterOrEqual(left, value) && lessOrEqual(right, value);
			default:
				throw new UnsupportedFeatureException(closure);
		}
	}

	static
	private boolean lessThan(Double reference, Double value){
		return (reference != null ? (value).compareTo(reference) < 0 : true);
	}

	static
	private boolean lessOrEqual(Double reference, Double value){
		return (reference != null ? (value).compareTo(reference) <= 0 : true);
	}

	static
	private boolean greaterThan(Double reference, Double value){
		return (reference != null ? (value).compareTo(reference) > 0 : true);
	}

	static
	private boolean greaterOrEqual(Double reference, Double value){
		return (reference != null ? (value).compareTo(reference) >= 0 : true);
	}

	static
	public String mapValue(MapValues mapValues, Map<String, Object> values){
		InlineTable table = mapValues.getInlineTable();

		if(table != null){
			List<Map<String, String>> rows = TableUtil.parse(table);

			Map<String, String> row = TableUtil.match(rows, values);
			if(row != null){
				String result = row.get(mapValues.getOutputColumn());
				if(result == null){
					throw new EvaluationException();
				}

				return result;
			}
		} else

		{
			TableLocator tableLocator = mapValues.getTableLocator();
			if(tableLocator != null){
				throw new UnsupportedFeatureException(tableLocator);
			}
		}

		return mapValues.getDefaultValue();
	}
}