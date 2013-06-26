/*
 * Copyright (c) 2013 University of Tartu
 */
package org.jpmml.manager;

import java.util.*;

import org.dmg.pmml.*;

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

	public String getSummary(){
		return "Scorecard";
	}

	@Override
	public Scorecard getModel(){
		ensureNotNull(this.scorecard);

		return this.scorecard;
	}

	/**
	 * @see #getModel
	 */
	public Scorecard createModel(){
		ensureNull(this.scorecard);

		this.scorecard = new Scorecard(new MiningSchema(), new Characteristics(), MiningFunctionType.REGRESSION);

		getModels().add(this.scorecard);

		return this.scorecard;
	}

	public List<Characteristic> getCharacteristics(){
		Scorecard scorecard = getModel();

		Characteristics characteristics = scorecard.getCharacteristics();
		ensureNotNull(characteristics);

		return characteristics.getCharacteristics();
	}
}