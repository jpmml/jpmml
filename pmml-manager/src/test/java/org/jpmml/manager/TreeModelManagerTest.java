/*
 * Copyright (c) 2009 University of Tartu
 */
package org.jpmml.manager;

import java.math.*;

import org.junit.*;

import static org.junit.Assert.*;

public class TreeModelManagerTest {

	@Test
	public void compareAsNumber(){
		assertTrue(TreeModelManager.compare(new BigDecimal("2"), "10") < 0);
		assertTrue(TreeModelManager.compare(new BigDecimal("1"), "1") == 0);
		assertTrue(TreeModelManager.compare(new BigDecimal("10"), "2") > 0);
	}

	@Test
	public void compareAsString(){
		assertTrue(TreeModelManager.compare("2", "10") > 0);
		assertTrue(TreeModelManager.compare("1", "1") == 0);
		assertTrue(TreeModelManager.compare("10", "2") < 0);
	}

	@Test
	public void binaryAnd(){
		assertEquals(Boolean.TRUE, TreeModelManager.binaryAnd(Boolean.TRUE, Boolean.TRUE));
		assertEquals(Boolean.FALSE, TreeModelManager.binaryAnd(Boolean.TRUE, Boolean.FALSE));
		assertEquals(null, TreeModelManager.binaryAnd(Boolean.TRUE, null));
		assertEquals(Boolean.FALSE, TreeModelManager.binaryAnd(Boolean.FALSE, Boolean.TRUE));
		assertEquals(Boolean.FALSE, TreeModelManager.binaryAnd(Boolean.FALSE, Boolean.FALSE));
		assertEquals(Boolean.FALSE, TreeModelManager.binaryAnd(Boolean.FALSE, null));
		assertEquals(null, TreeModelManager.binaryAnd(null, Boolean.TRUE));
		assertEquals(Boolean.FALSE, TreeModelManager.binaryAnd(null, Boolean.FALSE));
		assertEquals(null, TreeModelManager.binaryAnd(null, null));
	}

	@Test
	public void binaryOr(){
		assertEquals(Boolean.TRUE, TreeModelManager.binaryOr(Boolean.TRUE, Boolean.TRUE));
		assertEquals(Boolean.TRUE, TreeModelManager.binaryOr(Boolean.TRUE, Boolean.FALSE));
		assertEquals(Boolean.TRUE, TreeModelManager.binaryOr(Boolean.TRUE, null));
		assertEquals(Boolean.TRUE, TreeModelManager.binaryOr(Boolean.FALSE, Boolean.TRUE));
		assertEquals(Boolean.FALSE, TreeModelManager.binaryOr(Boolean.FALSE, Boolean.FALSE));
		assertEquals(null, TreeModelManager.binaryOr(Boolean.FALSE, null));
		assertEquals(Boolean.TRUE, TreeModelManager.binaryOr(null, Boolean.TRUE));
		assertEquals(null, TreeModelManager.binaryOr(null, Boolean.FALSE));
		assertEquals(null, TreeModelManager.binaryOr(null, null));
	}

	@Test
	public void binaryXor(){
		assertEquals(Boolean.FALSE, TreeModelManager.binaryXor(Boolean.TRUE, Boolean.TRUE));
		assertEquals(Boolean.TRUE, TreeModelManager.binaryXor(Boolean.TRUE, Boolean.FALSE));
		assertEquals(null, TreeModelManager.binaryXor(Boolean.TRUE, null));
		assertEquals(Boolean.TRUE, TreeModelManager.binaryXor(Boolean.FALSE, Boolean.TRUE));
		assertEquals(Boolean.FALSE, TreeModelManager.binaryXor(Boolean.FALSE, Boolean.FALSE));
		assertEquals(null, TreeModelManager.binaryXor(Boolean.FALSE, null));
		assertEquals(null, TreeModelManager.binaryXor(null, Boolean.TRUE));
		assertEquals(null, TreeModelManager.binaryXor(null, Boolean.FALSE));
		assertEquals(null, TreeModelManager.binaryXor(null, null));
	}
}