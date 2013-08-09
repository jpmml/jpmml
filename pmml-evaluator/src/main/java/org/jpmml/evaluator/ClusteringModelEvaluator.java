/*
 * Copyright (c) 2013 University of Tartu
 */
package org.jpmml.evaluator;

import java.util.*;

import org.jpmml.manager.*;

import org.dmg.pmml.*;

import com.google.common.collect.*;

public class ClusteringModelEvaluator extends ClusteringModelManager implements Evaluator {

	private BiMap<String, Cluster> entities = null;


	public ClusteringModelEvaluator(PMML pmml){
		super(pmml);
	}

	public ClusteringModelEvaluator(PMML pmml, ClusteringModel clusteringModel){
		super(pmml, clusteringModel);
	}

	@Override
	public BiMap<String, Cluster> getEntityRegistry(){

		if(this.entities == null){
			this.entities = super.getEntityRegistry();
		}

		return this.entities;
	}

	@Override
	public Object prepare(FieldName name, Object value){
		return ParameterUtil.prepare(getDataField(name), getMiningField(name), value);
	}

	@Override
	public Map<FieldName, ?> evaluate(Map<FieldName, ?> arguments){
		ClusteringModel clusteringModel = getModel();
		if(!clusteringModel.isScorable()){
			throw new InvalidResultException(clusteringModel);
		}

		Map<FieldName, ?> predictions;

		ModelManagerEvaluationContext context = new ModelManagerEvaluationContext(this, arguments);

		MiningFunctionType miningFunction = clusteringModel.getFunctionName();
		switch(miningFunction){
			case CLUSTERING:
				predictions = evaluateClustering(context);
				break;
			default:
				throw new UnsupportedFeatureException(clusteringModel, miningFunction);
		}

		return OutputUtil.evaluate(predictions, context);
	}

	private Map<FieldName, ClusterClassificationMap> evaluateClustering(EvaluationContext context){
		ClusteringModel clusteringModel = getModel();

		ClusteringModel.ModelClass modelClass = clusteringModel.getModelClass();
		switch(modelClass){
			case CENTER_BASED:
				break;
			default:
				throw new UnsupportedFeatureException(clusteringModel, modelClass);
		}

		ComparisonMeasure comparisonMeasure = clusteringModel.getComparisonMeasure();

		Measure measure = comparisonMeasure.getMeasure();

		if(MeasureUtil.isDistance(measure)){
			return evaluateDistanceClustering(context);
		} else

		if(MeasureUtil.isSimilarity(measure)){
			return evaluateSimilarityClustering(context);
		}

		throw new UnsupportedFeatureException(clusteringModel);
	}

	private Map<FieldName, ClusterClassificationMap> evaluateDistanceClustering(EvaluationContext context){
		ClusteringModel clusteringModel = getModel();

		ClusterClassificationMap result = new ClusterClassificationMap(ClassificationMap.Type.DISTANCE);

		List<Number> values = Lists.newArrayList();

		List<Double> fieldWeights = Lists.newArrayList();

		List<ClusteringField> clusteringFields = getCenterClusteringFields();
		for(ClusteringField clusteringField : clusteringFields){
			FieldName name = clusteringField.getField();

			Object value = context.getArgument(name);

			DataType dataType = ParameterUtil.getDataType(value);
			switch(dataType){
				case DOUBLE:
				case FLOAT: // XXX
				case INTEGER:
					values.add((Number)value);
					break;
				default:
					throw new EvaluationException();
			}

			Double fieldWeight = clusteringField.getFieldWeight();
			fieldWeights.add(fieldWeight);
		}

		Double adjustment;

		MissingValueWeights missingValueWeights = clusteringModel.getMissingValueWeights();
		if(missingValueWeights != null){
			Array array = missingValueWeights.getArray();

			List<Double> adjustmentValues = ArrayUtil.getRealContent(array);
			if(values.size() != adjustmentValues.size()){
				throw new EvaluationException();
			}

			double sum = 0d;
			double nonmissingSum = 0d;

			for(int i = 0; i < values.size(); i++){
				Object value = values.get(i);

				Double adjustmentValue = adjustmentValues.get(i);

				sum += adjustmentValue.doubleValue();
				nonmissingSum += (value != null ? adjustmentValue.doubleValue() : 0d);
			}

			adjustment = (sum / nonmissingSum);
		} else

		{
			double sum = 0d;
			double nonmissingSum = 0d;

			for(int i = 0; i < values.size(); i++){
				Object value = values.get(i);

				sum += 1d;
				nonmissingSum += (value != null ? 1d : 0d);
			}

			adjustment = (sum / nonmissingSum);
		}

		ComparisonMeasure comparisonMeasure = clusteringModel.getComparisonMeasure();

		BiMap<Cluster, String> inverseEntities = (getEntityRegistry().inverse());

		List<Cluster> clusters = getClusters();
		for(Cluster cluster : clusters){
			Array array = cluster.getArray();

			List<? extends Number> clusterValues = ArrayUtil.getNumberContent(array);
			if(values.size() != clusterValues.size()){
				throw new EvaluationException();
			}

			String id = inverseEntities.get(cluster);

			Double distance = MeasureUtil.evaluateDistance(comparisonMeasure, clusteringFields, values, clusterValues, fieldWeights, adjustment);

			result.put(cluster, id, distance);
		}

		return Collections.singletonMap(getTargetField(), result);
	}

	private Map<FieldName, ClusterClassificationMap> evaluateSimilarityClustering(EvaluationContext context){
		ClusteringModel clusteringModel = getModel();

		throw new UnsupportedFeatureException(clusteringModel);
	}

	private List<ClusteringField> getCenterClusteringFields(){
		List<ClusteringField> result = Lists.newArrayList();

		List<ClusteringField> clusteringFields = getClusteringFields();
		for(ClusteringField clusteringField : clusteringFields){
			ClusteringField.CenterField centerField = clusteringField.getCenterField();

			switch(centerField){
				case TRUE:
					result.add(clusteringField);
					break;
				case FALSE:
					break;
				default:
					throw new UnsupportedFeatureException(clusteringField, centerField);
			}
		}

		return result;
	}
}