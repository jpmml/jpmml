/*
 * Copyright (c) 2013 University of Tartu
 */
package org.jpmml.evaluator;

import java.util.*;

import org.dmg.pmml.*;

public class LocalEvaluationContext extends EvaluationContext {

	public LocalEvaluationContext(){
		super(Collections.<FieldName, Object>emptyMap());
	}

	public LocalEvaluationContext(FieldName name, Object value){
		super(Collections.<FieldName, Object>singletonMap(name, value));
	}

	public LocalEvaluationContext(Map<FieldName, ?> arguments){
		super(arguments);
	}

	@Override
	public DerivedField resolveField(FieldName name){
		return null;
	}
}