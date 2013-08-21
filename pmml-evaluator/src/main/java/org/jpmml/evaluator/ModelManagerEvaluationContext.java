/*
 * Copyright (c) 2013 University of Tartu
 */
package org.jpmml.evaluator;

import org.jpmml.manager.*;

import org.dmg.pmml.*;

public class ModelManagerEvaluationContext extends EvaluationContext {

	private ModelManager<?> modelManager = null;


	public ModelManagerEvaluationContext(ModelManager<?> modelManager){
		setModelManager(modelManager);
	}

	@Override
	public DerivedField resolveField(FieldName name){
		ModelManager<?> modelManager = getModelManager();

		return modelManager.resolveField(name);
	}

	@Override
	public DefineFunction resolveFunction(String name){
		ModelManager<?> modelManager = getModelManager();

		return modelManager.resolveFunction(name);
	}

	public ModelManager<?> getModelManager(){
		return this.modelManager;
	}

	private void setModelManager(ModelManager<?> modelManager){
		this.modelManager = modelManager;
	}
}