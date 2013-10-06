/*
 * Copyright (c) 2013 University of Tartu
 */
package org.jpmml.manager;

import java.util.*;

import org.dmg.pmml.*;

import com.google.common.cache.*;
import com.google.common.collect.*;

import static com.google.common.base.Preconditions.*;

public class ClusteringModelManager extends ModelManager<ClusteringModel> implements HasEntityRegistry<Cluster> {

	private ClusteringModel clusteringModel = null;


	public ClusteringModelManager(){
	}

	public ClusteringModelManager(PMML pmml){
		this(pmml, find(pmml.getModels(), ClusteringModel.class));
	}

	public ClusteringModelManager(PMML pmml, ClusteringModel clusteringModel){
		super(pmml);

		this.clusteringModel = clusteringModel;
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
	public ClusteringModel getModel(){
		checkState(this.clusteringModel != null);

		return this.clusteringModel;
	}

	/**
	 * @see #getModel()
	 */
	public ClusteringModel createModel(ComparisonMeasure comparisonMeasure, ClusteringModel.ModelClass modelClass){
		checkState(this.clusteringModel == null);

		this.clusteringModel = new ClusteringModel(new MiningSchema(), comparisonMeasure, MiningFunctionType.CLUSTERING, modelClass, 0);

		getModels().add(this.clusteringModel);

		return this.clusteringModel;
	}

	@Override
	public BiMap<String, Cluster> getEntityRegistry(){
		return getValue(ClusteringModelManager.entityCache);
	}

	public List<ClusteringField> getClusteringFields(){
		ClusteringModel clusteringModel = getModel();

		return clusteringModel.getClusteringFields();
	}

	public List<Cluster> getClusters(){
		ClusteringModel clusteringModel = getModel();

		return clusteringModel.getClusters();
	}

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