/*
 * Copyright (c) 2012 University of Tartu
 */
package org.jpmml.evaluator;

import org.jpmml.manager.*;

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

			if(RandomForestManager.isRandomForest((MiningModel)model)){
				return new RandomForestEvaluator(pmml, (MiningModel)model);
			}
		}
		if (model instanceof Scorecard) {
			return new ScorecardEvaluator(pmml, (Scorecard) model);	
		}

		throw new UnsupportedFeatureException(model);
	}

	static
	public ModelEvaluatorFactory getInstance(){
		return new ModelEvaluatorFactory();
	}
}