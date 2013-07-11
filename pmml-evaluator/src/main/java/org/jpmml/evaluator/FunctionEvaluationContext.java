/*
 * Copyright (c) 2013 University of Tartu
 */
package org.jpmml.evaluator;

import java.util.*;

import org.dmg.pmml.*;

public class FunctionEvaluationContext extends EvaluationContext {

	private EvaluationContext parent = null;


	public FunctionEvaluationContext(EvaluationContext parent, Map<FieldName, ?> arguments){
		super(arguments);

		setParent(parent);
	}

	@Override
	public DerivedField resolveField(FieldName name){
		// "The function body must not refer to fields other than the parameter fields"
		throw new EvaluationException();
	}

	@Override
	public DefineFunction resolveFunction(String name){
		return getParent().resolveFunction(name);
	}

	EvaluationContext getParent(){
		return this.parent;
	}

	private void setParent(EvaluationContext parent){
		this.parent = parent;
	}
}