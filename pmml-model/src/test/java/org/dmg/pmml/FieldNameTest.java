/*
 * Copyright (c) 2013 University of Tartu
 */
package org.dmg.pmml;

import org.junit.*;

import static org.junit.Assert.*;

public class FieldNameTest {

	@Test
	public void create(){
		FieldName first = FieldName.create("x");
		FieldName second = FieldName.create("x");

		assertSame(first, second);
	}
}