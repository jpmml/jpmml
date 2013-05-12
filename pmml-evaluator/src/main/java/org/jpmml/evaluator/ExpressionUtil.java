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
	public Object evaluate(FieldName name, EvaluationContext<?> context){
		DerivedField derivedField = context.resolve(name);
		if(derivedField != null){
			return evaluate(derivedField, context);
		}

		return context.getParameter(name);
	}

	static
	public Object evaluate(DerivedField derivedField, EvaluationContext<?> context){
		return evaluate(derivedField.getExpression(), context);
	}

	static
	public Object evaluate(Expression expression, EvaluationContext<?> context){

		if(expression instanceof Constant){
			Constant constant = (Constant)expression;

			String value = constant.getValue();

			DataType dataType = constant.getDataType();
			if(dataType == null){
				dataType = ParameterUtil.getDataType(value);
			}

			return ParameterUtil.parse(dataType, value);
		} else

		if(expression instanceof FieldRef){
			FieldRef fieldRef = (FieldRef)expression;

			return evaluate(fieldRef.getField(), context);
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

			DataType dataType = ParameterUtil.getDataType(value);

			boolean equals = (ParameterUtil.parse(dataType, normDiscrete.getValue())).equals(value);

			return Double.valueOf(equals ? 1.0 : 0.0);
		} else

		if(expression instanceof Discretize){
			Discretize discretize = (Discretize)expression;

			DataType dataType = discretize.getDataType();
			if(dataType == null){
				dataType = DataType.STRING; // XXX
			}

			Object value = evaluate(discretize.getField(), context);
			if(value == null){
				String missingValue = discretize.getMapMissingTo();
				if(missingValue != null){
					return ParameterUtil.parse(dataType, missingValue);
				}

				return null;
			}

			String result = DiscretizationUtil.discretize(discretize, value);

			return ParameterUtil.parse(dataType, result);
		} else

		if(expression instanceof MapValues){
			MapValues mapValues = (MapValues)expression;

			DataType dataType = mapValues.getDataType();
			if(dataType == null){
				dataType = DataType.STRING; // XXX
			}

			Map<String, Object> values = new LinkedHashMap<String, Object>();

			List<FieldColumnPair> fieldColumnPairs = mapValues.getFieldColumnPairs();
			for(FieldColumnPair fieldColumnPair : fieldColumnPairs){
				Object value = evaluate(fieldColumnPair.getField(), context);
				if(value == null){
					String missingValue = mapValues.getMapMissingTo();
					if(missingValue != null){
						return ParameterUtil.parse(dataType, missingValue);
					}

					return null;
				}

				values.put(fieldColumnPair.getColumn(), value);
			}

			String result = DiscretizationUtil.mapValue(mapValues, values);

			return ParameterUtil.parse(dataType, result);
		} else

		if(expression instanceof Apply){
			Apply apply = (Apply)expression;

			List<Object> values = new ArrayList<Object>();

			List<Expression> arguments = apply.getExpressions();
			for(Expression argument : arguments){
				Object value = evaluate(argument, context);

				values.add(value);
			}

			return FunctionUtil.evaluate(apply.getFunction(), values);
		}

		throw new UnsupportedFeatureException(expression);
	}
}