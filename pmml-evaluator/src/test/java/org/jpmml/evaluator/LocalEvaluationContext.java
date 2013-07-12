/*
 * Copyright (c) 2013 University of Tartu
 */
package org.jpmml.evaluator;

import java.util.*;

import org.dmg.pmml.*;

public class LocalEvaluationContext extends EvaluationContext {

	public LocalEvaluationContext(){
		super();
	}

	public LocalEvaluationContext(FieldName name, Object value){
		this(Collections.<FieldName, Object>singletonMap(name, value));
	}

	public LocalEvaluationContext(Map<FieldName, ?> arguments){
		super(arguments);
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