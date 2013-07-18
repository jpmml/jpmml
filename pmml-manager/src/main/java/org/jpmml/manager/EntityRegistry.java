/*
 * Copyright (c) 2013 University of Tartu
 */
package org.jpmml.manager;

import org.dmg.pmml.*;

import com.google.common.collect.*;

public interface EntityRegistry<E extends Entity> {

	/**
	 * @return Map of all known {@link Entity} instances.
	 */
	BiMap<String, E> getEntities();
}