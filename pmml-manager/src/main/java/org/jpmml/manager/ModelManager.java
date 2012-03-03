/*
 * Copyright (c) 2009 University of Tartu
 */
package org.jpmml.manager;

import java.util.*;

import org.dmg.pmml.*;

abstract
public class ModelManager<M extends Model> extends PMMLManager {

	public ModelManager(){
	}

	public ModelManager(PMML pmml){
		super(pmml);
	}

	/**
	 * @throws ModelManagerException If the Model does not exist
	 */
	abstract
	public M getModel();

	/**
	 * @throws EvaluationException If the evaluation failed.
	 */
	abstract
	public Object evaluate(Map<FieldName, ?> parameters);

	public void addField(FieldName name, String displayName, OpTypeType opType, DataTypeType dataType, FieldUsageTypeType fieldUsageType){
		addDataField(name, displayName, opType, dataType);
		addMiningField(name, fieldUsageType);
	}

	public List<FieldName> getFields(FieldUsageTypeType fieldUsageType){
		List<FieldName> result = new ArrayList<FieldName>();

		List<MiningField> miningFields = getMiningSchema().getMiningFields();
		for(MiningField miningField : miningFields){

			if((miningField.getUsageType()).equals(fieldUsageType)){
				result.add(miningField.getName());
			}
		}

		return result;
	}

	public MiningField getMiningField(FieldName name){
		List<MiningField> miningFields = getMiningSchema().getMiningFields();
		for(MiningField miningField : miningFields){

			if((miningField.getName()).equals(name)){
				return miningField;
			}
		}

		return null;
	}

	public MiningField addMiningField(FieldName name, FieldUsageTypeType usageType){
		MiningField miningField = new MiningField(name);
		miningField.setUsageType(usageType);

		List<MiningField> miningFields = getMiningSchema().getMiningFields();
		miningFields.add(miningField);

		return miningField;
	}

	public MiningSchema getMiningSchema(){
		return getModel().getMiningSchema();
	}

	static
	protected void ensureNull(Object object) throws ModelManagerException {

		if(object != null){
			throw new ModelManagerException();
		}
	}

	static
	protected void ensureNotNull(Object object) throws ModelManagerException {

		if(object == null){
			throw new ModelManagerException();
		}
	}

	static
	protected Object getParameterValue(Map<FieldName, ?> parameters, FieldName name){
		return getParameterValue(parameters, name, false);
	}

	static
	protected Object getParameterValue(Map<FieldName, ?> parameters, FieldName name, boolean nullable){
		Object value = parameters.get(name);

		if(value == null && !nullable){
			throw new EvaluationException("Missing parameter " + name.getValue());
		}

		return value;
	}
}