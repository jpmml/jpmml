/*
 * Copyright (c) 2013 University of Tartu
 */
package org.jpmml.evaluator;

import org.jpmml.manager.*;

import org.dmg.pmml.*;

public class PMMLManagerEvaluationContext extends EvaluationContext {

	private PMMLManager pmmlManager = null;


	public PMMLManagerEvaluationContext(PMMLManager pmmlManager){
		setPmmlManager(pmmlManager);
	}

	@Override
	public DerivedField resolveField(FieldName name){
		PMMLManager pmmlManager = getPmmlManager();

		return pmmlManager.resolveField(name);
	}

	@Override
	public DefineFunction resolveFunction(String name){
		PMMLManager pmmlManager = getPmmlManager();

		return pmmlManager.resolveFunction(name);
	}

	public PMMLManager getPmmlManager(){
		return this.pmmlManager;
	}

	private void setPmmlManager(PMMLManager pmmlManager){
		this.pmmlManager = pmmlManager;
	}
}