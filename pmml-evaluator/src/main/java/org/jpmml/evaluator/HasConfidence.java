/*
 * Copyright (c) 2013 University of Tartu
 */
package org.jpmml.evaluator;

public interface HasConfidence extends ResultFeature {

	Double getConfidence(String value);
}