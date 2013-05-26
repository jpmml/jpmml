/*
 * Copyright (c) 2013 University of Tartu
 */
package org.jpmml.evaluator;

public interface Classification extends Computable<String> {

	Double getProbability(String value);
}