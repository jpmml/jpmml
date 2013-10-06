/*
 * Copyright, KNIME.com AG, Zurich, Switzerland
 */
package org.jpmml.evaluator;

import java.util.*;

import org.dmg.pmml.*;

import org.junit.*;

import com.google.common.collect.*;

import static org.junit.Assert.*;

public class TargetValueCountsTest extends NaiveBayesModelEvaluatorTest {

	@Test
	public void evaluate() throws Exception {
		NaiveBayesModelEvaluator evaluator = createEvaluator();

		FieldName gender = new FieldName("gender");
		FieldName numberOfClaims = new FieldName("no of claims");
		FieldName domicile = new FieldName("domicile");
		FieldName ageOfCar = new FieldName("age of car");

		Map<FieldName, Object> arguments = Maps.newLinkedHashMap();
		arguments.put(gender, "male");
		arguments.put(numberOfClaims, "2");
		arguments.put(domicile, null);
		arguments.put(ageOfCar, 1d);

		Map<FieldName, Map<String, Double>> countsMap = evaluator.getCountsMap();

		Map<String, Double> genderCounts = countsMap.get(gender);

		assertEquals(Double.valueOf(8598d), genderCounts.get("100"));
		assertEquals(Double.valueOf(2533d), genderCounts.get("500"));
		assertEquals(Double.valueOf(1522d), genderCounts.get("1000"));
		assertEquals(Double.valueOf(697d), genderCounts.get("5000"));
		assertEquals(Double.valueOf(90d), genderCounts.get("10000"));

		Map<FieldName, ?> result = evaluator.evaluate(arguments);

		ClassificationMap targetValue = (ClassificationMap)result.get(evaluator.getTargetField());

		double l0 = 8723d * 4273d / 8598d * 225d / 8561d * 830d / 8008d;
		double l1 = 2557d * 1321d / 2533d * 10d / 2436d * 182d / 2266d;
		double l2 = 1530d * 780d / 1522d * 9d / 1496d * 51d / 1191d;
		double l3 = 709d * 405d / 697d * 0.001d * 26d / 699d;
		double l4 = 100d * 42d / 90d * 10d / 98d * 6d / 87d;

		double denominator = (l0 + l1 + l2 + l3 + l4);

		assertTrue(VerificationUtil.acceptable(l0 / denominator, targetValue.get("100")));
		assertTrue(VerificationUtil.acceptable(l1 / denominator, targetValue.get("500")));
		assertTrue(VerificationUtil.acceptable(l2 / denominator, targetValue.get("1000")));
		assertTrue(VerificationUtil.acceptable(l3 / denominator, targetValue.get("5000")));
		assertTrue(VerificationUtil.acceptable(l4 / denominator, targetValue.get("10000")));
	}
}