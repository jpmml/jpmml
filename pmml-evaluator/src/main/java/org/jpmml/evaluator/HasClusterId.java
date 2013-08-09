/*
 * Copyright (c) 2013 University of Tartu
 */
package org.jpmml.evaluator;

import org.dmg.pmml.*;

/**
 * @see ResultFeatureType#CLUSTER_ID
 */
public interface HasClusterId extends ResultFeature {

	String getClusterId();
}