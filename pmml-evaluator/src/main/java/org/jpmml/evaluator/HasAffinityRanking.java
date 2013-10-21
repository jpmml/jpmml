/*
 * Copyright, KNIME.com AG, Zurich, Switzerland
 */
package org.jpmml.evaluator;

import java.util.*;

public interface HasAffinityRanking extends HasAffinity {

	List<Double> getAffinityRanking();
}