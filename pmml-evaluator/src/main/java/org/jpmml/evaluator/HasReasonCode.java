/*
 * Copyright (c) 2013 University of Tartu
 */
package org.jpmml.evaluator;

import org.dmg.pmml.*;

/**
 * @see ResultFeatureType#REASON_CODE
 */
public interface HasReasonCode {

	/**
	 * @param The rank of the reason code. The rank of the top reason code is <code>1</code>.
	 */
	String getReasonCode(int rank);
}