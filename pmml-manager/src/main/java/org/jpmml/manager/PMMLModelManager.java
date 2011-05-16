/*
 * Copyright (c) 2009 University of Tartu
 */
package org.jpmml.manager;

import java.util.*;

import org.dmg.pmml.*;

abstract
public class PMMLModelManager<M extends PMMLModel> extends PMMLManager {

	public PMMLModelManager(){
	}

	public PMMLModelManager(PMML pmml){
		super(pmml);
	}

	abstract
	public M getOrCreateModel();

	/**
	 * @throws EvaluationException If the evaluation failed.
	 */
	abstract
	public Object evaluate(Map<FieldName, ?> parameters);

	public FieldName addField(FieldName name, String displayName, OpTypeType opType, DataTypeType dataType, FieldUsageTypeType fieldUsageType){
		addDataField(name, displayName, opType, dataType);
		addMiningField(name, fieldUsageType);

		return name;
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
		return getOrCreateModel().getMiningSchema();
	}

	static
	Object getParameterValue(Map<FieldName, ?> parameters, FieldName name){
		return getParameterValue(parameters, name, false);
	}

	static
	Object getParameterValue(Map<FieldName, ?> parameters, FieldName name, boolean nullable){
		Object value = parameters.get(name);

		if(value == null && !nullable){
			throw new EvaluationException("Missing parameter " + name.getValue());
		}

		return value;
	}

	static
	public List<PMMLModelManager<? extends PMMLModel>> getModelManagers(PMML pmml){
		return getModelManagers(pmml, new ModelManagerFactory());
	}

	static
	public List<PMMLModelManager<? extends PMMLModel>> getModelManagers(PMML pmml, ModelManagerFactory factory){
		List<PMMLModelManager<? extends PMMLModel>> result = new ArrayList<PMMLModelManager<? extends PMMLModel>>();

		List<PMMLModel> models = pmml.getContent();
		for(PMMLModel model : models){
			result.add(factory.getModelManager(pmml, model));
		}

		return result;
	}
}