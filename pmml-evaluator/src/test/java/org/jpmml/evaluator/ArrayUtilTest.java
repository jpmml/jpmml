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
	public void tokenizeIntArray(){
		assertEquals(Arrays.asList("1", "2", "3"), tokenizeIntArray("1 2 3"));
	}

	@Test
	public void tokenizeStringArray(){
		assertEquals(Arrays.asList("a", "b", "c"), tokenizeStringArray("a b c"));
		assertEquals(Arrays.asList("a", "b", "c"), tokenizeStringArray("\"a\" \"b\" \"c\""));

		assertEquals(Arrays.asList("a b c"), tokenizeStringArray("\"a b c\""));

		assertEquals(Arrays.asList("\"a b c"), tokenizeStringArray("\"a b c"));
		assertEquals(Arrays.asList("\\a", "\\b\\", "c\\"), tokenizeStringArray("\\a \\b\\ c\\"));

		assertEquals(Arrays.asList("a \"b\" c"), tokenizeStringArray("\"a \\\"b\\\" c\""));
		assertEquals(Arrays.asList("\"a b c\""), tokenizeStringArray("\"\\\"a b c\\\"\""));
	}

	static
	private List<String> tokenizeIntArray(String content){
		return ArrayUtil.tokenize(new ArrayType(content, ArrayType.Type.INT));
	}

	static
	private List<String> tokenizeStringArray(String content){
		return ArrayUtil.tokenize(new ArrayType(content, ArrayType.Type.STRING));
	}
}