/*
 * Copyright, KNIME.com AG, Zurich, Switzerland
 */
package org.jpmml.evaluator;

import java.util.*;

import org.jpmml.manager.*;

import org.dmg.pmml.*;

import com.google.common.cache.*;
import com.google.common.collect.*;

public class SupportVectorMachineModelEvaluator extends ModelEvaluator<SupportVectorMachineModel> {

	public SupportVectorMachineModelEvaluator(PMML pmml){
		this(pmml, find(pmml.getModels(), SupportVectorMachineModel.class));
	}

	public SupportVectorMachineModelEvaluator(PMML pmml, SupportVectorMachineModel supportVectorMachineModel){
		super(pmml, supportVectorMachineModel);
	}

	@Override
	public String getSummary(){
		return "Support vector machine";
	}

	@Override
	public Map<FieldName, ?> evaluate(Map<FieldName, ?> arguments){
		SupportVectorMachineModel supportVectorMachineModel = getModel();
		if(!supportVectorMachineModel.isScorable()){
			throw new InvalidResultException(supportVectorMachineModel);
		}

		SvmRepresentationType svmRepresentation = supportVectorMachineModel.getSvmRepresentation();
		switch(svmRepresentation){
			case SUPPORT_VECTORS:
				break;
			default:
				throw new UnsupportedFeatureException(supportVectorMachineModel, svmRepresentation);
		}

		Map<FieldName, ?> predictions;

		ModelManagerEvaluationContext context = new ModelManagerEvaluationContext(this);
		context.pushFrame(arguments);

		MiningFunctionType miningFunction = supportVectorMachineModel.getFunctionName();
		switch(miningFunction){
			case REGRESSION:
				predictions = evaluateRegression(context);
				break;
			case CLASSIFICATION:
				predictions = evaluateClassification(context);
				break;
			default:
				throw new UnsupportedFeatureException(supportVectorMachineModel, miningFunction);
		}

		return OutputUtil.evaluate(predictions, context);
	}

	private Map<FieldName, ? extends Number> evaluateRegression(ModelManagerEvaluationContext context){
		SupportVectorMachineModel supportVectorMachineModel = getModel();

		List<SupportVectorMachine> supportVectorMachines = supportVectorMachineModel.getSupportVectorMachines();
		if(supportVectorMachines.size() != 1){
			throw new InvalidFeatureException(supportVectorMachineModel);
		}

		SupportVectorMachine supportVectorMachine = supportVectorMachines.get(0);

		double[] input = createInput(context);

		Double value = evaluateSupportVectorMachine(supportVectorMachine, input);

		return TargetUtil.evaluateRegression(value, context);
	}

	private Map<FieldName, ? extends ClassificationMap> evaluateClassification(ModelManagerEvaluationContext context){
		SupportVectorMachineModel supportVectorMachineModel = getModel();

		List<SupportVectorMachine> supportVectorMachines = supportVectorMachineModel.getSupportVectorMachines();
		if(supportVectorMachines.size() < 1){
			throw new InvalidFeatureException(supportVectorMachineModel);
		}

		ClassificationMap result;

		SvmClassificationMethodType svmClassificationMethod = getClassificationMethod();
		switch(svmClassificationMethod){
			case ONE_AGAINST_ALL:
				result = new ClassificationMap(ClassificationMap.Type.DISTANCE);
				break;
			case ONE_AGAINST_ONE:
				result = new ClassificationMap(ClassificationMap.Type.VOTE);
				break;
			default:
				throw new UnsupportedFeatureException(supportVectorMachineModel, svmClassificationMethod);
		}

		double[] input = createInput(context);

		for(SupportVectorMachine supportVectorMachine : supportVectorMachines){
			String category = supportVectorMachine.getTargetCategory();
			String alternateCategory = supportVectorMachine.getAlternateTargetCategory();

			Double value = evaluateSupportVectorMachine(supportVectorMachine, input);

			switch(svmClassificationMethod){
				case ONE_AGAINST_ALL:
					{
						if(category == null || alternateCategory != null){
							throw new InvalidFeatureException(supportVectorMachine);
						}

						result.put(category, value);
					}
					break;
				case ONE_AGAINST_ONE:
					{
						if(category == null || alternateCategory == null){
							throw new InvalidFeatureException(supportVectorMachine);
						}

						Double threshold = supportVectorMachine.getThreshold();
						if(threshold == null){
							threshold = supportVectorMachineModel.getThreshold();
						}

						String label;

						// "If the numeric prediction is smaller than the threshold, it corresponds to the targetCategory attribute"
						if((value).compareTo(threshold) < 0){
							label = category;
						} else

						{
							label = alternateCategory;
						}

						Double vote = result.get(label);
						if(vote == null){
							vote = 0d;
						}

						result.put(label, vote + 1d);
					}
					break;
				default:
					break;
			}
		}

		return TargetUtil.evaluateClassification(result, context);
	}

