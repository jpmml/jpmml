/**
 * Copyright (c) 2013 University of Tartu
 */
package org.jpmml.manager;

import org.dmg.pmml.*;

import static com.google.common.base.Preconditions.*;

public class GeneralRegressionModelManager extends ModelManager<GeneralRegressionModel> {

	private GeneralRegressionModel generalRegressionModel = null;


	public GeneralRegressionModelManager(){
	}

	public GeneralRegressionModelManager(PMML pmml){
		this(pmml, find(pmml.getContent(), GeneralRegressionModel.class));
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

	public PPMatrix getPPMatrix(){
		GeneralRegressionModel generalRegressionModel = getModel();

		return generalRegressionModel.getPPMatrix();
	}

	public ParamMatrix getParamMatrix(){
		GeneralRegressionModel generalRegressionModel = getModel();

		return generalRegressionModel.getParamMatrix();
	}
}