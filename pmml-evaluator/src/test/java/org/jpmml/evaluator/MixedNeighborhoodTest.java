/*
 * Copyright, KNIME.com AG, Zurich, Switzerland
 */
package org.jpmml.evaluator;

import java.util.*;

import org.dmg.pmml.*;

import org.junit.*;

import static org.junit.Assert.*;

public class MixedNeighborhoodTest extends NearestNeighborModelEvaluatorTest {

	@Test
	public void evaluate() throws Exception {
		NearestNeighborModelEvaluator evaluator = createEvaluator();

		NearestNeighborModel nearestNeighborModel = evaluator.getModel();

		// XXX
		nearestNeighborModel.setNumberOfNeighbors(1);

		Map<FieldName, ?> arguments = createArguments("petal length", 4.7d, "petal width", 1.4d, "sepal length", 7d, "sepal width", 3.2d);

		Map<FieldName, ?> result = evaluator.evaluate(arguments);

		InstanceClassificationMap species = (InstanceClassificationMap)result.get(new FieldName("species"));
		assertEquals(20d, species.getResult());
		assertEquals("51", (species.getEntityIdRanking()).get(0));

		InstanceClassificationMap speciesClass = (InstanceClassificationMap)result.get(new FieldName("species_class"));
		assertEquals("Iris-versicolor", speciesClass.getResult());
		assertEquals("51", (speciesClass.getEntityIdRanking()).get(0));

		assertEquals(20d, result.get(new FieldName("output_1")));
		assertEquals("Iris-versicolor", result.get(new FieldName("output_2")));
	}
}