	private Double evaluateSupportVectorMachine(SupportVectorMachine supportVectorMachine, double[] input){
		SupportVectorMachineModel supportVectorMachineModel = getModel();

		double result = 0d;

		KernelType kernelType = supportVectorMachineModel.getKernelType();

		Coefficients coefficients = supportVectorMachine.getCoefficients();
		Iterator<Coefficient> coefficientIterator = coefficients.iterator();

		SupportVectors supportVectors = supportVectorMachine.getSupportVectors();
		Iterator<SupportVector> supportVectorIterator = supportVectors.iterator();

		Map<String, double[]> vectorMap = getVectorMap();

		while(coefficientIterator.hasNext() && supportVectorIterator.hasNext()){
			Coefficient coefficient = coefficientIterator.next();
			SupportVector supportVector = supportVectorIterator.next();

			double[] vector = vectorMap.get(supportVector.getVectorId());
			if(vector == null){
				throw new InvalidFeatureException(supportVector);
			}

			Double value = KernelTypeUtil.evaluate(kernelType, input, vector);

			result += (coefficient.getValue() * value);
		}

		if(coefficientIterator.hasNext() || supportVectorIterator.hasNext()){
			throw new InvalidFeatureException(supportVectorMachine);
		}

		result += coefficients.getAbsoluteValue();

		return result;
	}

	private SvmClassificationMethodType getClassificationMethod(){
		SupportVectorMachineModel supportVectorMachineModel = getModel();

		SvmClassificationMethodType svmClassificationMethod = PMMLObjectUtil.getField(supportVectorMachineModel, "classificationMethod");
		if(svmClassificationMethod != null){
			return svmClassificationMethod;
		}

		List<SupportVectorMachine> supportVectorMachines = supportVectorMachineModel.getSupportVectorMachines();
		for(SupportVectorMachine supportVectorMachine : supportVectorMachines){
			String category = supportVectorMachine.getTargetCategory();
			String alternateCategory = supportVectorMachine.getAlternateTargetCategory();

			if(category != null){

				if(alternateCategory != null){
					return SvmClassificationMethodType.ONE_AGAINST_ONE;
				}

				return SvmClassificationMethodType.ONE_AGAINST_ALL;
			}

			throw new InvalidFeatureException(supportVectorMachine);
		}

		throw new InvalidFeatureException(supportVectorMachineModel);
	}

	private double[] createInput(EvaluationContext context){
		SupportVectorMachineModel supportVectorMachineModel = getModel();

		VectorDictionary vectorDictionary = supportVectorMachineModel.getVectorDictionary();

		VectorFields vectorFields = vectorDictionary.getVectorFields();

		List<FieldRef> fieldRefs = vectorFields.getFieldRefs();

		double[] result = new double[fieldRefs.size()];

		for(int i = 0; i < fieldRefs.size(); i++){
			FieldRef fieldRef = fieldRefs.get(i);

			FieldValue value = ExpressionUtil.evaluate(fieldRef, context);
			if(value == null){
				throw new MissingFieldException(fieldRef.getField(), vectorFields);
			}

			result[i] = (value.asNumber()).doubleValue();
		}

		Integer numberOfFields = vectorFields.getNumberOfFields();
		if(numberOfFields != null && numberOfFields.intValue() != result.length){
			throw new InvalidFeatureException(vectorFields);
		}

		return result;
	}

	private Map<String, double[]> getVectorMap(){
		return getValue(SupportVectorMachineModelEvaluator.vectorCache);
	}

	static
	private Map<String, double[]> parseVectorDictionary(SupportVectorMachineModel supportVectorMachineModel){
		VectorDictionary vectorDictionary = supportVectorMachineModel.getVectorDictionary();

		VectorFields vectorFields = vectorDictionary.getVectorFields();

		Map<String, double[]> result = Maps.newLinkedHashMap();

		List<VectorInstance> vectorInstances = vectorDictionary.getVectorInstances();
		for(VectorInstance vectorInstance : vectorInstances){
			Array array = vectorInstance.getArray();
			RealSparseArray sparseArray = vectorInstance.getREALSparseArray();

			double[] vector;

			if(array != null && sparseArray == null){
				vector = ArrayUtil.toArray(array);
			} else

			if(array == null && sparseArray != null){
				vector = SparseArrayUtil.toArray(sparseArray);
			} else

			{
				throw new InvalidFeatureException(vectorInstance);
			} // End if

			Integer numberOfFields = vectorFields.getNumberOfFields();
			if(numberOfFields != null && numberOfFields.intValue() != vector.length){
				throw new InvalidFeatureException(vectorInstance);
			}

			result.put(vectorInstance.getId(), vector);
		}

		Integer numberOfVectors = vectorDictionary.getNumberOfVectors();
		if(numberOfVectors != null && numberOfVectors.intValue() != result.size()){
			throw new InvalidFeatureException(vectorDictionary);
		}

		return result;
	}

	private static final LoadingCache<SupportVectorMachineModel, Map<String, double[]>> vectorCache = CacheBuilder.newBuilder()
		.weakKeys()
		.build(new CacheLoader<SupportVectorMachineModel, Map<String, double[]>>(){

			@Override
			public Map<String, double[]> load(SupportVectorMachineModel supportVectorMachineModel){
				return parseVectorDictionary(supportVectorMachineModel);
			}
		});
}