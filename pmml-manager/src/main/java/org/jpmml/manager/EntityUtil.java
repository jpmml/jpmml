/*
 * Copyright (c) 2013 University of Tartu
 */
package org.jpmml.manager;

import java.util.*;

import org.dmg.pmml.*;

import com.google.common.collect.*;

public class EntityUtil {

	private EntityUtil(){
	}

	static
	public <E extends PMMLObject & HasId> void put(E entity, BiMap<String, E> map){
		String id = entity.getId();
		if(id == null || map.containsKey(id)){
			throw new InvalidFeatureException(entity);
		}

		map.put(id, entity);
	}

	static
	public <E extends PMMLObject & HasId> void putAll(List<E> entities, BiMap<String, E> map){

		for(int i = 0, j = 1; i < entities.size(); i++, j++){
			E entity = entities.get(i);

			String id = entity.getId();

			// Generate an implicit identifier (ie. 1-based index) if the explicit identifier is missing
			if(id == null){
				id = String.valueOf(j);
			} // End if

			if(map.containsKey(id)){
				throw new InvalidFeatureException(entity);
			}

			map.put(id, entity);
		}
	}
}