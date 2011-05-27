/*
 * Copyright (c) 2009 University of Tartu
 */
package org.jpmml.manager;

import java.util.*;

import org.dmg.pmml.*;

public class RegressionModelManager extends ModelManager<RegressionModel> {

	private RegressionModel regressionModel = null;

	private RegressionTable regressionTable = null;


	public RegressionModelManager(){
	}

	public RegressionModelManager(PMML pmml){
		super(pmml);

		List<Model> content = pmml.getContent();
		this.regressionModel = find(content, RegressionModel.class);
	}

	public RegressionModelManager(PMML pmml, RegressionModel regressionModel){
		super(pmml);

		this.regressionModel = regressionModel;
	}

	@Override
	public RegressionModel getModel(){

		if(this.regressionModel == null){
			throw new IllegalStateException();
		}

		return this.regressionModel;
	}

	public RegressionModel createModel(MiningFunctionType miningFunction){

		if(this.regressionModel != null){
			throw new IllegalStateException();
		}

		this.regressionModel = new RegressionModel(new MiningSchema(), miningFunction);

		List<Model> content = getPmml().getContent();
		content.add(this.regressionModel);

		return this.regressionModel;
	}

	public FieldName getTarget(){
		RegressionModel regressionModel = getModel();

		return regressionModel.getTargetFieldName();
	}

	public RegressionModel setTarget(FieldName name){
		RegressionModel regressionModel = getModel();
		regressionModel.setTargetFieldName(name);

		return regressionModel;
	}

	public Double getIntercept(){
		RegressionTable regressionTable = getOrCreateRegressionTable();

		return Double.valueOf(regressionTable.getIntercept());
	}

	public RegressionTable setIntercept(Double intercept){
		RegressionTable regressionTable = getOrCreateRegressionTable();
		regressionTable.setIntercept(intercept.doubleValue());

		return regressionTable;
	}

	public List<NumericPredictor> getNumericPrecictors(){
		RegressionTable regressionTable = getOrCreateRegressionTable();

		return regressionTable.getNumericPredictors();
	}

	public NumericPredictor getNumericPredictor(FieldName name){
		List<NumericPredictor> numericPredictors = getNumericPrecictors();

		for(NumericPredictor numericPredictor : numericPredictors){

			if((numericPredictor.getName()).equals(name)){
				return numericPredictor;
			}
		}

		return null;
	}

	public NumericPredictor addNumericPredictor(FieldName name, Double coefficient){
		RegressionTable regressionTable = getOrCreateRegressionTable();

		NumericPredictor numericPredictor = new NumericPredictor(name, coefficient.doubleValue());
		regressionTable.getNumericPredictors().add(numericPredictor);

		return numericPredictor;
	}

	public RegressionTable getOrCreateRegressionTable(){

		if(this.regressionTable == null){
			RegressionModel regressionModel = getModel();

			List<RegressionTable> regressionTables = regressionModel.getRegressionTables();
			if(regressionTables.isEmpty()){
				RegressionTable regressionTable = new RegressionTable(0d);

				regressionTables.add(regressionTable);
			}

			this.regressionTable = regressionTables.get(0);
		}

		return this.regressionTable;
	}

	@Override
	public Double evaluate(Map<FieldName, ?> parameters){
		double result = 0D;

		result += evaluateIntercept();

		List<NumericPredictor> numericPredictors = getNumericPrecictors();
		for(NumericPredictor numericPredictor : numericPredictors){
			result += evaluateNumericPredictor(numericPredictor, parameters);
		}

		return Double.valueOf(result);
	}

	private double evaluateIntercept(){
		Double intercept = getIntercept();

		if(intercept == null){
			throw new EvaluationException("Missing intercept");
		}

		return intercept.doubleValue();
	}

	private double evaluateNumericPredictor(NumericPredictor numericPredictor, Map<FieldName, ?> parameters){
		Double fieldValue = (Double)getParameterValue(parameters, numericPredictor.getName());

		return numericPredictor.getCoefficient() * fieldValue.doubleValue();
	}
}