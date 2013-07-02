/*
 * Copyright (c) 2013 University of Tartu
 */
package org.jpmml.evaluator;

import java.util.*;

import org.dmg.pmml.*;

abstract
public class EvaluationContext implements Cloneable {

	private Map<FieldName, ?> arguments = null;


	public EvaluationContext(Map<FieldName, ?> arguments){
		setArguments(arguments);
	}

	abstract
	public DerivedField resolve(FieldName name);

	@Override
	public EvaluationContext clone(){
		try {
			EvaluationContext result = (EvaluationContext)super.clone();

			// Deep copy arguments
			Map<FieldName, Object> arguments = new LinkedHashMap<FieldName, Object>(getArguments());
			result.setArguments(arguments);

			return result;
		} catch(CloneNotSupportedException cnse){
			throw new AssertionError(cnse);
		}
	}

	public Object getArgument(FieldName name){
		Map<FieldName, ?> arguments = getArguments();

		return arguments.get(name);
	}

	@SuppressWarnings (
		value = {"unchecked"}
	)
	void putArgument(FieldName name, Object value){
		// Use cast to remove the implicit "read-only" protection
		Map<FieldName, Object> arguments = (Map<FieldName, Object>)getArguments();

		arguments.put(name, value);
	}

	public Map<FieldName, ?> getArguments(){
		return this.arguments;
	}

	void setArguments(Map<FieldName, ?> arguments){
		this.arguments = arguments;
	}
}