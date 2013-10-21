/*
 * Copyright, KNIME.com AG, Zurich, Switzerland
 */
package org.jpmml.evaluator;

import java.util.*;

public interface HasEntityIdRanking extends HasEntityId {

	List<String> getEntityIdRanking();
}