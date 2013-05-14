/*
 * Copyright (c) 2013 University of Tartu
 */
package org.jpmml.evaluator;

import java.util.*;

import org.dmg.pmml.*;

public class EvaluatorUtil {

	private EvaluatorUtil(){
	}

	/**
	 * Decouples a {@link Map} instance from the current runtime environment by simplifying both its keys and values.
	 *
	 * @see #simplifyKeys(Map)
	 * @see #simplifyValues(Map)
	 */
	static
	public Map<String, ?> simplify(Map<FieldName, ?> map){
		return simplifyKeys(simplifyValues(map));
	}

	/**
	 * Replaces {@link FieldName} keys with String keys.
	 *
	 * @see FieldName#getValue()
	 */
	static
	public <V> Map<String, V> simplifyKeys(Map<FieldName, V> map){
		Map<String, V> result = new LinkedHashMap<String, V>();

		Collection<Map.Entry<FieldName, V>> entries = map.entrySet();
		for(Map.Entry<FieldName, V> entry : entries){
			result.put((entry.getKey()).getValue(), entry.getValue());
		}

		return result;
	}

	/**
	 * Replaces {@link Computable} complex values with simple values.
	 *
	 * @see Computable
	 */
	static
	public <K> Map<K, ?> simplifyValues(Map<K, ?> map){
		Map<K, Object> result = new LinkedHashMap<K, Object>();

		Collection<? extends Map.Entry<K, ?>> entries = map.entrySet();
		for(Map.Entry<K, ?> entry : entries){
			result.put(entry.getKey(), compute(entry.getValue()));
		}

		return result;
	}

	static
	private Object compute(Object object){

		if(object instanceof Computable){
			Computable<?> computable = (Computable<?>)object;

			return computable.getResult();
		}

		return object;
	}
}