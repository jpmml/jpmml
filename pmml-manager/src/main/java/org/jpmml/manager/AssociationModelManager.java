/*
 * Copyright (c) 2013 University of Tartu
 */
package org.jpmml.manager;

import java.util.*;
import java.util.concurrent.*;

import org.dmg.pmml.*;

import com.google.common.cache.*;
import com.google.common.collect.*;

import static com.google.common.base.Preconditions.*;

public class AssociationModelManager extends ModelManager<AssociationModel> implements HasEntityRegistry<AssociationRule> {

	private AssociationModel associationModel = null;


	public AssociationModelManager(){
	}

	public AssociationModelManager(PMML pmml){
		this(pmml, find(pmml.getModels(), AssociationModel.class));
	}

	public AssociationModelManager(PMML pmml, AssociationModel associationModel){
		super(pmml);

		this.associationModel = associationModel;
	}

	@Override
	public String getSummary(){
		return "Association rules";
	}

	public FieldName getActiveField(){
		List<FieldName> activeFields = getActiveFields();

		if(activeFields.size() < 1){
			throw new InvalidFeatureException("No active fields", getMiningSchema());
		} else

		if(activeFields.size() > 1){
			throw new InvalidFeatureException("Too many active fields", getMiningSchema());
		}

		return activeFields.get(0);
	}

	/**
	 * @return <code>null</code> Always.
	 */
	@Override
	public Target getTarget(FieldName name){
		return null;
	}

	@Override
	public AssociationModel getModel(){
		checkState(this.associationModel != null);

		return this.associationModel;
	}

	/**
	 * @see #getModel()
	 */
	public AssociationModel createModel(Double minimumSupport, Double minimumConfidence){
		checkState(this.associationModel == null);

		this.associationModel = new AssociationModel(new MiningSchema(), MiningFunctionType.ASSOCIATION_RULES, 0, minimumSupport, minimumConfidence, 0, 0, 0);

		getModels().add(this.associationModel);

		return this.associationModel;
	}

	/**
	 * @return A bidirectional map between {@link Item#getId Item identifiers} and {@link Item instances}.
	 */
	public BiMap<String, Item> getItemRegistry(){
		AssociationModel associationModel = getModel();

		try {
			return AssociationModelManager.itemCache.get(associationModel);
		} catch(ExecutionException ee){
			throw new InvalidFeatureException(associationModel);
		}
	}

	/**
	 * @return A bidirectional map between {@link Itemset#getId() Itemset identifiers} and {@link Itemset instances}.
	 */
	public BiMap<String, Itemset> getItemsetRegistry(){
		AssociationModel associationModel = getModel();

		try {
			return AssociationModelManager.itemsetCache.get(associationModel);
		} catch(ExecutionException ee){
			throw new InvalidFeatureException(associationModel);
		}
	}

	@Override
	public BiMap<String, AssociationRule> getEntityRegistry(){
		AssociationModel associationModel = getModel();

		try {
			return AssociationModelManager.cache.get(associationModel);
		} catch(ExecutionException ee){
			throw new InvalidFeatureException(associationModel);
		}
	}

	public List<Item> getItems(){
		AssociationModel associationModel = getModel();

		return associationModel.getItems();
	}

	public List<Itemset> getItemsets(){
		AssociationModel associationModel = getModel();

		return associationModel.getItemsets();
	}

	public List<AssociationRule> getAssociationRules(){
		AssociationModel associationModel = getModel();

		return associationModel.getAssociationRules();
	}

	private static final LoadingCache<AssociationModel, BiMap<String, AssociationRule>> cache = CacheBuilder.newBuilder()
		.weakKeys()
		.build(new CacheLoader<AssociationModel, BiMap<String, AssociationRule>>(){

			@Override
			public BiMap<String, AssociationRule> load(AssociationModel associationModel){
				BiMap<String, AssociationRule> result = HashBiMap.create();

				EntityUtil.putAll(associationModel.getAssociationRules(), result);

				return result;
			}
		});

	private static final LoadingCache<AssociationModel, BiMap<String, Item>> itemCache = CacheBuilder.newBuilder()
		.weakKeys()
		.build(new CacheLoader<AssociationModel, BiMap<String, Item>>(){

			@Override
			public BiMap<String, Item> load(AssociationModel associationModel){
				BiMap<String, Item> result = HashBiMap.create();

				EntityUtil.putAll(associationModel.getItems(), result);

				return result;
			}
		});

	private static final LoadingCache<AssociationModel, BiMap<String, Itemset>> itemsetCache = CacheBuilder.newBuilder()
		.weakKeys()
		.build(new CacheLoader<AssociationModel, BiMap<String, Itemset>>(){

			@Override
			public BiMap<String, Itemset> load(AssociationModel associationModel){
				BiMap<String, Itemset> result = HashBiMap.create();

				EntityUtil.putAll(associationModel.getItemsets(), result);

				return result;
			}
		});
}