/*
 * Copyright (c) 2013 Villu Ruusmann
 */
package org.jpmml.evaluator;

import java.util.*;

import org.jpmml.manager.*;

import org.dmg.pmml.*;

import com.google.common.annotations.*;
import com.google.common.collect.*;

@Beta
abstract
public class Association implements Computable, HasRuleValues {

	private List<AssociationRule> associationRules = null;

	private BitSet antecedentFlags = null;

	private BitSet consequentFlags = null;


	protected Association(List<AssociationRule> associationRules, BitSet antecedentFlags, BitSet consequentFlags){
		setAssociationRules(associationRules);

		setAntecedentFlags(antecedentFlags);
		setConsequentFlags(consequentFlags);
	}

	/**
	 * @throws MissingResultException Always.
	 */
	@Override
	public Object getResult(){
		throw new MissingResultException(null);
	}

	@Override
	public List<AssociationRule> getRuleValues(OutputField.Algorithm algorithm){
		List<AssociationRule> associationRules = getAssociationRules();

		BitSet flags;

		switch(algorithm){
			// "a rule is selected if its antecedent itemset is a subset of the input itemset"
			case RECOMMENDATION:
				flags = getAntecedentFlags();
				break;
			// "a rule is selected if its antecedent itemset is a subset of the input itemset, and its consequent itemset is not a subset of the input itemset"
			case EXCLUSIVE_RECOMMENDATION:
				flags = (BitSet)getAntecedentFlags().clone();
				flags.andNot(getConsequentFlags());
				break;
			// "a rule is selected if its antecedent and consequent itemsets are included in the input itemset"
			case RULE_ASSOCIATION:
				flags = (BitSet)getAntecedentFlags().clone();
				flags.and(getConsequentFlags());
				break;
			default:
				throw new UnsupportedFeatureException(null, algorithm);
		}

		List<AssociationRule> result = Lists.newArrayList();

		for(int i = flags.nextSetBit(0); i > -1; i = flags.nextSetBit(i + 1)){
			AssociationRule associationRule = associationRules.get(i);

			result.add(associationRule);
		}

		return result;
	}

	public List<AssociationRule> getAssociationRules(){
		return this.associationRules;
	}

	private void setAssociationRules(List<AssociationRule> associationRules){
		this.associationRules = associationRules;
	}

	public BitSet getAntecedentFlags(){
		return this.antecedentFlags;
	}

	private void setAntecedentFlags(BitSet antecedentFlags){
		this.antecedentFlags = antecedentFlags;
	}

	public BitSet getConsequentFlags(){
		return this.consequentFlags;
	}

	private void setConsequentFlags(BitSet consequentFlags){
		this.consequentFlags = consequentFlags;
	}
}