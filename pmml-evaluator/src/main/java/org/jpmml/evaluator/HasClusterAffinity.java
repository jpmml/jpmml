/*
 * Copyright (c) 2013 Villu Ruusmann
 */
package org.jpmml.evaluator;

import org.dmg.pmml.*;

/**
 * @see ResultFeatureType#CLUSTER_AFFINITY
 */
public interface HasClusterAffinity extends ResultFeature {

	Double getClusterAffinity();
}