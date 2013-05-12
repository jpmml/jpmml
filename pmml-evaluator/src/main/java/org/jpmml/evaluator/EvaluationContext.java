/*
 * Copyright (c) 2013 University of Tartu
 */
package org.jpmml.evaluator;

import java.util.*;

import org.dmg.pmml.*;

abstract
public class EvaluationContext {

	private Map<FieldName, ?> parameters = null;


	public EvaluationContext(Map<FieldName, ?> parameters){
		setParameters(parameters);
	}

	abstract
	public DerivedField resolve(FieldName name);

	public Object getParameter(FieldName name){
		Map<FieldName, ?> parameters = getParameters();

		return parameters.get(name);
	}

	public Map<FieldName, ?> getParameters(){
		return this.parameters;
	}

	private void setParameters(Map<FieldName, ?> parameters){
		this.parameters = parameters;
	}
}