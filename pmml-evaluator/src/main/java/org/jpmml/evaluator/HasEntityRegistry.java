/*
 * Copyright (c) 2013 University of Tartu
 */
package org.jpmml.evaluator;

import org.dmg.pmml.*;

import com.google.common.collect.*;

public interface HasEntityRegistry<E extends Entity> {

	/**
	 * Takes the snapshot of all known (ie. registered with the class model) Entity instances.
	 *
	 * @return A bidirectional map between {@link Entity#getId() Entity identifiers} and {@link Entity Entity instances}.
	 */
	BiMap<String, E> getEntityRegistry();
}