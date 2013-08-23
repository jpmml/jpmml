/*
 * Copyright (c) 2013 University of Tartu
 */
package org.jpmml.evaluator;

import java.util.*;

import org.dmg.pmml.*;

import com.google.common.collect.*;

import org.w3c.dom.*;

public class TableUtil {

	private TableUtil(){
	}

	static
	public List<Map<String, String>> parse(InlineTable table){
		List<Map<String, String>> result = Lists.newArrayList();

		List<Row> rows = table.getRows();
		for(Row row : rows){
			Map<String, String> map = Maps.newLinkedHashMap();

			List<Object> cells = row.getContent();
			for(Object cell : cells){

				if(cell instanceof Element){
					Element element = (Element)cell;

					map.put(element.getTagName(), element.getTextContent());
				}
			}

			result.add(map);
		}

		return Collections.unmodifiableList(result);
	}

	static
	public Map<String, String> match(List<Map<String, String>> rows, Map<String, FieldValue> values){

		rows:
		for(Map<String, String> row : rows){

			// A table row contains a certain number of input columns, plus an output column
			if(values.size() < (row.size() - 1)){
				continue rows;
			}

			Collection<Map.Entry<String, FieldValue>> entries = values.entrySet();
			for(Map.Entry<String, FieldValue> entry : entries){
				String key = entry.getKey();
				FieldValue value = entry.getValue();

				String rowValue = row.get(key);
				if(rowValue == null){
					continue rows;
				}

				boolean equals = value.equalsString(rowValue);
				if(!equals){
					continue rows;
				}
			}

			return row;
		}

		return null;
	}
}