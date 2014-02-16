/*
 * Copyright (c) 2013 Villu Ruusmann
 */
package org.jpmml.evaluator;

import org.dmg.pmml.*;

/**
 * @see ResultFeatureType#PROBABILITY
 */
public interface HasProbability extends ResultFeature {

	Double getProbability(String value);
}