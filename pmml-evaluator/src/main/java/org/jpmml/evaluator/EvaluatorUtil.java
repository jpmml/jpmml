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

	@SuppressWarnings (
		value = {"rawtypes", "unchecked"}
	)
	static
	public List<Map<FieldName, Object>> groupRows(FieldName groupField, List<Map<FieldName, Object>> table){
		Map<Object, Map<FieldName, List<Object>>> groupedRows = new LinkedHashMap<Object, Map<FieldName, List<Object>>>();

		for(int i = 0; i < table.size(); i++){
			Map<FieldName, ?> row = table.get(i);

			Object groupValue = row.get(groupField);

			Map<FieldName, List<Object>> groupedRow = groupedRows.get(groupValue);
			if(groupedRow == null){
				groupedRow = new LinkedHashMap<FieldName, List<Object>>();

				groupedRows.put(groupValue, groupedRow);
			}

			Collection<? extends Map.Entry<FieldName, ?>> entries = row.entrySet();
			for(Map.Entry<FieldName, ?> entry : entries){
				FieldName key = entry.getKey();
				Object value = entry.getValue();

				// Drop the group column from the table
				if((groupField).equals(key)){
					continue;
				}

				List<Object> values = groupedRow.get(key);
				if(values == null){
					values = new ArrayList<Object>();

					groupedRow.put(key, values);
				}

				values.add(value);
			}
		}

		List<Map<FieldName, Object>> result = new ArrayList<Map<FieldName, Object>>();

		Collection<Map<FieldName, List<Object>>> values = groupedRows.values();
		for(Map<FieldName, List<Object>> value : values){
			result.add((Map)value);
		}

		return result;
	}
}