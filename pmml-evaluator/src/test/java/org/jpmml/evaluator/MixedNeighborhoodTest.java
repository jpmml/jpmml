/*
 * Copyright, KNIME.com AG, Zurich, Switzerland
 */
package org.jpmml.evaluator;

import java.util.*;

import org.dmg.pmml.*;

import org.junit.*;

import com.google.common.collect.*;

import static org.junit.Assert.*;

public class MixedNeighborhoodTest extends NearestNeighborModelEvaluatorTest {

	@Test
	public void evaluate() throws Exception {
		NearestNeighborModelEvaluator evaluator = createEvaluator();

		NearestNeighborModel nearestNeighborModel = evaluator.getModel();

		// XXX
		nearestNeighborModel.setNumberOfNeighbors(1);

		FieldName petalLength = new FieldName("petal length");
		FieldName petalWidth = new FieldName("petal width");
		FieldName sepalLength = new FieldName("sepal length");
		FieldName sepalWidth = new FieldName("sepal width");

		Map<FieldName, Object> arguments = Maps.newLinkedHashMap();
		arguments.put(petalLength, 4.7d);
		arguments.put(petalWidth, 1.4d);
		arguments.put(sepalLength, 7d);
		arguments.put(sepalWidth, 3.2d);

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