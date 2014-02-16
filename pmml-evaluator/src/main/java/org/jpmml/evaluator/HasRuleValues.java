/*
 * Copyright (c) 2013 Villu Ruusmann
 */
package org.jpmml.evaluator;

import java.util.*;

import org.dmg.pmml.*;

import com.google.common.collect.*;

/**
 * @see ResultFeatureType#RULE_VALUE
 */
public interface HasRuleValues extends ResultFeature {

	BiMap<String, Item> getItemRegistry();

	BiMap<String, Itemset> getItemsetRegistry();

	BiMap<String, AssociationRule> getAssociationRuleRegistry();

	List<AssociationRule> getRuleValues(OutputField.Algorithm algorithm);
}