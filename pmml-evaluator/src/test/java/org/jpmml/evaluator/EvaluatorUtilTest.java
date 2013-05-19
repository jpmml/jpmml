/*
 * Copyright (c) 2013 University of Tartu
 */
package org.jpmml.evaluator;

import org.junit.*;

import static org.junit.Assert.*;

public class EvaluatorUtilTest {

	@Test
	public void decode(){
		Computable<String> value = new Computable<String>(){

			public String getResult(){
				return "value";
			}
		};

		assertEquals("value", EvaluatorUtil.decode(value));
	}
}