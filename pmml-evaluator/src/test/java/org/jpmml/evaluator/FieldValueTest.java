/*
 * Copyright (c) 2013 Villu Ruusmann
 */
package org.jpmml.evaluator;

import java.util.*;

import org.dmg.pmml.*;

import org.junit.*;

import static org.junit.Assert.*;

public class FieldValueTest {

	@Test
	public void ordinalString(){
		OrdinalValue loud = new OrdinalValue(DataType.STRING, "loud");
		OrdinalValue louder = new OrdinalValue(DataType.STRING, "louder");
		OrdinalValue insane = new OrdinalValue(DataType.STRING, "insane");

		assertTrue(louder.equalsString("louder"));

		assertTrue(louder.equalsValue(FieldValueUtil.create(DataType.STRING, OpType.CATEGORICAL, "louder")));
		assertTrue(louder.equalsValue(FieldValueUtil.create(DataType.STRING, OpType.ORDINAL, "louder")));

		// Implicit (ie. lexicographic) ordering
		louder.setOrdering(null);

		assertTrue(louder.compareToString("loud") > 0);
		assertTrue(louder.compareToString("louder") == 0);
		assertTrue(louder.compareToString("insane") > 0);

		assertTrue(louder.compareToValue(loud) > 0);
		assertTrue(louder.compareToValue(louder) == 0);
		assertTrue(louder.compareToValue(insane) > 0);

		// Explicit ordering
		louder.setOrdering(Arrays.asList("loud", "louder", "insane"));

		assertTrue(louder.compareToString("loud") > 0);
		assertTrue(louder.compareToString("louder") == 0);
		assertTrue(louder.compareToString("insane") < 0);

		assertTrue(louder.compareToValue(loud) > 0);
		assertTrue(louder.compareToValue(louder) == 0);
		assertTrue(louder.compareToValue(insane) < 0);
	}
}