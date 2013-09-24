/**
 * Copyright (c) 2013 University of Tartu
 */
package org.jpmml.manager;

import java.util.*;

import org.dmg.pmml.*;

import com.google.common.collect.*;

import static com.google.common.base.Preconditions.*;

public class GeneralRegressionModelManager extends ModelManager<GeneralRegressionModel> {

	private GeneralRegressionModel generalRegressionModel = null;


	public GeneralRegressionModelManager(){
	}

	public GeneralRegressionModelManager(PMML pmml){
		this(pmml, find(pmml.getModels(), GeneralRegressionModel.class));
	}

	public GeneralRegressionModelManager(PMML pmml, GeneralRegressionModel generalRegressionModel){
		super(pmml);

		this.generalRegressionModel = generalRegressionModel;
	}

	@Override
	public String getSummary(){
		return "General regression";
	}

	@Override
	public GeneralRegressionModel getModel(){
		checkState(this.generalRegressionModel != null);

		return this.generalRegressionModel;
	}

	public BiMap<FieldName, Predictor> getFactorRegistry(){
		GeneralRegressionModel generalRegressionModel = getModel();

		PredictorList predictorList = generalRegressionModel.getFactorList();

		return toPredictorRegistry(predictorList);
	}

	public BiMap<FieldName, Predictor> getCovariateRegistry(){
		GeneralRegressionModel generalRegressionModel = getModel();

		PredictorList predictorList = generalRegressionModel.getCovariateList();

		return toPredictorRegistry(predictorList);
	}

	public PPMatrix getPPMatrix(){
		GeneralRegressionModel generalRegressionModel = getModel();

		return generalRegressionModel.getPPMatrix();
	}

	public ParamMatrix getParamMatrix(){
		GeneralRegressionModel generalRegressionModel = getModel();

		return generalRegressionModel.getParamMatrix();
	}

	static
	private BiMap<FieldName, Predictor> toPredictorRegistry(PredictorList predictorList){
		BiMap<FieldName, Predictor> result = HashBiMap.create();

		List<Predictor> predictors = predictorList.getPredictors();
		for(Predictor predictor : predictors){
			result.put(predictor.getName(), predictor);
		}

		return result;
	}
}