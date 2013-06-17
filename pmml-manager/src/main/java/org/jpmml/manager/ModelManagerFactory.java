/*
 * Copyright (c) 2010 University of Tartu
 */
package org.jpmml.manager;

import org.dmg.pmml.*;

public class ModelManagerFactory {

	protected boolean alternateImplementation = false;

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
		} else

		if(model instanceof MiningModel){
			return new MiningModelManager(pmml, (MiningModel)model);
		}

		throw new UnsupportedFeatureException(model);
	}

	static
	public ModelManagerFactory getInstance(){
		return new ModelManagerFactory();
	}

	public void setAlternateImplementation(boolean alternateImplementation) {
		this.alternateImplementation = alternateImplementation;
	}
}