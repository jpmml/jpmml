/*
 * Copyright (c) 2013 University of Tartu
 */
package org.jpmml.evaluator;

import java.util.*;

import org.jpmml.manager.*;

import org.dmg.pmml.*;

import com.google.common.collect.*;

public class AssociationModelEvaluator extends AssociationModelManager implements Evaluator {

	private BiMap<String, Item> items = null;

	private BiMap<String, Itemset> itemsets = null;

	private BiMap<String, AssociationRule> entities = null;

	private BiMap<String, String> itemValues = null;


	public AssociationModelEvaluator(PMML pmml){
		super(pmml);
	}

	public AssociationModelEvaluator(PMML pmml, AssociationModel associationModel){
		super(pmml, associationModel);
	}

	@Override
	public BiMap<String, Item> getItemRegistry(){

		if(this.items == null){
			this.items = super.getItemRegistry();
		}

		return this.items;
	}

	@Override
	public BiMap<String, Itemset> getItemsetRegistry(){

		if(this.itemsets == null){
			this.itemsets = super.getItemsetRegistry();
		}

		return this.itemsets;
	}

	@Override
	public BiMap<String, AssociationRule> getEntityRegistry(){

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

	Map<FieldName, ?> evaluate(EvaluationContext context){
		AssociationModel associationModel = getModel();

		FieldName activeField = getActiveField();

		Object value = context.getArgument(activeField);
		if(value == null){
			throw new MissingFieldException(activeField, associationModel);
		}

		Set<String> input = createInput((Collection<?>)value);

		Map<String, Boolean> flags = Maps.newLinkedHashMap();

		List<Itemset> itemsets = getItemsets();
		for(Itemset itemset : itemsets){
			flags.put(itemset.getId(), isSubset(input, itemset));
		}

		List<AssociationRule> associationRules = getAssociationRules();

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
	private Set<String> createInput(Collection<?> values){
		Set<String> result = Sets.newLinkedHashSet();

		Map<String, String> valueItems = (getItemValues().inverse());

		values:
		for(Object value : values){
			String stringValue = (String)ParameterUtil.cast(DataType.STRING, value);

			String id = valueItems.get(stringValue);
			if(id == null){
				continue values;
			}

			result.add(id);
		}

		return result;
	}

	/**
	 * @return A bidirectional map between {@link Item#getId() Item identifiers} and {@link Item#getValue() Item values}.
	 */
	private BiMap<String, String> getItemValues(){

		if(this.itemValues == null){
			this.itemValues = createItemValues();
		}

		return this.itemValues;
	}

	private BiMap<String, String> createItemValues(){
		BiMap<String, String> result = HashBiMap.create();

		List<Item> items = getItems();
		for(Item item : items){
			result.put(item.getId(), item.getValue());
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
}