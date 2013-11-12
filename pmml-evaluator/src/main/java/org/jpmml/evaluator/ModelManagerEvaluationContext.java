/*
 * Copyright (c) 2013 University of Tartu
 */
package org.jpmml.evaluator;

import java.util.Map;

import org.jpmml.manager.*;

import org.dmg.pmml.*;

public class ModelManagerEvaluationContext extends EvaluationContext {

	private ModelManager<?> modelManager = null;


	public ModelManagerEvaluationContext(ModelManager<?> modelManager, Map<FieldName, ?> arguments){
		setModelManager(modelManager);
		pushFrame(arguments);
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

	@Override
	public FieldValue createFieldValue(FieldName name, Object value){
		ModelManager<?> modelManager = getModelManager();

		DataField dataField = modelManager.getDataField(name);
		if(dataField != null){
			return FieldValueUtil.create(dataField, value);
		}

		return super.createFieldValue(name, value);
	}

	public ModelManager<?> getModelManager(){
		return this.modelManager;
	}

	private void setModelManager(ModelManager<?> modelManager){
		this.modelManager = modelManager;
	}
}