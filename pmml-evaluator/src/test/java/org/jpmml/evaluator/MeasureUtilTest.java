/*
 * Copyright, KNIME.com AG, Zurich, Switzerland
 */
package org.jpmml.evaluator;

import java.util.*;

import org.dmg.pmml.*;

import org.junit.*;

import com.google.common.collect.*;

import static org.junit.Assert.*;

public class MeasureUtilTest {

	@Test
	public void evaluateSimilarity(){
		List<ClusteringField> clusteringFields = createClusteringFields("one", "two", "three", "four");

		List<FieldValue> values = createValues(0, 0, 1, 1);
		List<FieldValue> referenceValues = createValues(0, 1, 0, 1);

		ComparisonMeasure comparisonMeasure = new ComparisonMeasure(ComparisonMeasure.Kind.SIMILARITY);

		comparisonMeasure = comparisonMeasure.withMeasure(new SimpleMatching());
		assertEquals(Double.valueOf(2d / 4d), MeasureUtil.evaluareSimilarity(comparisonMeasure, clusteringFields, values, referenceValues));

		comparisonMeasure = comparisonMeasure.withMeasure(new Jaccard());
		assertEquals(Double.valueOf(1d / 3d), MeasureUtil.evaluareSimilarity(comparisonMeasure, clusteringFields, values, referenceValues));

		comparisonMeasure = comparisonMeasure.withMeasure(new Tanimoto());
		assertEquals(Double.valueOf(2d / (1d + 2 * 2d + 1d)), MeasureUtil.evaluareSimilarity(comparisonMeasure, clusteringFields, values, referenceValues));

		comparisonMeasure = comparisonMeasure.withMeasure(new BinarySimilarity(0.5d, 0.5d, 0.5d, 0.5d, 1d, 1d, 1d, 1d));
		assertEquals(Double.valueOf(2d / 4d), MeasureUtil.evaluareSimilarity(comparisonMeasure, clusteringFields, values, referenceValues));
	}

	static
	private List<ClusteringField> createClusteringFields(String... names){
		List<ClusteringField> result = Lists.newArrayList();

		for(String name : names){
			result.add(new ClusteringField(new FieldName(name)));
		}

		return result;
	}

	static
	private List<FieldValue> createValues(Number... numbers){
		List<FieldValue> result = Lists.newArrayList();

		for(Number number : numbers){
			result.add(FieldValueUtil.create(number));
		}

		return result;
	}
}