/*
 * Copyright (c) 2013 University of Tartu
 */
package org.jpmml.evaluator;

import java.util.*;

import org.dmg.pmml.*;

import com.google.common.collect.*;

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
		Map<FieldName, V> result = Maps.newLinkedHashMap();

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
		Map<String, V> result = Maps.newLinkedHashMap();

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
		Map<K, Object> result = Maps.newLinkedHashMap();

		Collection<? extends Map.Entry<K, ?>> entries = map.entrySet();
		for(Map.Entry<K, ?> entry : entries){
			result.put(entry.getKey(), decode(entry.getValue()));
		}

		return result;
	}

	static
	public List<Map<FieldName, Object>> groupRows(FieldName groupField, List<Map<FieldName, Object>> table){
		Map<Object, ListMultimap<FieldName, Object>> groupedRows = Maps.newLinkedHashMap();

		Set<FieldName> keys = Sets.newLinkedHashSet();

		for(int i = 0; i < table.size(); i++){
			Map<FieldName, ?> row = table.get(i);

			Object groupValue = row.get(groupField);

			ListMultimap<FieldName, Object> groupedRow = groupedRows.get(groupValue);
			if(groupedRow == null){
				groupedRow = ArrayListMultimap.create();

				groupedRows.put(groupValue, groupedRow);
			}

			Collection<? extends Map.Entry<FieldName, ?>> entries = row.entrySet();
			for(Map.Entry<FieldName, ?> entry : entries){
				FieldName key = entry.getKey();
				Object value = entry.getValue();

				keys.add(key);

				if((groupField).equals(key)){
					continue;
				}

				groupedRow.put(key, value);
			}
		}

		List<Map<FieldName, Object>> result = Lists.newArrayList();

		Collection<Map.Entry<Object, ListMultimap<FieldName, Object>>> entries = groupedRows.entrySet();
		for(Map.Entry<Object, ListMultimap<FieldName, Object>> entry : entries){
			Map<FieldName, Object> row = Maps.newLinkedHashMap();

			Object groupValue = entry.getKey();

			ListMultimap<FieldName, Object> groupedRow = entry.getValue();

			for(FieldName key : keys){

				// The group field holds an Object
				if((groupField).equals(key)){
					row.put(key, groupValue);
				} else

				// All other fields hold collections (ie. java.util.List) of Objects
				{
					row.put(key, groupedRow.get(key));
				}
			}

			result.add(row);
		}

		return result;
	}
}