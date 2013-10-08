/*
 * Copyright, KNIME.com AG, Zurich, Switzerland
 */
package org.jpmml.evaluator;

import org.jpmml.manager.*;

import org.dmg.pmml.*;

import com.google.common.cache.*;

abstract
public class ModelEvaluator<M extends Model> extends ModelManager<M> implements Evaluator {

	public ModelEvaluator(PMML pmml, M model){
		super(pmml, model);
	}

	@Override
	public FieldValue prepare(FieldName name, Object value){
		return ArgumentUtil.prepare(getDataField(name), getMiningField(name), value);
	}

	public <V> V getValue(LoadingCache<M, V> cache){
		M model = getModel();

		return CacheUtil.getValue(model, cache);
	}
}