/*
 * Copyright (c) 2013 University of Tartu
 */
package org.jpmml.evaluator;

import java.util.Map;

import org.dmg.pmml.*;

public class LocalEvaluationContext extends EvaluationContext {

	public LocalEvaluationContext(Map<FieldName, Object> arguments) {
		pushFrame(arguments);
	}

	@Override
	public DerivedField resolveField(FieldName name){
		return null;
	}

	@Override
	public DefineFunction resolveFunction(String name){
		return null;
	}
}