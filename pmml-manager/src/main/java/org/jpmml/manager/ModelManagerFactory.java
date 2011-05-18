/*
 * Copyright (c) 2010 University of Tartu
 */
package org.jpmml.manager;

import org.dmg.pmml.*;

public class ModelManagerFactory {

	public ModelManager<? extends Model> getModelManager(PMML pmml, Model model){

		if(model instanceof RegressionModel){
			return new RegressionModelManager(pmml, (RegressionModel)model);
		} else

		if(model instanceof TreeModel){
			return new TreeModelManager(pmml, (TreeModel)model);
		}

		throw new IllegalArgumentException();
	}
}