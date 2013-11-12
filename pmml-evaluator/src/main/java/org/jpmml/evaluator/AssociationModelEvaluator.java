/*
 * Copyright (c) 2013 University of Tartu
 */
package org.jpmml.evaluator;

import java.util.*;

import org.jpmml.manager.*;

import org.dmg.pmml.*;

import com.google.common.cache.*;
import com.google.common.collect.*;

public class AssociationModelEvaluator extends ModelEvaluator<AssociationModel> implements HasEntityRegistry<AssociationRule> {

	public AssociationModelEvaluator(PMML pmml){
		this(pmml, find(pmml.getModels(), AssociationModel.class));
	}

	public AssociationModelEvaluator(PMML pmml, AssociationModel associationModel){
		super(pmml, associationModel);
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
	public BiMap<String, AssociationRule> getEntityRegistry(){
		return getValue(AssociationModelEvaluator.entityCache);
	}

	@Override
	public Map<FieldName, ?> evaluate(Map<FieldName, ?> arguments){
		AssociationModel associationModel = getModel();
		if(!associationModel.isScorable()){
			throw new InvalidResultException(associationModel);
		}

		Map<FieldName, ?> predictions;

		ModelManagerEvaluationContext context = new ModelManagerEvaluationContext(this, arguments);

		MiningFunctionType miningFunction = associationModel.getFunctionName();
		switch(miningFunction){
			case ASSOCIATION_RULES:
				predictions = evaluate(context);
				break;
			default:
				throw new UnsupportedFeatureException(associationModel, miningFunction);
		}

		return OutputUtil.evaluate(predictions, context);
	}

	private Map<FieldName, ?> evaluate(EvaluationContext context){
		AssociationModel associationModel = getModel();

		FieldName activeField = getActiveField();

		FieldValue value = context.getArgument(activeField);
		if(value == null){
			throw new MissingFieldException(activeField, associationModel);
		}

		Collection<?> values;

		try {
			values = (Collection<?>)FieldValueUtil.getValue(value);
		} catch(ClassCastException cce){
			throw new TypeCheckException(Collection.class, value);
		}

		Set<String> input = createInput(values, context);

		Map<String, Boolean> flags = Maps.newLinkedHashMap();

		List<Itemset> itemsets = associationModel.getItemsets();
		for(Itemset itemset : itemsets){
			flags.put(itemset.getId(), isSubset(input, itemset));
		}

		List<AssociationRule> associationRules = associationModel.getAssociationRules();

		BitSet antecedentFlags = new BitSet(associationRules.size());
		BitSet consequentFlags = new BitSet(associationRules.size());

		for(int i = 0; i < associationRules.size(); i++){
			AssociationRule associationRule = associationRules.get(i);

			Boolean antecedentFlag = flags.get(associationRule.getAntecedent());
			if(antecedentFlag == null){
				throw new InvalidFeatureException(associationRule);
			}

			antecedentFlags.set(i, antecedentFlag);

			Boolean consequentFlag = flags.get(associationRule.getConsequent());
			if(consequentFlag == null){
				throw new InvalidFeatureException(associationRule);
			}

			consequentFlags.set(i, consequentFlag);
		}

		Association association = new Association(associationRules, antecedentFlags, consequentFlags){

			@Override
			public BiMap<String, Item> getItemRegistry(){
				return AssociationModelEvaluator.this.getItemRegistry();
			}

			@Override
			public BiMap<String, Itemset> getItemsetRegistry(){
				return AssociationModelEvaluator.this.getItemsetRegistry();
			}

			@Override
			public BiMap<String, AssociationRule> getAssociationRuleRegistry(){
				return AssociationModelEvaluator.this.getEntityRegistry();
			}
		};

		return Collections.singletonMap(getTargetField(), association);
	}

	/**
	 * @return A set of {@link Item#getId() Item identifiers}.
	 */
	private Set<String> createInput(Collection<?> values, EvaluationContext context){
		Set<String> result = Sets.newLinkedHashSet();

		Map<String, String> valueItems = (getItemValues().inverse());

		values:
		for(Object value : values){
			String stringValue = TypeUtil.format(value);

			String id = valueItems.get(stringValue);
			if(id == null){
				context.addWarning("Unknown item value \"" + stringValue + "\"");

				continue values;
			}

			result.add(id);
		}

		return result;
	}

	static
	private boolean isSubset(Set<String> input, Itemset itemset){
		boolean result = true;

		List<ItemRef> itemRefs = itemset.getItemRefs();
		for(ItemRef itemRef : itemRefs){
			result &= input.contains(itemRef.getItemRef());

			if(!result){
				return false;
			}
		}

		return result;
	}

	/**
	 * @return A bidirectional map between {@link Item#getId Item identifiers} and {@link Item instances}.
	 */
	private BiMap<String, Item> getItemRegistry(){
		return getValue(AssociationModelEvaluator.itemCache);
	}

	/**
	 * @return A bidirectional map between {@link Itemset#getId() Itemset identifiers} and {@link Itemset instances}.
	 */
	private BiMap<String, Itemset> getItemsetRegistry(){
		return getValue(AssociationModelEvaluator.itemsetCache);
	}

	/**
	 * @return A bidirectional map between {@link Item#getId() Item identifiers} and {@link Item#getValue() Item values}.
	 */
	private BiMap<String, String> getItemValues(){
		return getValue(AssociationModelEvaluator.itemValueCache);
	}

	static
	private BiMap<String, String> parseItemValues(AssociationModel associationModel){
		BiMap<String, String> result = HashBiMap.create();

		List<Item> items = associationModel.getItems();
		for(Item item : items){
			result.put(item.getId(), item.getValue());
		}

		return result;
	}

	private static final LoadingCache<AssociationModel, BiMap<String, AssociationRule>> entityCache = CacheBuilder.newBuilder()
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

	private static final LoadingCache<AssociationModel, BiMap<String, String>> itemValueCache = CacheBuilder.newBuilder()
		.weakKeys()
		.build(new CacheLoader<AssociationModel, BiMap<String, String>>(){

			@Override
			public BiMap<String, String> load(AssociationModel associationModel){
				return parseItemValues(associationModel);
			}
		});
}