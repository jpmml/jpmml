/*
 * Copyright (c) 2013 University of Tartu
 */
package org.jpmml.evaluator;

import org.dmg.pmml.*;

/**
 * @see ResultFeatureType#PREDICTED_DISPLAY_VALUE
 */
public interface HasDisplayValue extends ResultFeature {

	String getDisplayValue();
}