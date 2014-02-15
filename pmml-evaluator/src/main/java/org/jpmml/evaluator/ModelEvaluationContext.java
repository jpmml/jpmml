/*
 * Copyright (c) 2013 University of Tartu
 */
package org.jpmml.evaluator;

import java.util.*;

import org.jpmml.manager.*;

import org.dmg.pmml.*;

public class ModelEvaluationContext extends EvaluationContext {

	private ModelManager<?> modelManager = null;

	private ModelEvaluationContext parent = null;


	public ModelEvaluationContext(ModelManager<?> modelManager){
		this(modelManager, null);
	}

	public ModelEvaluationContext(ModelManager<?> modelManager, ModelEvaluationContext parent){
		setModelManager(modelManager);
		setParent(parent);
	}

	@Override
	public FieldValue getArgument(FieldName name){
		ModelEvaluationContext parent = getParent();
		if(parent != null){
			return parent.getArgument(name);
		}

		return super.getArgument(name);
	}

	@Override
	public Map.Entry<FieldName, FieldValue> getArgumentEntry(FieldName name){
		ModelEvaluationContext parent = getParent();
		if(parent != null){
			return parent.getArgumentEntry(name);
		}

		return super.getArgumentEntry(name);
	}

	@Override
	public DerivedField resolveDerivedField(FieldName name){
		ModelManager<?> modelManager = getModelManager();

		DerivedField derivedField = modelManager.getLocalDerivedField(name);
		if(derivedField == null){
			ModelEvaluationContext parent = getParent();
			if(parent != null){
				return parent.resolveDerivedField(name);
			}

			return modelManager.getDerivedField(name);
		}

		return derivedField;
	}

	@Override
	public DefineFunction resolveFunction(String name){
		ModelManager<?> modelManager = getModelManager();

		return modelManager.getFunction(name);
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

	public ModelEvaluationContext getParent(){
		return this.parent;
	}

	private void setParent(ModelEvaluationContext parent){
		this.parent = parent;
	}
}