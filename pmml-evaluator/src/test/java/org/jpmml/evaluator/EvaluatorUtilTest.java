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

		assertEquals(Arrays.asList("Cracker", "Coke"), getBasket(table.get(0)));
		assertEquals(Arrays.asList("Cracker", "Water"), getBasket(table.get(1)));
		assertEquals(Arrays.asList("Cracker", "Water", "Coke"), getBasket(table.get(2)));
	}

	static
	private Map<FieldName, Object> createRow(String transaction, String item){
		Map<FieldName, Object> result = Maps.newLinkedHashMap();
		result.put(new FieldName("transaction"), transaction);
		result.put(new FieldName("item"), item);

		return result;
	}

	static
	private Object getBasket(Map<FieldName, Object> map){
		Collection<Object> values = map.values();

		assertEquals(1, values.size());

		return (values.iterator()).next();
	}
}