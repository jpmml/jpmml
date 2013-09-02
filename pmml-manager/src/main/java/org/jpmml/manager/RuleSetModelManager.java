/*
 * Copyright (c) 2013 University of Tartu
 */
package org.jpmml.manager;

import org.dmg.pmml.*;

import static com.google.common.base.Preconditions.*;

public class RuleSetModelManager extends ModelManager<RuleSetModel> {

	private RuleSetModel ruleSetModel = null;


	public RuleSetModelManager(){
	}

	public RuleSetModelManager(PMML pmml){
		this(pmml, find(pmml.getContent(), RuleSetModel.class));
	}

	public RuleSetModelManager(PMML pmml, RuleSetModel ruleSetModel){
		super(pmml);

		this.ruleSetModel = ruleSetModel;
	}

	@Override
	public String getSummary(){
		return "Ruleset model";
	}

	@Override
	public RuleSetModel getModel(){
		checkState(this.ruleSetModel != null);

		return this.ruleSetModel;
	}

	/**
	 * @see #getModel()
	 */
	public RuleSetModel createModel(){
		checkState(this.ruleSetModel == null);

		this.ruleSetModel = new RuleSetModel(new MiningSchema(), new RuleSet(), MiningFunctionType.CLASSIFICATION);

		getModels().add(this.ruleSetModel);

		return this.ruleSetModel;
	}

	public RuleSet getRuleSet(){
		RuleSetModel ruleSetModel = getModel();

		return ruleSetModel.getRuleSet();
	}
}