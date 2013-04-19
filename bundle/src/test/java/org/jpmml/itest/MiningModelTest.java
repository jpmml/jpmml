package org.jpmml.itest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.dmg.pmml.PMML;
import org.jpmml.manager.IOUtil;
import org.testng.annotations.Test;

@Test
public class MiningModelTest extends BaseModelTest {
	@Test
	public void testSampleMiningModel() throws Exception {
		PMML pmmlDoc = IOUtil.unmarshal(getClass().getResourceAsStream("/miningModel.xml"));
		Map<String, List<?>> variableToValues = new HashMap<String, List<?>>();
		variableToValues.put("petal_length", Arrays.asList(1.0, 1.3, 2.80, 2.90, 3.0, 3.1, 3.2));
		variableToValues.put("petal_width", Arrays.asList(1.1, 1.4, 1.6, 2.85, 3.33, 2.89));
		variableToValues.put("continent", Arrays.asList("asia", "africa", "europe",
							"america", "antartica", "oceania"));


		testModelEvaluation(pmmlDoc,
			SAMPLE_REGRESSION_MODEL_TEMPLATE,
			new SampleMiningModel(),
			variableToValues,
			20);
	}

	protected double getMissingVarProbability() {
		return 0.01;
	}

	static public class SampleMiningModel implements ManualModelImplementation {
		public Object execute(Map<String, Object> nameToValue) {

			TreeMap<String, Integer> categoryNameToVote= new TreeMap<String, Integer>();
			ArrayList<String> results = new ArrayList<String>(3);
			results.add((String) evaluateFirstSegment(nameToValue));
			results.add((String) evaluateSecondSegment(nameToValue));
			results.add((String) evaluateThirdSegment(nameToValue));

			for (String firstRes : results) {
				if (categoryNameToVote.containsKey(firstRes)) {
					categoryNameToVote.put(firstRes, categoryNameToVote.get(firstRes) + 1);
				}
				else {
					categoryNameToVote.put(firstRes, 1);
				}
			}

			Integer max = 0;
			String result = null;

			for (Map.Entry<String, Integer> e : categoryNameToVote.entrySet()) {
				if (e.getValue() > max) {
					max = e.getValue();
					result = e.getKey();
				}
			}

			return result;
		}


		private Object evaluateFirstSegment(Map<String, Object> nameToValue) {
			Double petalLength = (Double) nameToValue.get("petal_length");
			Double petalWidth = (Double) nameToValue.get("petal_width");
			String result = null;

			result = "Iris-setosa";

			if (petalLength != null && petalLength < 2.45) {
				result = "Iris-setosa";
			}
			else {
				result = "Iris-versicolor";
				if (petalWidth != null && petalWidth < 1.75) {

				}
				else {
					result = "Iris-virginica";
				}
			}


			return result;
		}

		public Object evaluateSecondSegment(Map<String, Object> nameToValue) {
			Double petalLength = (Double) nameToValue.get("petal_length");
			Double petalWidth = (Double) nameToValue.get("petal_width");
			String continent = (String) nameToValue.get("continent");
			String result = null;

			result = "Iris-setosa";

			if (petalLength != null && petalLength < 2.15) {
				result = "Iris-setosa";
			}
			else {
				result = "Iris-versicolor";

				if (petalWidth != null && petalWidth < 1.93) {
					if (continent != null && continent.equals("africa")) {

					}
					else {
						result = "Iris-virginica";
					}
				}
				else {
					result = "Iris-virginica";
				}
			}

			return result;
		}

		public Object evaluateThirdSegment(Map<String, Object> nameToValue) {
			Double petalLength = (Double) nameToValue.get("petal_length");
			Double petalWidth = (Double) nameToValue.get("petal_width");
			String continent = (String) nameToValue.get("continent");
			String result = null;

			result = "Iris-setosa";

			if (petalWidth != null && petalWidth < 2.85) {

			}
			else {
				result = "Iris-versicolor";

				if (continent != null && continent.equals("asia")) {

				}
				else {
					result = "Iris-virginica";
				}
			}

			return result;
		}


		String resultExplanation = null;
		public String getResultExplanation() {
			return resultExplanation;
		}
	}


	static private final String SAMPLE_REGRESSION_MODEL_TEMPLATE = "" +
			"package org.jpmml.itest;\n" +
			"import java.util.Map;\n" +
			"import org.jpmml.itest.BaseModelTest.CompiledModel;\n" +
			"" +
			"#foreach($import in $imports) \n" +
			"${import}\n" +
			"#end\n" +
			"\n" +
			"#foreach($constant in $constants) \n" +
			"static private final ${constant}\n" +
			"#end" +
			"\n" +
			"public class ${className} implements CompiledModel {\n" +
			"\n" +
			"	public Object execute(Map<String, Object> nameToValue) {\n" +
			"		try {\n" +
			"		String Class = new String();\n" +
			"		Double petal_length = (Double)nameToValue.get(\"petal_length\");\n" +
			"		Double petal_width = (Double)nameToValue.get(\"petal_width\");\n" +
			"		String continent = (String)nameToValue.get(\"continent\");\n" +
			"		\n" +
			"${modelCode}\n" +
			"		return Class;\n" +
			"	} catch (Exception eee) { return null; }\n" +
			"	}\n" +
			"	String resultExplanation = null;\n" +
			" 	public String getResultExplanation() {\n" +
			" 		return resultExplanation;\n" +
			"	}\n" +
			"}\n";

}
