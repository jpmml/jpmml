package org.jpmml.itest;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.dmg.pmml.PMML;
import org.jpmml.manager.IOUtil;
import org.testng.annotations.Test;

@Test
public class RegressionModelTest extends BaseModelTest {
	@Test
	public void testSampleRegressionModel() throws Exception {
		PMML pmmlDoc = IOUtil.unmarshal(getClass().getResourceAsStream("/regression.xml"));
		Map<String, List<?>> variableToValues = new HashMap<String, List<?>>();
		//variableToValues.put("department", "engineering");
		variableToValues.put("age", Arrays.asList(22, 35, 45, 63, 33, 42, 51));
		variableToValues.put("salary", Arrays.asList(1600, 1000, 500));
		variableToValues.put("car_location", Arrays.asList("street", "carpark"));

		testModelEvaluation(pmmlDoc,
			SAMPLE_REGRESSION_MODEL_TEMPLATE,
			new SampleRegressionModel(),
			variableToValues, 
			20);
	}

	protected double getMissingVarProbability() {
		return 0.01;
	}
	
	static public class SampleRegressionModel implements ManualModelImplementation {

		public Object execute(Map<String, Object> nameToValue) {
			double score = 0.0; 

			String car_location = (String) nameToValue.get("car_location");
			Integer age = (Integer) nameToValue.get("age");
			Integer salary = (Integer) nameToValue.get("salary");

			if (age == null || salary == null) {
				return null;
			}
			else {
				score = 132.37 + 7.1  * age + 0.01 * salary;
			}
			if (car_location != null) {
				score +=  41.1 * (car_location.equals("carpark") ? 1 : 0)
						+ 325.03 * (car_location.equals("street") ? 1 : 0);
			}
			
			return score;
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
			"		Double number_of_claims = 0.0;\n" + 
			"		Integer age = (Integer)nameToValue.get(\"age\");\n" + 
			"		Integer salary = (Integer)nameToValue.get(\"salary\");\n" + 
			"		String car_location = (String)nameToValue.get(\"car_location\");\n" + 
			"		\n" + 
			"${modelCode}\n" + 
			"		return number_of_claims;\n" +
			"	} catch (Exception eee) { return null; }\n" +
			"	}\n" +
			"	String resultExplanation = null;\n" +
			" 	public String getResultExplanation() {\n" +
			" 		return resultExplanation;\n" +
			"	}\n" +
			"}\n"; 
}
