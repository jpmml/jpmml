/*
 * Copyright (c) 2009 University of Tartu
 */
package org.jpmml.manager;

import java.util.*;

import org.dmg.pmml.*;

/**
 * Naming conventions for getter methods:
 * <ul>
 * <li><code>getXXX()</code> - Required schema elements. For example {@link #getDataDictionary()}
 * <li><code>getOrCreateXXX()</code> - Optional schema elements. When <code>null</code> then a new element instance is created. For example {@link #getOrCreateTransformationDictionary()}
 * </ul>
 */
public class PMMLManager {

	private PMML pmml = null;

	private TransformationDictionary transformationDictionary = null;


	public PMMLManager(){
		this(new PMML(new Header(), new DataDictionary(), "4.1"));
	}

	public PMMLManager(PMML pmml){
		setPmml(pmml);
	}

	public DataField getDataField(FieldName name){
		List<DataField> dataFields = getDataDictionary().getDataFields();

		return find(dataFields, name);
	}

	public DataField addDataField(FieldName name, String displayName, OpType opType, DataType dataType){
		DataField dataField = new DataField(name, opType, dataType);
		dataField.setDisplayName(displayName);

		List<DataField> dataFields = getDataDictionary().getDataFields();
		dataFields.add(dataField);

		return dataField;
	}

	public DerivedField resolve(FieldName name){
		TransformationDictionary transformationDictionary = getOrCreateTransformationDictionary();

		List<DerivedField> derivedFields = transformationDictionary.getDerivedFields();

		return find(derivedFields, name);
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

	public TransformationDictionary getOrCreateTransformationDictionary(){

		if(this.transformationDictionary == null){
			PMML pmml = getPmml();

			TransformationDictionary transformationDictionary = pmml.getTransformationDictionary();
			if(transformationDictionary == null){
				transformationDictionary = new TransformationDictionary();

				pmml.setTransformationDictionary(transformationDictionary);
			}

			this.transformationDictionary = transformationDictionary;
		}

		return this.transformationDictionary;
	}

	public DataField getOutputField(ModelManager<?> model) throws Exception {
		String outputVariableName = null;
		List<FieldName> predictedFields = model.getPredictedFields();
		// Get the predicted field. If there is none, it is an error.
		if (predictedFields != null && predictedFields.size() > 0) {
			outputVariableName = predictedFields.get(0).getValue();
		}
		if (outputVariableName == null) {
			throw new Exception("Predicted variable is not defined");
		}

		DataField outputField = model.getDataField(new FieldName(outputVariableName));
		if (outputField == null || outputField.getDataType() == null) {
			throw new Exception("Predicted variable [" +
					outputVariableName + "] does not have type defined");
		}

		return outputField;
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

	static
	public <E extends PMMLObject & HasName> E find(Collection<E> objects, FieldName name){

		for(E object : objects){

			if((object.getName()).equals(name)){
				return object;
			}
		}

		return null;
	}
}