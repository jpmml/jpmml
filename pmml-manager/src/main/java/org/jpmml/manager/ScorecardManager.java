/*
 * Copyright (c) 2013 University of Tartu
 */
package org.jpmml.manager;

import java.util.*;

import org.dmg.pmml.*;

import static com.google.common.base.Preconditions.*;

public class ScorecardManager extends ModelManager<Scorecard> {

	private Scorecard scorecard = null;


	public ScorecardManager(){
	}

	public ScorecardManager(PMML pmml){
		this(pmml, find(pmml.getContent(), Scorecard.class));
	}

	public ScorecardManager(PMML pmml, Scorecard scorecard){
		super(pmml);

		this.scorecard = scorecard;
	}

	@Override
	public String getSummary(){
		return "Scorecard";
	}

	@Override
	public Scorecard getModel(){
		checkState(this.scorecard != null);

		return this.scorecard;
	}

	/**
	 * @see #getModel
	 */
	public Scorecard createModel(){
		checkState(this.scorecard == null);

		this.scorecard = new Scorecard(new MiningSchema(), new Characteristics(), MiningFunctionType.REGRESSION);

		getModels().add(this.scorecard);

		return this.scorecard;
	}

	public List<Characteristic> getCharacteristics(){
		Scorecard scorecard = getModel();

		Characteristics characteristics = scorecard.getCharacteristics();

		return characteristics.getCharacteristics();
	}
}