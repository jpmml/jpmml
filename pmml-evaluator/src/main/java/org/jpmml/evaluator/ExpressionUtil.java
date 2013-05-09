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

		if(expression instanceof FieldRef){
			FieldRef fieldRef = (FieldRef)expression;

			return getValue(fieldRef.getField(), modelManager, parameters);
		} else

		if(expression instanceof NormContinuous){
			NormContinuous normContinuous = (NormContinuous)expression;

			Number value = (Number)getValue(normContinuous.getField(), modelManager, parameters);

			return NormalizationUtil.normalize(normContinuous, value.doubleValue());
		} else

		if(expression instanceof NormDiscrete){
			NormDiscrete normDiscrete = (NormDiscrete)expression;

			String value = (String)getValue(normDiscrete.getField(), modelManager, parameters);

			return (normDiscrete.getValue()).equals(value) ? 1.0d : 0.0d;
		}

		throw new UnsupportedFeatureException(expression);
	}
}