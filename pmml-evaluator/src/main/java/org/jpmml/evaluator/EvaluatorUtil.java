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
	 * @see Computable
	 */
	static
	public Object decode(Object object){

		if(object instanceof Computable){
			Computable<?> computable = (Computable<?>)object;

			return computable.getResult();
		}

		return object;
	}

	/**
	 * Decouples a {@link Map} instance from the current runtime environment by decoding both its keys and values.
	 *
	 * @see #decodeKeys(Map)
	 * @see #decodeValues(Map)
	 */
	static
	public Map<String, ?> decode(Map<FieldName, ?> map){
		return decodeKeys(decodeValues(map));
	}

	/**
	 * Replaces String keys with {@link FieldName} keys.
	 */
	static
	public <V> Map<FieldName, V> encodeKeys(Map<String, V> map){
		Map<FieldName, V> result = new LinkedHashMap<FieldName, V>();

		Collection<Map.Entry<String, V>> entries = map.entrySet();
		for(Map.Entry<String, V> entry : entries){
			result.put(new FieldName(entry.getKey()), entry.getValue());
		}

		return result;
	}

	/**
	 * Replaces {@link FieldName} keys with String keys.
	 *
	 * @see FieldName#getValue()
	 */
	static
	public <V> Map<String, V> decodeKeys(Map<FieldName, V> map){
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
	public <K> Map<K, ?> decodeValues(Map<K, ?> map){
		Map<K, Object> result = new LinkedHashMap<K, Object>();

		Collection<? extends Map.Entry<K, ?>> entries = map.entrySet();
		for(Map.Entry<K, ?> entry : entries){
			result.put(entry.getKey(), decode(entry.getValue()));
		}

		return result;
	}
}