/*
 * Copyright, KNIME.com AG, Zurich, Switzerland
 */
package org.jpmml.evaluator;

import java.util.*;

import org.dmg.pmml.*;

import org.junit.*;

import com.google.common.collect.*;

import static org.junit.Assert.*;

public class ClusteringNeighborhoodTest extends NearestNeighborModelEvaluatorTest {

	@Test
	public void evaluate() throws Exception {
		NearestNeighborModelEvaluator evaluator = createEvaluator();

		NearestNeighborModel nearestNeighborModel = evaluator.getModel();

		FieldName maritialStatus = new FieldName("maritial status");
		FieldName age = new FieldName("age");
		FieldName dependents = new FieldName("dependents");

		Map<FieldName, Object> arguments = Maps.newLinkedHashMap();
		arguments.put(maritialStatus, "m");
		arguments.put(age, 40d);
		arguments.put(dependents, 2);

		Map<FieldName, ?> result = evaluator.evaluate(arguments);

		InstanceClassificationMap target = (InstanceClassificationMap)result.get(evaluator.getTargetField());

		try {
			target.getResult();

			fail();
		} catch(MissingResultException mre){
		}

		assertEquals(Arrays.asList("4", "3", "2"), (target.getEntityIdRanking()).subList(0, 3));

		assertEquals("4", result.get(new FieldName("neighbor1")));
		assertEquals("3", result.get(new FieldName("neighbor2")));
		assertEquals("2", result.get(new FieldName("neighbor3")));
	}
}