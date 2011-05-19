/*
 * Copyright (c) 2009 University of Tartu
 */
package org.jpmml.manager;

import java.util.*;

import org.dmg.pmml.*;

public class PMMLManager {

	private PMML pmml = null;


	public PMMLManager(){
		this(new PMML(new Header(""), new DataDictionary(), "3.2"));
	}

	public PMMLManager(PMML pmml){
		setPmml(pmml);
	}

	public DataField getDataField(FieldName name){
		List<DataField> dataFields = getDataDictionary().getDataFields();
		for(DataField dataField : dataFields){

			if((dataField.getName()).equals(name)){
				return dataField;
			}
		}

		return null;
	}

	public DataField addDataField(FieldName name, String displayName, OpTypeType opType, DataTypeType dataType){
		DataField dataField = new DataField(name, opType, dataType);
		dataField.setDisplayName(displayName);

		List<DataField> dataFields = getDataDictionary().getDataFields();
		dataFields.add(dataField);

		return dataField;
	}

	public PMML getPmml(){
		return this.pmml;
	}

	private void setPmml(PMML pmml){
		this.pmml = pmml;
	}

	public Header getHeader(){
		return getPmml().getHeader();
	}

	public DataDictionary getDataDictionary(){
		return getPmml().getDataDictionary();
	}

	public List<Model> getModels(){
		return getPmml().getContent();
	}

	/**
	 * @param modelName The name of the Model to be selected. If <code>null</code>, the first model is selected.
	 *
	 * @see Model#getModelName()
	 */
	public Model getModel(String modelName){
		List<Model> models = getModels();

		if(modelName != null){

			for(Model model : models){

				if(modelName.equals(model.getModelName())){
					return model;
				}
			}

			return null;
		} // End if

		if(models.size() > 0){
			return models.get(0);
		}

		return null;
	}

	public ModelManager<? extends Model> getModelManager(String modelName){
		return getModelManager(modelName, ModelManagerFactory.getInstance());
	}

	public ModelManager<? extends Model> getModelManager(String modelName, ModelManagerFactory modelManagerFactory){
		Model model = getModel(modelName);

		return modelManagerFactory.getModelManager(getPmml(), model);
	}

	@SuppressWarnings (
		value = {"unchecked"}
	)
	static
	public <E extends PMMLObject> E find(List<? extends PMMLObject> objects, Class<? extends E> clazz){

		for(PMMLObject object : objects){

			if(object.getClass().equals(clazz)){
				return (E)object;
			}
		}

		return null;
	}

	@SuppressWarnings (
		value = {"unchecked"}
	)
	static
	public <E extends PMMLObject> List<E> findAll(List<? extends PMMLObject> objects, Class<? extends E> clazz){
		List<E> result = new ArrayList<E>();

		for(PMMLObject object : objects){

			if(object.getClass().equals(clazz)){
				result.add((E)object);
			}
		}

		return result;
	}
}