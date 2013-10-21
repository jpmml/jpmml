/*
 * Copyright (c) 2009 University of Tartu
 */
package org.jpmml.manager;

import java.util.*;

import org.dmg.pmml.*;

import com.google.common.collect.*;

import static com.google.common.base.Preconditions.*;

public class ModelManager<M extends Model> extends PMMLManager implements Consumer {

	private M model = null;


	public ModelManager(PMML pmml, M model){
		super(pmml);

		setModel(model);
	}

	public M getModel(){
		return this.model;
	}

	private void setModel(M model){
		checkNotNull(model);

		this.model = model;
	}

	@Override
	public String getSummary(){
		return null;
	}

	@Override
	public List<FieldName> getActiveFields(){
		return getMiningFields(FieldUsageType.ACTIVE);
	}

	@Override
	public List<FieldName> getGroupFields(){
		return getMiningFields(FieldUsageType.GROUP);
	}

	@Override
	public FieldName getTargetField(){
		List<FieldName> predictedFields = getPredictedFields();

		// "The definition of predicted fields in the MiningSchema is not required"
		if(predictedFields.size() < 1){
			return null;
		} else

		if(predictedFields.size() > 1){
			throw new InvalidFeatureException("Too many predicted fields", getMiningSchema());
		}

		return predictedFields.get(0);
	}

	@Override
	public List<FieldName> getPredictedFields(){
		return getMiningFields(FieldUsageType.PREDICTED);
	}

	@Override
	public MiningField getMiningField(FieldName name){
		MiningSchema miningSchema = getMiningSchema();

		List<MiningField> miningFields = miningSchema.getMiningFields();

		return find(miningFields, name);
	}

	public List<FieldName> getMiningFields(FieldUsageType fieldUsageType){
		List<FieldName> result = Lists.newArrayList();

		MiningSchema miningSchema = getMiningSchema();

		List<MiningField> miningFields = miningSchema.getMiningFields();
		for(MiningField miningField : miningFields){

			if((miningField.getUsageType()).equals(fieldUsageType)){
				result.add(miningField.getName());
			}
		}

		return result;
	}

	@Override
	public OutputField getOutputField(FieldName name){
		Output output = getOrCreateOutput();

		List<OutputField> outputFields = output.getOutputFields();

		return find(outputFields, name);
	}

	@Override
	public List<FieldName> getOutputFields(){
		List<FieldName> result = Lists.newArrayList();

		Output output = getOrCreateOutput();

		List<OutputField> outputFields = output.getOutputFields();
		for(OutputField outputField : outputFields){
			result.add(outputField.getName());
		}

		return result;
	}

	@Override
	public DerivedField resolveField(FieldName name){
		LocalTransformations localTransformations = getOrCreateLocalTransformations();

		List<DerivedField> derivedFields = localTransformations.getDerivedFields();

		DerivedField derivedField = find(derivedFields, name);
		if(derivedField == null){
			derivedField = super.resolveField(name);
		}

		return derivedField;
	}

	public Target getTarget(FieldName name){
		Targets targets = getOrCreateTargets();

		for(Target target : targets){

			if((target.getField()).equals(name)){
				return target;
			}
		}

		return null;
	}

	public MiningSchema getMiningSchema(){
		M model = getModel();

		return model.getMiningSchema();
	}

	public LocalTransformations getOrCreateLocalTransformations(){
		M model = getModel();

		LocalTransformations localTransformations = model.getLocalTransformations();
		if(localTransformations == null){
			localTransformations = new LocalTransformations();

			model.setLocalTransformations(localTransformations);
		}

		return localTransformations;
	}

	public Output getOrCreateOutput(){
		M model = getModel();

		Output output = model.getOutput();
		if(output == null){
			output = new Output();

			model.setOutput(output);
		}

		return output;
	}

	public Targets getOrCreateTargets(){
		M model = getModel();

		Targets targets = model.getTargets();
		if(targets == null){
			targets = new Targets();

			model.setTargets(targets);
		}

		return targets;
	}
}