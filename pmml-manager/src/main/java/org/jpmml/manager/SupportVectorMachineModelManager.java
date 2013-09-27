/*
 * Copyright, KNIME.com AG, Zurich, Switzerland
 */
package org.jpmml.manager;

import java.util.*;

import org.dmg.pmml.*;

import static com.google.common.base.Preconditions.*;

public class SupportVectorMachineModelManager extends ModelManager<SupportVectorMachineModel> {

	private SupportVectorMachineModel supportVectorMachineModel = null;


	public SupportVectorMachineModelManager(){
	}

	public SupportVectorMachineModelManager(PMML pmml){
		this(pmml, find(pmml.getModels(), SupportVectorMachineModel.class));
	}

	public SupportVectorMachineModelManager(PMML pmml, SupportVectorMachineModel supportVectorMachineModel){
		super(pmml);

		this.supportVectorMachineModel = supportVectorMachineModel;
	}

	@Override
	public String getSummary(){
		return "Support vector machine";
	}

	@Override
	public SupportVectorMachineModel getModel(){
		checkState(this.supportVectorMachineModel != null);

		return this.supportVectorMachineModel;
	}

	public KernelType getKernelType(){
		SupportVectorMachineModel supportVectorMachineModel = getModel();

		return supportVectorMachineModel.getKernelType();
	}

	public VectorDictionary getVectorDictionary(){
		SupportVectorMachineModel supportVectorMachineModel = getModel();

		return supportVectorMachineModel.getVectorDictionary();
	}

	public List<SupportVectorMachine> getSupportVectorMachines(){
		SupportVectorMachineModel supportVectorMachineModel = getModel();

		return supportVectorMachineModel.getSupportVectorMachines();
	}
}