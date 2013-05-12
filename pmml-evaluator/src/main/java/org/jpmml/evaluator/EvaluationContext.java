/*
 * Copyright (c) 2013 University of Tartu
 */
package org.jpmml.evaluator;

import java.util.*;

import org.jpmml.manager.*;

import org.dmg.pmml.*;

public class EvaluationContext<M extends Model> {

	private ModelManager<M> modelManager = null;

	private Map<FieldName, ?> parameters = null;


	public EvaluationContext(ModelManager<M> modelManager, Map<FieldName, ?> parameters){
		setModelManager(modelManager);
		setParameters(parameters);
	}

	public DerivedField resolve(FieldName name){
		ModelManager<M> modelManager = getModelManager();

		return modelManager.resolve(name);
	}

	public Object getParameter(FieldName name){
		Map<FieldName, ?> parameters = getParameters();

		return parameters.get(name);
	}

	public ModelManager<M> getModelManager(){
		return this.modelManager;
	}

	private void setModelManager(ModelManager<M> modelManager){
		this.modelManager = modelManager;
	}

	public Map<FieldName, ?> getParameters(){
		return this.parameters;
	}

	private void setParameters(Map<FieldName, ?> parameters){
		this.parameters = parameters;
	}
}