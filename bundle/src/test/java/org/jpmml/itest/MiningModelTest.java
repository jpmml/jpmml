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
			SAMPLE_CLASSIFICATION_MODEL_TEMPLATE,
			new SampleMiningModel(),
			variableToValues,
			20);
	}

	@Test
	public void testSampleRegressionMiningModel() throws Exception {
		PMML pmmlDoc = IOUtil.unmarshal(getClass().getResourceAsStream("/miningModel2.xml"));
		Map<String, List<?>> variableToValues = new HashMap<String, List<?>>();
		variableToValues.put("petal_length", Arrays.asList(1.0, 1.3, 2.80, 2.90, 3.0, 3.1, 3.2));
		variableToValues.put("petal_width", Arrays.asList(1.1, 1.4, 1.6, 2.85, 3.33, 2.89));
		variableToValues.put("sepal_width", Arrays.asList(1.1, 1.4, 1.6, 2.85, 3.33, 2.89));
		variableToValues.put("continent", Arrays.asList("asia", "africa", "europe",
							"america", "antartica", "oceania"));


		testModelEvaluation(pmmlDoc,
			SAMPLE_REGRESSION_MODEL_TEMPLATE,
			new SampleRegressionMiningModel(),
			variableToValues,
			20);
	}


	@Test
	public void testSampleRegressionMultipleEasyMiningModel() throws Exception {
		PMML pmmlDoc = IOUtil.unmarshal(getClass().getResourceAsStream("/modelChainMiningModel.xml"));
		Map<String, List<?>> variableToValues = new HashMap<String, List<?>>();
		variableToValues.put("petal_length", Arrays.asList(1.0, 1.3, 2.80, 2.90, 3.0, 3.1, 3.2));
		variableToValues.put("petal_width", Arrays.asList(1.1, 1.4, 1.6, 2.85, 3.33, 2.89));
		variableToValues.put("cloudiness", Arrays.asList(1.1, 1.4, 1.6, 2.85, 3.33, 2.89));
		variableToValues.put("temperature", Arrays.asList(1.1, 1.4, 1.6, 2.85, 3.33, 2.89));



		testModelEvaluation(pmmlDoc,
			SAMPLE_REGRESSION_MULTIPLE_MODEL_TEMPLATE,
			new SampleRegressionModelChainMiningModel(),
			variableToValues,
			20);
	}


	protected double getMissingVarProbability() {
		return 0.01;
	}

	static public class SampleRegressionModelChainMiningModel implements ManualModelImplementation {

		public Object execute(Map<String, Object> nameToValue) {
			Double result = 0.0;
			String treeRes = null;

			Double petalLength = (Double) nameToValue.get("petal_length");
			Double petalWidth = (Double) nameToValue.get("petal_width");
			Double temperature = (Double) nameToValue.get("temperature");
			Double cloudiness = (Double) nameToValue.get("cloudiness");

			treeRes = "Iris-setosa";
			if (petalLength != null && petalLength < 2.45) {
				treeRes = "Iris-setosa";
			}
			else if (petalLength != null && petalLength > 2.45) {
				if (petalWidth != null && petalWidth < 1.75) {
					treeRes = "Iris-versicolor";
				}
				else if (petalWidth != null && petalWidth > 1.75) {
					treeRes = "Iris-virginica";
				}
				else
					treeRes = null;
			}
			else
				treeRes = null;

			if (temperature == null || cloudiness == null) {
				return null;
			}

			result = 0.3 + 0.02 * temperature - 0.1 * cloudiness;

			if (treeRes != null && treeRes.equals("Iris-virginica")) {
				result += 0.8;
			}
			if (treeRes != null && treeRes.equals("Iris-setosa")) {
				result += 2.0;
			}

			return result;
		}

		String resultExplanation = null;
		public String getResultExplanation() {
			return resultExplanation;
		}

	}

	static public class SampleRegressionMiningModel implements ManualModelImplementation {
		public Object execute(Map<String, Object> nameToValue) {
			Double result = 0.0;
			Double sumWeight = 0.0;

			Double first = (Double) evaluateFirstSegment(nameToValue);
			Double second = (Double) evaluateSecondSegment(nameToValue);
			Double third = (Double) evaluateThirdSegment(nameToValue);


			if (first != null) {
				sumWeight += 0.25;
				result = 0.25 * first;
			}
			if (second != null) {
				sumWeight += 0.25;
				result += 0.25 * second;
			}
			if (third != null) {
				sumWeight += 0.5;
				result += 0.5 * third;
			}
			if (sumWeight != 0.0)
				return (Double)result / sumWeight;
			else
				return 0.0;
		}

		Double evaluateFirstSegment(Map<String, Object> nameToValue) {
			Double petalLength = (Double) nameToValue.get("petal_length");
			Double sepalWidth = (Double) nameToValue.get("sepal_width");
			Double result = 0.0;



			result = 5.843333;
			if (petalLength != null && petalLength < 4.25) {
				result = 5.179452;
				if (petalLength != null && petalLength < 3.40) {
					result = 5.005660;
				}
				else if (sepalWidth != null && sepalWidth < 3.25) {
					result = 4.735000;
				}
				else if (sepalWidth != null && sepalWidth > 3.25) {
					result = 5.169697;
				}
				else if (petalLength != null &&  petalLength > 3.40) {
					result = 5.640000;
				}
				else {
					result = null;
				}
			}
			else if (petalLength != null &&  petalLength > 4.25) {
				result = 6.472727;

				if (petalLength != null &&  petalLength < 6.05) {
					result = 6.326471;

					if (petalLength != null &&  petalLength < 5.15) {
						result = 6.165116;
						if (sepalWidth != null && sepalWidth < 3.05) {
							result = 6.054545;
						}
						else if (sepalWidth != null && sepalWidth > 3.05) {
							result = 6.530000;
						}
					}
					else if (petalLength != null &&  petalLength > 5.15) {
						result = 6.604000;
					}
				}
				else if (petalLength != null && petalLength > 6.05) {
					result = 7.577778;
				}
				else {
					result = null;
				}
			}
			else {
				result = null;
			}

			return result;
		}

		Double evaluateSecondSegment(Map<String, Object> nameToValue) {
			Double petalWidth = (Double) nameToValue.get("petal_width");
			Double sepalWidth = (Double) nameToValue.get("sepal_width");
			Double result = 0.0;
			result = 5.843333;

			if (petalWidth != null && petalWidth < 1.15) {
				result = 5.073333;

				if (petalWidth != null && petalWidth < 0.35) {
					result = 4.953659;
				}
				else if (sepalWidth != null && sepalWidth < 3.25) {
					result = 4.688235;
				}
				else if (sepalWidth != null && sepalWidth > 3.25) {
					result = 5.141667;
				}
				else if (petalWidth != null && petalWidth > 0.35) {
					result = 5.331579;
				}
				else {
					result = null;
				}
			}
			else if (petalWidth != null && petalWidth > 1.15) {
				result = 6.356667;

				if (petalWidth != null && petalWidth < 1.95) {
					result = 6.160656;

					if (petalWidth != null && petalWidth < 1.35) {
						result = 5.855556;
					}
					else if (petalWidth != null && petalWidth > 1.35) {
						result = 6.288372;

						if (sepalWidth != null && sepalWidth < 2.75) {
							result = 6.000000;
						}
						else if (sepalWidth != null && sepalWidth > 2.75) {
							result = 6.413333;
						}
						else {
							result = null;
						}
					}
					else {
						result = null;
					}
				}
				else if (petalWidth != null && petalWidth > 1.95) {
					result = 6.768966;
				}
				else {
					result = null;
				}
			}
			else {
				result = null;
			}

			return result;
		}

		Double evaluateThirdSegment(Map<String, Object> nameToValue) {
			Double petalLength = (Double) nameToValue.get("petal_length");
			Double result = 0.0;
			result = 5.843333;

			if (petalLength != null && petalLength < 4.25) {
				result = 5.179452;

				if (petalLength != null && petalLength < 3.40) {
					result = 5.005660;
				}
				else if (petalLength != null && petalLength > 3.40) {
					result = 5.640000;
				}
				else {
					result = null;
				}
			}
			else if (petalLength != null && petalLength > 4.25) {
				result = 6.472727;

				if (petalLength != null && petalLength < 6.05) {
					result = 6.326471;

					if (petalLength != null && petalLength < 5.15) {
						result = 6.165116;
					}
					else if (petalLength != null && petalLength > 5.15) {
						result = 6.604000;
					}
					else {
						result = null;
					}
				}
				else if (petalLength != null && petalLength > 6.05) {
					result = 7.577778;
				}
				else {
					result = null;
				}
			}
			else {
				result = null;
			}

			return result;
		}

		String resultExplanation = null;
		public String getResultExplanation() {
			return resultExplanation;
		}
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

	static private final String SAMPLE_REGRESSION_MULTIPLE_MODEL_TEMPLATE = "" +
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
			"		String Class = null;\n" +
			"		Double PollenIndex = null;\n" +
			"		Double petal_length = (Double)nameToValue.get(\"petal_length\");\n" +
			"		Double petal_width = (Double)nameToValue.get(\"petal_width\");\n" +
			"		Double sepal_width = (Double)nameToValue.get(\"sepal_width\");\n" +
			"		Double cloudiness = (Double)nameToValue.get(\"cloudiness\");\n" +
			"		Double temperature = (Double)nameToValue.get(\"temperature\");\n" +
			"		String continent = (String)nameToValue.get(\"continent\");\n" +
			"		\n" +
			"${modelCode}\n" +
			"		return PollenIndex;\n" +
			"	} catch (Exception eee) { return null; }\n" +
			"	}\n" +
			"	String resultExplanation = null;\n" +
			" 	public String getResultExplanation() {\n" +
			" 		return resultExplanation;\n" +
			"	}\n" +
			"}\n";



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
			"		Double sepal_length = 0.0;\n" +
			"		Double petal_length = (Double)nameToValue.get(\"petal_length\");\n" +
			"		Double petal_width = (Double)nameToValue.get(\"petal_width\");\n" +
			"		Double sepal_width = (Double)nameToValue.get(\"sepal_width\");\n" +
			"		String continent = (String)nameToValue.get(\"continent\");\n" +
			"		\n" +
			"${modelCode}\n" +
			"		return sepal_length;\n" +
			"	} catch (Exception eee) { return null; }\n" +
			"	}\n" +
			"	String resultExplanation = null;\n" +
			" 	public String getResultExplanation() {\n" +
			" 		return resultExplanation;\n" +
			"	}\n" +
			"}\n";

	static private final String SAMPLE_CLASSIFICATION_MODEL_TEMPLATE = "" +
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
