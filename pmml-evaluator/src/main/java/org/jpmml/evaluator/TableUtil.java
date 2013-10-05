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
	public Table<Integer, String, String> parse(InlineTable table){
		Table<Integer, String, String> result = TreeBasedTable.create();

		Integer rowKey = 1;

		List<Row> rows = table.getRows();
		for(Row row : rows){
			List<Object> cells = row.getContent();

			for(Object cell : cells){

				if(cell instanceof Element){
					Element element = (Element)cell;

					result.put(rowKey, element.getTagName(), element.getTextContent());
				}
			}

			rowKey += 1;
		}

		return Tables.unmodifiableTable(result);
	}

	static
	public Map<String, String> match(Table<Integer, String, String> table, Map<String, FieldValue> values){
		Set<Integer> rowKeys = table.rowKeySet();

		rows:
		for(Integer rowKey : rowKeys){
			Map<String, String> row = table.row(rowKey);

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