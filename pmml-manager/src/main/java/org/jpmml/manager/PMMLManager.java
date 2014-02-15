/*
 * Copyright (c) 2009 University of Tartu
 */
package org.jpmml.manager;

import java.io.*;
import java.util.*;

import org.dmg.pmml.*;

import com.google.common.collect.*;

import static com.google.common.base.Preconditions.*;

/**
 * Naming conventions for getter methods:
 * <ul>
 * <li><code>getXXX()</code> - Required schema elements. For example {@link #getDataDictionary()}
 * <li><code>getOrCreateXXX()</code> - Optional schema elements. When <code>null</code> then a new element instance is created. For example {@link #getOrCreateTransformationDictionary()}
 * </ul>
 */
public class PMMLManager implements Serializable {

	private PMML pmml = null;


	public PMMLManager(PMML pmml){
		setPMML(pmml);
	}

	public DataField getDataField(FieldName name){
		DataDictionary dataDictionary = getDataDictionary();

		List<DataField> dataFields = dataDictionary.getDataFields();

		return find(dataFields, name);
	}

	public DerivedField getDerivedField(FieldName name){
		TransformationDictionary transformationDictionary = getOrCreateTransformationDictionary();

		List<DerivedField> derivedFields = transformationDictionary.getDerivedFields();

		return find(derivedFields, name);
	}

	public DefineFunction getFunction(String name){
		TransformationDictionary transformationDictionary = getOrCreateTransformationDictionary();

		List<DefineFunction> defineFunctions = transformationDictionary.getDefineFunctions();
		for(DefineFunction defineFunction : defineFunctions){

			if((defineFunction.getName()).equals(name)){
				return defineFunction;
			}
		}

		return null;
	}

	public PMML getPMML(){
		return this.pmml;
	}

	private void setPMML(PMML pmml){
		checkNotNull(pmml);

		this.pmml = pmml;
	}

	public Header getHeader(){
		PMML pmml = getPMML();

		return pmml.getHeader();
	}

	public DataDictionary getDataDictionary(){
		PMML pmml = getPMML();

		return pmml.getDataDictionary();
	}

	public TransformationDictionary getOrCreateTransformationDictionary(){
		PMML pmml = getPMML();

		TransformationDictionary transformationDictionary = pmml.getTransformationDictionary();
		if(transformationDictionary == null){
			transformationDictionary = new TransformationDictionary();

			pmml.setTransformationDictionary(transformationDictionary);
		}

		return transformationDictionary;
	}

	public List<Model> getModels(){
		PMML pmml = getPMML();

		return pmml.getModels();
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

	public ModelManager<? extends Model> getModelManager(String modelName, ModelManagerFactory modelManagerFactory){
		Model model = getModel(modelName);

		return modelManagerFactory.getModelManager(getPMML(), model);
	}

	@SuppressWarnings (
		value = {"unchecked"}
	)
	static
	public <E extends PMMLObject> E find(List<?> objects, Class<? extends E> clazz){

		for(Object object : objects){

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
	public <E extends PMMLObject> List<E> findAll(List<?> objects, Class<? extends E> clazz){
		List<E> result = Lists.newArrayList();

		for(Object object : objects){

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