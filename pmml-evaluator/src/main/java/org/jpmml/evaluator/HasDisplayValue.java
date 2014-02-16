/*
 * Copyright (c) 2013 Villu Ruusmann
 */
package org.jpmml.evaluator;

import org.dmg.pmml.*;

/**
 * @see ResultFeatureType#PREDICTED_DISPLAY_VALUE
 */
public interface HasDisplayValue extends ResultFeature {

	String getDisplayValue();
}