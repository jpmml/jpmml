/*
 * Copyright (c) 2013 Villu Ruusmann
 */
package org.jpmml.evaluator;

import org.dmg.pmml.*;

/**
 * @see ResultFeatureType#AFFINITY
 */
public interface HasAffinity extends ResultFeature {

	Double getAffinity(String value);
}