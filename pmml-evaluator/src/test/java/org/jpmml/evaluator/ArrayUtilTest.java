/*
 * Copyright (c) 2012 University of Tartu
 */
package org.jpmml.evaluator;

import java.util.*;

import org.dmg.pmml.*;

import org.junit.*;

import static org.junit.Assert.*;

public class ArrayUtilTest {

	@Test
	public void parseIntArray(){
		assertEquals(Arrays.asList("1", "2", "3"), parseIntArray("1 2 3"));
	}

	@Test
	public void parseStringArray(){
		assertEquals(Arrays.asList("a", "b", "c"), parseStringArray("a b c"));
		assertEquals(Arrays.asList("a", "b", "c"), parseStringArray("\"a\" \"b\" \"c\""));

		assertEquals(Arrays.asList("a b c"), parseStringArray("\"a b c\""));

		assertEquals(Arrays.asList("\"a b c"), parseStringArray("\"a b c"));
		assertEquals(Arrays.asList("\\a", "\\b\\", "c\\"), parseStringArray("\\a \\b\\ c\\"));

		assertEquals(Arrays.asList("a \"b\" c"), parseStringArray("\"a \\\"b\\\" c\""));
		assertEquals(Arrays.asList("\"a b c\""), parseStringArray("\"\\\"a b c\\\"\""));
	}

	static
	private List<String> parseIntArray(String content){
		return ArrayUtil.parse(new Array(content, Array.Type.INT));
	}

	static
	private List<String> parseStringArray(String content){
		return ArrayUtil.parse(new Array(content, Array.Type.STRING));
	}
}