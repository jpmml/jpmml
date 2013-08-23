/*
 * Copyright (c) 2013 University of Tartu
 */
package org.jpmml.evaluator;

import java.util.*;

import org.junit.*;

import com.google.common.collect.*;

import static org.junit.Assert.*;

public class TableUtilTest {

	@Test
	public void matchSingleColumn(){
		Map<String, String> first = createRow(new String[][]{{"value", "1"}, {"output", "first"}});
		Map<String, String> second = createRow(new String[][]{{"value", "2"}, {"output", "second"}});
		Map<String, String> third = createRow(new String[][]{{"value", "3"}, {"output", "third"}});

		List<Map<String, String>> rows = Arrays.asList(first, second, third);

		assertEquals(first, TableUtil.match(rows, createValues(new Object[][]{{"value", "1"}})));
		assertEquals(second, TableUtil.match(rows, createValues(new Object[][]{{"value", 2}})));
		assertEquals(third, TableUtil.match(rows, createValues(new Object[][]{{"value", 3d}})));

		assertEquals(null, TableUtil.match(rows, createValues(new Object[][]{{"value", "false"}})));
	}

	@Test
	public void matchMultipleColumns(){
		Map<String, String> firstTrue = createRow(new String[][]{{"value", "1"}, {"flag", "true"}, {"output", "firstTrue"}});
		Map<String, String> firstFalse = createRow(new String[][]{{"value", "1"}, {"flag", "false"}, {"output", "firstFalse"}});
		Map<String, String> secondTrue = createRow(new String[][]{{"value", "2"}, {"flag", "true"}, {"output", "secondTrue"}});
		Map<String, String> secondFalse = createRow(new String[][]{{"value", "2"}, {"flag", "false"}, {"output", "secondFalse"}});
		Map<String, String> thirdTrue = createRow(new String[][]{{"value", "3"}, {"flag", "true"}, {"output", "thirdTrue"}});
		Map<String, String> thirdFalse = createRow(new String[][]{{"value", "3"}, {"flag", "false"}, {"output", "thirdFalse"}});

		List<Map<String, String>> rows = Arrays.asList(firstTrue, firstFalse, secondTrue, secondFalse, thirdTrue, thirdFalse);

		assertEquals(null, TableUtil.match(rows, createValues(new Object[][]{{"value", "1"}})));

		assertEquals(firstTrue, TableUtil.match(rows, createValues(new Object[][]{{"value", "1"}, {"flag", "true"}})));
		assertEquals(firstFalse, TableUtil.match(rows, createValues(new Object[][]{{"value", "1"}, {"flag", false}})));

		assertEquals(secondTrue, TableUtil.match(rows, createValues(new Object[][]{{"value", 2}, {"flag", "true"}})));
		assertEquals(secondFalse, TableUtil.match(rows, createValues(new Object[][]{{"value", 2}, {"flag", false}})));

		assertEquals(thirdTrue, TableUtil.match(rows, createValues(new Object[][]{{"value", 3d}, {"flag", "true"}})));
		assertEquals(thirdFalse, TableUtil.match(rows, createValues(new Object[][]{{"value", 3d}, {"flag", false}})));
	}

	static
	private Map<String, String> createRow(String[][] strings){
		Map<String, String> result = Maps.newLinkedHashMap();

		for(int i = 0; i < strings.length; i++){
			result.put(strings[i][0], strings[i][1]);
		}

		return result;
	}

	static
	private Map<String, FieldValue> createValues(Object[][] objects){
		Map<String, FieldValue> result = Maps.newLinkedHashMap();

		for(int i = 0; i < objects.length; i++){
			result.put((String)objects[i][0], FieldValueUtil.create(objects[i][1]));
		}

		return result;
	}
}