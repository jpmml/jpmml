/*
 * Copyright (c) 2012 University of Tartu
 */
package org.jpmml.manager;

import java.util.*;

import org.dmg.pmml.*;

abstract
public class MiningModelManager extends ModelManager<MiningModel> {

	private MiningModel miningModel = null;


	public MiningModelManager(){
	}

	public MiningModelManager(PMML pmml){
		this(pmml, find(pmml.getContent(), MiningModel.class));
	}

	public MiningModelManager(PMML pmml, MiningModel miningModel){
		super(pmml);

		this.miningModel = miningModel;
	}

	@Override
	public MiningModel getModel(){
		ensureNotNull(this.miningModel);

		return this.miningModel;
	}

	/**
	 * @throws ModelManagerException If the Model already exists
	 *
	 * @see #getModel()
	 */
	public MiningModel createModel(MiningFunctionType miningFunction){
		ensureNull(this.miningModel);

		this.miningModel = new MiningModel(new MiningSchema(), miningFunction);

		List<Model> content = getPmml().getContent();
		content.add(this.miningModel);

		return this.miningModel;
	}
}