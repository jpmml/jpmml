/*
 * Copyright (c) 2013 University of Tartu
 */
package org.jpmml.evaluator;

import java.util.*;

import org.jpmml.manager.*;

import org.dmg.pmml.*;

public class ExpressionUtil {

	private ExpressionUtil(){
	}

	static
	public Object evaluate(FieldName name, EvaluationContext context){
		DerivedField derivedField = context.resolve(name);
		if(derivedField != null){
			return evaluate(derivedField, context);
		}

		return context.getParameter(name);
	}

	static
	public Object evaluate(DerivedField derivedField, EvaluationContext context){
		Object value = evaluate(derivedField.getExpression(), context);

		DataType dataType = derivedField.getDataType();
		if(dataType != null){
			value = ParameterUtil.cast(dataType, value);
		}

		return value;
	}

	static
	public Object evaluate(Expression expression, EvaluationContext context){

		if(expression instanceof Constant){
			Constant constant = (Constant)expression;

			String value = constant.getValue();

			DataType dataType = constant.getDataType();
			if(dataType == null){
				dataType = ParameterUtil.getConstantDataType(value);
			}

			return ParameterUtil.parse(dataType, value);
		} else

		if(expression instanceof FieldRef){
			FieldRef fieldRef = (FieldRef)expression;

			Object value = evaluate(fieldRef.getField(), context);
			if(value == null){
				return fieldRef.getMapMissingTo();
			}

			return value;
		} else

		if(expression instanceof NormContinuous){
			NormContinuous normContinuous = (NormContinuous)expression;

			Number value = (Number)evaluate(normContinuous.getField(), context);
			if(value == null){
				return normContinuous.getMapMissingTo();
			}

			return NormalizationUtil.normalize(normContinuous, value.doubleValue());
		} else

		if(expression instanceof NormDiscrete){
			NormDiscrete normDiscrete = (NormDiscrete)expression;

			Object value = evaluate(normDiscrete.getField(), context);
			if(value == null){
				return normDiscrete.getMapMissingTo();
			}

			boolean equals = ParameterUtil.equals(value, normDiscrete.getValue());

			return Double.valueOf(equals ? 1.0 : 0.0);
		} else

		if(expression instanceof Discretize){
			Discretize discretize = (Discretize)expression;

			DataType dataType = discretize.getDataType();

			Object value = evaluate(discretize.getField(), context);
			if(value == null){
				return parseSafely(dataType, discretize.getMapMissingTo());
			}

			String result = DiscretizationUtil.discretize(discretize, value);

			return parseSafely(dataType, result);
		} else

		if(expression instanceof MapValues){
			MapValues mapValues = (MapValues)expression;

			DataType dataType = mapValues.getDataType();

			Map<String, Object> values = new LinkedHashMap<String, Object>();

			List<FieldColumnPair> fieldColumnPairs = mapValues.getFieldColumnPairs();
			for(FieldColumnPair fieldColumnPair : fieldColumnPairs){
				Object value = evaluate(fieldColumnPair.getField(), context);
				if(value == null){
					return parseSafely(dataType, mapValues.getMapMissingTo());
				}

				values.put(fieldColumnPair.getColumn(), value);
			}

			String result = DiscretizationUtil.mapValue(mapValues, values);

			return parseSafely(dataType, result);
		} else

		if(expression instanceof Apply){
			Apply apply = (Apply)expression;

			List<Object> values = new ArrayList<Object>();

			List<Expression> arguments = apply.getExpressions();
			for(Expression argument : arguments){
				Object value = evaluate(argument, context);

				values.add(value);
			}

			Object result = FunctionUtil.evaluate(apply.getFunction(), values);
			if(result == null){
				return apply.getMapMissingTo();
			}

			return result;
		}

		throw new UnsupportedFeatureException(expression);
	}

	static
	private Object parseSafely(DataType dataType, String value){

		if(value != null){

			if(dataType != null){
				return ParameterUtil.parse(dataType, value);
			}
		}

		return value;
	}
}