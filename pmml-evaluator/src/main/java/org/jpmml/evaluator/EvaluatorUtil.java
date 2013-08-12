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
			result.put(FieldName.create(entry.getKey()), entry.getValue());
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
	public <K> List<Map<K, Object>> groupRows(K groupKey, List<Map<K, Object>> table){
		Map<Object, ListMultimap<K, Object>> groupedRows = Maps.newLinkedHashMap();

		for(int i = 0; i < table.size(); i++){
			Map<K, ?> row = table.get(i);

			Object groupValue = row.get(groupKey);

			ListMultimap<K, Object> groupedRow = groupedRows.get(groupValue);
			if(groupedRow == null){
				groupedRow = ArrayListMultimap.create();

				groupedRows.put(groupValue, groupedRow);
			}

			Collection<? extends Map.Entry<K, ?>> entries = row.entrySet();
			for(Map.Entry<K, ?> entry : entries){
				K key = entry.getKey();
				Object value = entry.getValue();

				groupedRow.put(key, value);
			}
		}

		List<Map<K, Object>> resultTable = Lists.newArrayList();

		Collection<Map.Entry<Object, ListMultimap<K, Object>>> entries = groupedRows.entrySet();
		for(Map.Entry<Object, ListMultimap<K, Object>> entry : entries){
			Map<K, Object> resultRow = Maps.newLinkedHashMap();
			resultRow.putAll((entry.getValue()).asMap());

			// The value of the "group by" column is a single Object, not a Collection (ie. java.util.List) of Objects
			resultRow.put(groupKey, entry.getKey());

			resultTable.add(resultRow);
		}

		return resultTable;
	}
}