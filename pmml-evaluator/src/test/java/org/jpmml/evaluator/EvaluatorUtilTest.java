/*
 * Copyright (c) 2013 University of Tartu
 */
package org.jpmml.evaluator;

import java.util.*;

import org.dmg.pmml.*;

import org.junit.*;

import static org.junit.Assert.*;

public class EvaluatorUtilTest {

	@Test
	public void simplifyKeys(){
		FieldName name = new FieldName("x");

		assertEquals(Collections.singletonMap(name.getValue(), "value"), EvaluatorUtil.simplifyKeys(Collections.singletonMap(name, "value")));
	}

	@Test
	public void simplifyValues(){
		FieldName name = new FieldName("x");

		Computable<String> value = new Computable<String>(){

			public String getResult(){
				return "value";
			}
		};

		assertEquals(Collections.singletonMap(name, "value"), EvaluatorUtil.simplifyValues(Collections.singletonMap(name, value)));
	}
}