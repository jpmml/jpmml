/*
 * Copyright (c) 2013 University of Tartu
 */
package org.jpmml.evaluator;

import java.util.*;

import org.jpmml.manager.*;

import org.dmg.pmml.*;

import com.google.common.collect.*;

public class AssociationModelEvaluator extends AssociationModelManager implements Evaluator {

	private BiMap<String, AssociationRule> entities = null;


	public AssociationModelEvaluator(PMML pmml){
		super(pmml);
	}

	public AssociationModelEvaluator(PMML pmml, AssociationModel associationModel){
		super(pmml, associationModel);
	}

	public AssociationModelEvaluator(AssociationModelManager parent){
		super(parent.getPmml(), parent.getModel());
	}

	@Override
	public BiMap<String, AssociationRule> getEntities(){

		if(this.entities == null){
			this.entities = super.getEntities();
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

		List<Item> items = getItems();

		Set<String> input = createInput((Collection<?>)value, items);

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

		Association association = new Association(associationRules, antecedentFlags, consequentFlags);

		return Collections.singletonMap(getTargetField(), association);
	}

	private Set<String> createInput(Collection<?> values, Collection<Item> items){
		Set<String> result = Sets.newLinkedHashSet();

		Map<String, Item> valueMap = Maps.newLinkedHashMap();

		for(Item item : items){
			valueMap.put(item.getValue(), item);
		}

		values:
		for(Object value : values){
			String stringValue = (String)ParameterUtil.cast(DataType.STRING, value);

			Item item = valueMap.get(stringValue);
			if(item == null){
				continue values;
			}

			result.add(item.getId());
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

		return true;
	}
}