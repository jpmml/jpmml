/*
 * Copyright (c) 2010 University of Tartu
 */
package org.jpmml.manager;

import org.dmg.pmml.*;

abstract
public class ModelManagerFactory {

	protected ModelManagerFactory(){
	}

	abstract
	public ModelManager<? extends Model> getModelManager(PMML pmml, Model model);
}