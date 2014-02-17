/*
 * Copyright (c) 2013 KNIME.com AG, Zurich, Switzerland
 */
package org.jpmml.evaluator;

import java.util.*;

public interface HasEntityIdRanking extends HasEntityId {

	List<String> getEntityIdRanking();
}