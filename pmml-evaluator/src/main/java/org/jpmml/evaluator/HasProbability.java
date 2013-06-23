/*
 * Copyright (c) 2013 University of Tartu
 */
package org.jpmml.evaluator;

import org.dmg.pmml.*;

/**
 * @see ResultFeatureType#PROBABILITY
 */
public interface HasProbability {

	Double getProbability(String value);
}