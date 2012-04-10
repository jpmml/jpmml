/*
 * Copyright (c) 2012 University of Tartu
 */
package org.jpmml.manager;

import org.dmg.pmml.*;

public class ModelEvaluatorFactory extends ModelManagerFactory {

	protected ModelEvaluatorFactory(){
	}

	@Override
	public ModelManager<? extends Model> getModelManager(PMML pmml, Model model){

		if(model instanceof RegressionModel){
			return new RegressionModelEvaluator(pmml, (RegressionModel)model);
		} else

		if(model instanceof TreeModel){
			return new TreeModelEvaluator(pmml, (TreeModel)model);
		} else

		if(model instanceof NeuralNetwork){
			return new NeuralNetworkEvaluator(pmml, (NeuralNetwork)model);
		} else

		if(model instanceof MiningModel){

			if(RandomForestModelManager.isRandomForest((MiningModel)model)){
				return new RandomForestModelEvaluator(pmml, (MiningModel)model);
			}
		}

		throw new IllegalArgumentException("Unsupported model type: " + model.getClass().getName());
	}

	static
	public ModelEvaluatorFactory getInstance(){
		return new ModelEvaluatorFactory();
	}
}