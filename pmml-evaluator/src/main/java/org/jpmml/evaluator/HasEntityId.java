/*
 * Copyright (c) 2013 Villu Ruusmann
 */
package org.jpmml.evaluator;

import org.dmg.pmml.*;

/**
 * @see ResultFeatureType#ENTITY_ID
 */
public interface HasEntityId extends ResultFeature {

	String getEntityId();
}