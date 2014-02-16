/*
 * Copyright (c) 2013 Villu Ruusmann
 */
package org.jpmml.evaluator;

public interface HasConfidence extends ResultFeature {

	Double getConfidence(String value);
}