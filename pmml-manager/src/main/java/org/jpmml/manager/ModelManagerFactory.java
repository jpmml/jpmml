/*
 * Copyright (c) 2010 University of Tartu
 */
package org.jpmml.manager;

import org.dmg.pmml.*;

public class ModelManagerFactory {

	protected ModelManagerFactory(){
	}

	public ModelManager<? extends Model> getModelManager(PMML pmml, Model model){

		if(model instanceof RegressionModel){
			return new RegressionModelManager(pmml, (RegressionModel)model);
		} else

		if(model instanceof TreeModel){
			return new TreeModelManager(pmml, (TreeModel)model);
		} else
			
		if(model instanceof NeuralNetwork){
			return new NeuralNetworkManager(pmml, (NeuralNetwork)model);
		}

		throw new IllegalArgumentException("Unsupported model type: "+model.getClass().getName());
	}

	static
	public ModelManagerFactory getInstance(){
		return new ModelManagerFactory();
	}
}