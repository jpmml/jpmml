/*
 * Copyright (c) 2013 Villu Ruusmann
 */
package org.jpmml.evaluator;

import java.util.*;

import org.dmg.pmml.*;

import org.junit.*;

import static org.junit.Assert.*;

public class AssociationAlgorithmTest extends AssociationModelEvaluatorTest {

	@Test
	public void evaluate() throws Exception {
		evaluate(Arrays.asList("Cracker", "Coke"), Arrays.asList("1", "3"), Arrays.asList("1"), Arrays.asList("3"));
		evaluate(Arrays.asList("Cracker", "Water"), Arrays.asList("1", "2", "3", "4", "5"), Arrays.asList("3", "4", "5"), Arrays.asList("1", "2"));
		evaluate(Arrays.asList("Water", "Coke"), Arrays.asList("2", "5"), Arrays.asList("2", "5"), Arrays.<String>asList());
		evaluate(Arrays.asList("Cracker", "Water", "Coke"), Arrays.asList("1", "2", "3", "4", "5"), Arrays.asList("4", "5"), Arrays.asList("1", "2", "3"));
		evaluate(Arrays.asList("Cracker", "Water", "Banana", "Apple"), Arrays.asList("1", "2", "3", "4", "5"), Arrays.asList("3", "4", "5"), Arrays.asList("1", "2"));
	}

	private void evaluate(Collection<String> items, List<String> recommendations, List<String> exclusiveRecommendations, List<String> ruleAssociations) throws Exception {
		AssociationModelEvaluator evaluator = createEvaluator();

		Map<FieldName, ?> arguments = createArguments("item", items);

		Map<FieldName, ?> result = evaluator.evaluate(arguments);

		assertEquals(recommendations, result.get(new FieldName("Recommendation")));
		assertEquals(exclusiveRecommendations, result.get(new FieldName("Exclusive_Recommendation")));
		assertEquals(ruleAssociations, result.get(new FieldName("Rule_Association")));
	}
}