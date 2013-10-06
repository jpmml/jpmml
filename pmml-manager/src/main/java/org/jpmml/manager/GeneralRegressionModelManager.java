/**
 * Copyright (c) 2013 University of Tartu
 */
package org.jpmml.manager;

import java.util.*;

import org.dmg.pmml.*;

import com.google.common.cache.*;
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
		return getValue(GeneralRegressionModelManager.factorCache);
	}

	public BiMap<FieldName, Predictor> getCovariateRegistry(){
		return getValue(GeneralRegressionModelManager.covariateCache);
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
	private BiMap<FieldName, Predictor> parsePredictorRegistry(PredictorList predictorList){
		BiMap<FieldName, Predictor> result = HashBiMap.create();

		List<Predictor> predictors = predictorList.getPredictors();
		for(Predictor predictor : predictors){
			result.put(predictor.getName(), predictor);
		}

		return result;
	}

	protected static final LoadingCache<GeneralRegressionModel, BiMap<FieldName, Predictor>> factorCache = CacheBuilder.newBuilder()
		.weakKeys()
		.build(new CacheLoader<GeneralRegressionModel, BiMap<FieldName, Predictor>>(){

			@Override
			public BiMap<FieldName, Predictor> load(GeneralRegressionModel generalRegressionModel){
				return parsePredictorRegistry(generalRegressionModel.getFactorList());
			}
		});

	protected static final LoadingCache<GeneralRegressionModel, BiMap<FieldName, Predictor>> covariateCache = CacheBuilder.newBuilder()
		.weakKeys()
		.build(new CacheLoader<GeneralRegressionModel, BiMap<FieldName, Predictor>>(){

			@Override
			public BiMap<FieldName, Predictor> load(GeneralRegressionModel generalRegressionModel){
				return parsePredictorRegistry(generalRegressionModel.getCovariateList());
			}
		});
}