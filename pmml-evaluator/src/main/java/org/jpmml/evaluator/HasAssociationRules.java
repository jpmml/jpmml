/*
 * Copyright (c) 2013 University of Tartu
 */
package org.jpmml.evaluator;

import java.util.*;

import org.dmg.pmml.*;

import com.google.common.collect.*;

public interface HasAssociationRules {

	BiMap<String, Item> getItemRegistry();

	BiMap<String, Itemset> getItemsetRegistry();

	BiMap<String, AssociationRule> getAssociationRuleRegistry();

	List<AssociationRule> getAssociationRules(OutputField.Algorithm algorithm);
}