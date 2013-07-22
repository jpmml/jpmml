/*
 * Copyright (c) 2013 University of Tartu
 */
package org.jpmml.evaluator;

import java.util.*;

import org.dmg.pmml.*;

import org.junit.*;

import com.google.common.collect.*;

import static org.junit.Assert.*;

public class EvaluatorUtilTest {

	@Test
	public void decode(){
		Computable<String> value = new Computable<String>(){

			@Override
			public String getResult(){
				return "value";
			}
		};

		assertEquals("value", EvaluatorUtil.decode(value));
	}

	@Test
	public void groupRows(){
		List<Map<FieldName, Object>> table = Lists.newArrayList();
		table.add(createRow("1", "Cracker"));
		table.add(createRow("2", "Cracker"));
		table.add(createRow("1", "Coke"));
		table.add(createRow("3", "Cracker"));
		table.add(createRow("3", "Water"));
		table.add(createRow("3", "Coke"));
		table.add(createRow("2", "Water"));

		table = EvaluatorUtil.groupRows(new FieldName("transaction"), table);

		checkGroupedRow(table.get(0), "1", Arrays.asList("Cracker", "Coke"));
		checkGroupedRow(table.get(1), "2", Arrays.asList("Cracker", "Water"));
		checkGroupedRow(table.get(2), "3", Arrays.asList("Cracker", "Water", "Coke"));
	}

	static
	private Map<FieldName, Object> createRow(String transaction, String item){
		Map<FieldName, Object> result = Maps.newLinkedHashMap();
		result.put(new FieldName("transaction"), transaction);
		result.put(new FieldName("item"), item);

		return result;
	}

	static
	private void checkGroupedRow(Map<FieldName, Object> row, String transaction, List<String> items){
		assertEquals(2, row.size());

		assertEquals(transaction, row.get(new FieldName("transaction")));
		assertEquals(items, row.get(new FieldName("item")));
	}
}