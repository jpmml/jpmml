/*
 * Copyright, KNIME.com AG, Zurich, Switzerland
 */
package org.jpmml.manager;

import org.dmg.pmml.*;

import com.google.common.cache.*;
import com.google.common.util.concurrent.*;

public class CacheUtil {

	private CacheUtil(){
	}

	static
	public <K extends PMMLObject, V> V getValue(K key, LoadingCache<K, V> cache){

		try {
			return cache.getUnchecked(key);
		} catch(UncheckedExecutionException uee){
			Throwable cause = uee.getCause();

			if(cause instanceof PMMLException){
				throw (PMMLException)cause;
			}

			throw new InvalidFeatureException(key);
		}
	}
}