package org.jpmml.translator;

import org.dmg.pmml.Model;
import org.dmg.pmml.PMML;
import org.dmg.pmml.Scorecard;
import org.dmg.pmml.TreeModel;
import org.jpmml.manager.ModelManager;
import org.jpmml.manager.ModelManagerFactory;
import org.jpmml.manager.UnsupportedFeatureException;

public class ModelTranslatorFactory extends ModelManagerFactory {

	protected ModelTranslatorFactory() {
	}

	@Override
	public ModelManager<? extends Model> getModelManager(PMML pmml, Model model){

		if(model instanceof TreeModel){
			return new TreeModelTranslator(pmml, (TreeModel)model);
		} else

		if (model instanceof Scorecard) {
			return new ScorecardTranslator(pmml, (Scorecard)model);
		} else
			
//		if(model instanceof RegressionModel){
//			return new RegressionModelEvaluator(pmml, (RegressionModel)model);
//		} else
//
//		if(model instanceof NeuralNetwork){
//			return new NeuralNetworkEvaluator(pmml, (NeuralNetwork)model);
//		} else
//
//		if(model instanceof MiningModel){
//
//			if(RandomForestManager.isRandomForest((MiningModel)model)){
//				return new RandomForestEvaluator(pmml, (MiningModel)model);
//			}
//		}

		throw new UnsupportedFeatureException(model);
	}

	static
	public ModelTranslatorFactory getInstance(){
		return new ModelTranslatorFactory();
	}
}
