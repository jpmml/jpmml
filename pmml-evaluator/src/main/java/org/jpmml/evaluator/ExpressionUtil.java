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
	public Object getValue(FieldName name, ModelManager<?> modelManager, Map<FieldName, ?> parameters){

		// XXX: Should not accept <code>null</code> parameter
		if(modelManager != null){
			DerivedField derivedField = modelManager.resolve(name);

			if(derivedField != null){
				return getValue(derivedField, modelManager, parameters);
			}
		}

		return parameters.get(name);
	}

	static
	public Object getValue(DerivedField derivedField, ModelManager<?> modelManager, Map<FieldName, ?> parameters){
		return getValue(derivedField.getExpression(), modelManager, parameters);
	}

	static
	public Object getValue(Expression expression, ModelManager<?> modelManager, Map<FieldName, ?> parameters){

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

			return getValue(fieldRef.getField(), modelManager, parameters);
		} else

		if(expression instanceof NormContinuous){
			NormContinuous normContinuous = (NormContinuous)expression;

			Number value = (Number)getValue(normContinuous.getField(), modelManager, parameters);
			if(value == null){
				return normContinuous.getMapMissingTo();
			}

			return NormalizationUtil.normalize(normContinuous, value.doubleValue());
		} else

		if(expression instanceof NormDiscrete){
			NormDiscrete normDiscrete = (NormDiscrete)expression;

			Object value = getValue(normDiscrete.getField(), modelManager, parameters);
			if(value == null){
				return normDiscrete.getMapMissingTo();
			}

			DataType dataType = ParameterUtil.getDataType(value);

			boolean equals = (ParameterUtil.parse(dataType, normDiscrete.getValue())).equals(value);

			return Double.valueOf(equals ? 1.0 : 0.0);
		} else

		if(expression instanceof Apply){
			Apply apply = (Apply)expression;

			List<Object> values = new ArrayList<Object>();

			List<Expression> arguments = apply.getExpressions();
			for(Expression argument : arguments){
				Object value = getValue(argument, modelManager, parameters);

				values.add(value);
			}

			return FunctionUtil.evaluate(apply.getFunction(), values);
		}

		throw new UnsupportedFeatureException(expression);
	}
}