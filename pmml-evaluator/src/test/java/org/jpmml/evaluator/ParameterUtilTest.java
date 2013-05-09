/*
 * Copyright (c) 2013 University of Tartu
 */
package org.jpmml.evaluator;

import org.dmg.pmml.*;

import org.junit.*;

import static org.junit.Assert.*;

public class ParameterUtilTest {

	@Test
	public void getDataType(){
		assertEquals(DataType.FLOAT, ParameterUtil.getDataType("1.0"));
		assertEquals(DataType.FLOAT, ParameterUtil.getDataType("1.0E0"));
		assertEquals(DataType.STRING, ParameterUtil.getDataType("1.0X"));

		assertEquals(DataType.INTEGER, ParameterUtil.getDataType("1"));
		assertEquals(DataType.STRING, ParameterUtil.getDataType("1E0"));
		assertEquals(DataType.STRING, ParameterUtil.getDataType("1X"));
	}
}