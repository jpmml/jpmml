/*
 * Copyright (c) 2013 KNIME.com AG, Zurich, Switzerland
 */
package org.jpmml.evaluator;

import java.util.*;

import org.dmg.pmml.*;

import org.junit.*;

import static org.junit.Assert.*;

public class SparseArrayUtilTest {

	@Test
	public void intSparseArray(){
		IntSparseArray sparseArray = new IntSparseArray();
		(sparseArray.getIndices()).addAll(Arrays.asList(2, 5));
		(sparseArray.getEntries()).addAll(Arrays.asList(3, 42));
		sparseArray.setN(7);

		assertEquals(0, SparseArrayUtil.getValue(sparseArray, 1));
		assertEquals(3, SparseArrayUtil.getValue(sparseArray, 2));
		assertEquals(42, SparseArrayUtil.getValue(sparseArray, 5));
		assertEquals(0, SparseArrayUtil.getValue(sparseArray, 7));
	}
}