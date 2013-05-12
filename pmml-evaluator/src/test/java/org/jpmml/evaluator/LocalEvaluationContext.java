/*
 * Copyright (c) 2013 University of Tartu
 */
package org.jpmml.evaluator;

import java.util.*;

import org.jpmml.manager.*;

import org.dmg.pmml.*;

public class LocalEvaluationContext<M extends Model> extends EvaluationContext<M> {

	public LocalEvaluationContext(){
		this((ModelManager<M>)null);
	}

	public LocalEvaluationContext(ModelManager<M> modelManager){
		super(modelManager, Collections.<FieldName, Object>emptyMap());
	}

	public LocalEvaluationContext(FieldName name, Object value){
		this((ModelManager<M>)null, name, value);
	}

	public LocalEvaluationContext(ModelManager<M> modelManager, FieldName name, Object value){
		super(modelManager, Collections.<FieldName, Object>singletonMap(name, value));
	}

	public LocalEvaluationContext(Map<FieldName, ?> parameters){
		this((ModelManager<M>)null, parameters);
	}

	public LocalEvaluationContext(ModelManager<M> modelManager, Map<FieldName, ?> parameters){
		super(modelManager, parameters);
	}

	@Override
	public DerivedField resolve(FieldName name){
		ModelManager<M> modelManager = getModelManager();
		if(modelManager == null){
			return null;
		}

		return super.resolve(name);
	}
}