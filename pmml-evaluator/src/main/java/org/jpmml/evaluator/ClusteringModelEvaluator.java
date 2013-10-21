/*
 * Copyright (c) 2013 University of Tartu
 */
package org.jpmml.evaluator;

import java.util.*;

import org.jpmml.manager.*;

import org.dmg.pmml.*;

import com.google.common.cache.*;
import com.google.common.collect.*;

public class ClusteringModelEvaluator extends ModelEvaluator<ClusteringModel> implements HasEntityRegistry<Cluster> {

	public ClusteringModelEvaluator(PMML pmml){
		this(pmml, find(pmml.getModels(), ClusteringModel.class));
	}

	public ClusteringModelEvaluator(PMML pmml, ClusteringModel clusteringModel){
		super(pmml, clusteringModel);
	}

	@Override
	public String getSummary(){
		return "Clustering model";
	}

	/**
	 * @return <code>null</code> Always.
	 */
	@Override
	public Target getTarget(FieldName name){
		return null;
	}

	@Override
	public BiMap<String, Cluster> getEntityRegistry(){
		return getValue(ClusteringModelEvaluator.entityCache);
	}

	@Override
	public Map<FieldName, ?> evaluate(Map<FieldName, ?> arguments){
		ClusteringModel clusteringModel = getModel();
		if(!clusteringModel.isScorable()){
			throw new InvalidResultException(clusteringModel);
		}

		Map<FieldName, ?> predictions;

		ModelManagerEvaluationContext context = new ModelManagerEvaluationContext(this);
		context.pushFrame(arguments);

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
		if(!MeasureUtil.isDistance(measure)){
			throw new UnsupportedFeatureException(measure);
		}

		ClusterClassificationMap result = new ClusterClassificationMap(ClassificationMap.Type.DISTANCE);

		List<FieldValue> values = Lists.newArrayList();

		List<ClusteringField> clusteringFields = getCenterClusteringFields();
		for(ClusteringField clusteringField : clusteringFields){
			FieldValue value = ExpressionUtil.evaluate(clusteringField.getField(), context);

			values.add(value);
		}

		Double adjustment;

		MissingValueWeights missingValueWeights = clusteringModel.getMissingValueWeights();
		if(missingValueWeights != null){
			Array array = missingValueWeights.getArray();

			List<Double> adjustmentValues = ArrayUtil.getRealContent(array);
			if(values.size() != adjustmentValues.size()){
				throw new InvalidFeatureException(missingValueWeights);
			}

			adjustment = MeasureUtil.calculateAdjustment(values, adjustmentValues);
		} else

		{
			adjustment = MeasureUtil.calculateAdjustment(values);
		}

		BiMap<Cluster, String> inverseEntities = (getEntityRegistry().inverse());

		List<Cluster> clusters = clusteringModel.getClusters();
		for(Cluster cluster : clusters){
			List<FieldValue> clusterValues = CacheUtil.getValue(cluster, ClusteringModelEvaluator.clusterValueCache);

			if(values.size() != clusterValues.size()){
				throw new InvalidFeatureException(cluster);
			}

			String id = inverseEntities.get(cluster);

			Double distance = MeasureUtil.evaluateDistance(comparisonMeasure, clusteringFields, values, clusterValues, adjustment);

			result.put(cluster, id, distance);
		}

		return Collections.singletonMap(getTargetField(), result);
	}

	private List<ClusteringField> getCenterClusteringFields(){
		ClusteringModel clusteringModel = getModel();

		List<ClusteringField> result = Lists.newArrayList();

		List<ClusteringField> clusteringFields = clusteringModel.getClusteringFields();
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

	private static final LoadingCache<Cluster, List<FieldValue>> clusterValueCache = CacheBuilder.newBuilder()
		.weakKeys()
		.build(new CacheLoader<Cluster, List<FieldValue>>(){

			@Override
			public List<FieldValue> load(Cluster cluster){
				Array array = cluster.getArray();

				List<FieldValue> result = Lists.newArrayList();

				List<? extends Number> values = ArrayUtil.getNumberContent(array);
				for(Number value : values){
					result.add(FieldValueUtil.create(value));
				}

				return result;
			}
		});

	private static final LoadingCache<ClusteringModel, BiMap<String, Cluster>> entityCache = CacheBuilder.newBuilder()
		.weakKeys()
		.build(new CacheLoader<ClusteringModel, BiMap<String, Cluster>>(){

			@Override
			public BiMap<String, Cluster> load(ClusteringModel clusteringModel){
				BiMap<String, Cluster> result = HashBiMap.create();

				EntityUtil.putAll(clusteringModel.getClusters(), result);

				return result;
			}
		});
}