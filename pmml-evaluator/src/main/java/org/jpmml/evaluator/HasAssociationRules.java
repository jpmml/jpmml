/*
 * Copyright (c) 2013 University of Tartu
 */
package org.jpmml.evaluator;

import java.util.*;

import org.dmg.pmml.*;

public interface HasAssociationRules {

	List<AssociationRule> getAssociationRules(OutputField.Algorithm algorithm);
}