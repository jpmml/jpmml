/*
 * Copyright (c) 2009 University of Tartu
 */
package org.jpmml.manager;

import java.util.*;

import org.dmg.pmml.*;

public class RegressionModelManager extends ModelManager<RegressionModel> {

	private RegressionModel regressionModel = null;


	public RegressionModelManager(){
	}

	public RegressionModelManager(PMML pmml){
		this(pmml, find(pmml.getContent(), RegressionModel.class));
	}

	public RegressionModelManager(PMML pmml, RegressionModel regressionModel){
		super(pmml);

		this.regressionModel = regressionModel;
	}

	public String getSummary(){
		return "Regression";
	}

	@Override
	public RegressionModel getModel(){
		ensureNotNull(this.regressionModel);

		return this.regressionModel;
	}

	public RegressionModel createRegressionModel(){
		return createModel(MiningFunctionType.REGRESSION);
	}

	/**
	 * @throws ModelManagerException If the Model already exists
	 *
	 * @see #getModel()
	 */
	public RegressionModel createModel(MiningFunctionType miningFunction){
		ensureNull(this.regressionModel);

		this.regressionModel = new RegressionModel(new MiningSchema(), miningFunction);

		getModels().add(this.regressionModel);

		return this.regressionModel;
	}

	@Override
	public FieldName getTarget(){
		RegressionModel regressionModel = getModel();

		FieldName name = regressionModel.getTargetFieldName();
		if(name != null){
			return name;
		}

		return super.getTarget();
	}

	public RegressionModel setTarget(FieldName name){
		RegressionModel regressionModel = getModel();
		regressionModel.setTargetFieldName(name);

		return regressionModel;
	}

	public List<RegressionTable> getRegressionTables(){
		RegressionModel model = getModel();
		return model.getRegressionTables();
	}

	static
	public NumericPredictor getNumericPredictor(RegressionTable regressionTable, FieldName name){
		return find(regressionTable.getNumericPredictors(), name);
	}

	static
	public NumericPredictor addNumericPredictor(RegressionTable regressionTable, FieldName name, Double coefficient){
		NumericPredictor numericPredictor = new NumericPredictor(name, coefficient.doubleValue());
		(regressionTable.getNumericPredictors()).add(numericPredictor);

		return numericPredictor;
	}

	static
	public CategoricalPredictor getCategoricalPredictor(RegressionTable regressionTable, FieldName name){
		return find(regressionTable.getCategoricalPredictors(), name);
	}

	static
	public CategoricalPredictor addCategoricalPredictor(RegressionTable regressionTable, FieldName name, String value, Double coefficient){
		CategoricalPredictor categoricalPredictor = new CategoricalPredictor(name, value, coefficient.doubleValue());
		(regressionTable.getCategoricalPredictors()).add(categoricalPredictor);

		return categoricalPredictor;
	}
}