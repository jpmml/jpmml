/*
 * Copyright (c) 2013 University of Tartu
 */
package org.jpmml.evaluator;

import java.util.Map;

import org.dmg.pmml.*;

public class FunctionEvaluationContext extends EvaluationContext {

	private EvaluationContext parent = null;


	public FunctionEvaluationContext(EvaluationContext parent, Map<FieldName, FieldValue> arguments){
		setParent(parent);
		pushFrame(arguments);
	}

	@Override
	public DerivedField resolveField(FieldName name){
		// "The function body must not refer to fields other than the parameter fields"
		return null;
	}

	@Override
	public DefineFunction resolveFunction(String name){
		EvaluationContext parent = getParent();

		return parent.resolveFunction(name);
	}

	public EvaluationContext getParent(){
		return this.parent;
	}

	private void setParent(EvaluationContext parent){
		this.parent = parent;
	}
}