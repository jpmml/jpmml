/*
 * Copyright (c) 2010 University of Tartu
 */
package org.jpmml.manager;

import org.dmg.pmml.*;

public class ModelManagerFactory {

	protected ModelManagerFactory(){
	}

	public ModelManager<? extends Model> getModelManager(PMML pmml, Model model){

		if(model instanceof AssociationModel){
			return new AssociationModelManager(pmml, (AssociationModel)model);
		} else

		if(model instanceof ClusteringModel){
			return new ClusteringModelManager(pmml, (ClusteringModel)model);
		} else

		if(model instanceof GeneralRegressionModel){
			return new GeneralRegressionModelManager(pmml, (GeneralRegressionModel)model);
		} else

		if(model instanceof MiningModel){
			return new MiningModelManager(pmml, (MiningModel)model);
		} else

		if(model instanceof NeuralNetwork){
			return new NeuralNetworkManager(pmml, (NeuralNetwork)model);
		} else

		if(model instanceof RegressionModel){
			return new RegressionModelManager(pmml, (RegressionModel)model);
		} else

		if(model instanceof RuleSetModel){
			return new RuleSetModelManager(pmml, (RuleSetModel)model);
		} else

		if(model instanceof Scorecard){
			return new ScorecardManager(pmml, (Scorecard)model);
		} else

		if(model instanceof TreeModel){
			return new TreeModelManager(pmml, (TreeModel)model);
		}

		throw new UnsupportedFeatureException(model);
	}

	static
	public ModelManagerFactory getInstance(){
		return new ModelManagerFactory();
	}
}