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

		FieldName maritalStatus = new FieldName("marital status");
		FieldName dependents = new FieldName("dependents");

		Map<FieldName, Object> arguments = Maps.newLinkedHashMap();
		arguments.put(maritalStatus, "d");
		arguments.put(dependents, 0);

		Map<FieldName, ?> result = evaluator.evaluate(arguments);

		InstanceClassificationMap target = (InstanceClassificationMap)result.get(evaluator.getTargetField());

		try {
			target.getResult();

			fail();
		} catch(MissingResultException mre){
		}

		assertEquals(Arrays.asList("3", "1", "4"), (target.getEntityIdRanking()).subList(0, 3));

		assertEquals("3", result.get(new FieldName("neighbor1")));
		assertEquals("1", result.get(new FieldName("neighbor2")));
		assertEquals("4", result.get(new FieldName("neighbor3")));
	}
}