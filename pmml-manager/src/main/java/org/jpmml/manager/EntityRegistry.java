/*
 * Copyright (c) 2013 University of Tartu
 */
package org.jpmml.manager;

import java.util.*;

import org.dmg.pmml.*;

public interface EntityRegistry<E extends Entity> {

	/**
	 * @return Map of all known {@link Entity} instances.
	 */
	Map<String, E> getEntities();
}