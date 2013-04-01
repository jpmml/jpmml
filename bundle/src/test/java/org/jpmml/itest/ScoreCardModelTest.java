package org.jpmml.itest;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dmg.pmml.PMML;
import org.jpmml.manager.IOUtil;
import org.testng.annotations.Test;

@Test
public class ScoreCardModelTest extends BaseModelTest {
	@Test
	public void testSampleScoreCardModel() throws Exception {
		PMML pmmlDoc = IOUtil.unmarshal(getClass().getResourceAsStream("/scorecard.xml"));
		Map<String, List<?>> variableToValues = new HashMap<String, List<?>>();
		//variableToValues.put("department", "engineering");
		variableToValues.put("age", Arrays.asList(22, 35, 45));
		variableToValues.put("income", Arrays.asList(1600, 1000, 500));
		variableToValues.put("department", Arrays.asList("engineering", "marketing", "business"));

		testModelEvaluation(pmmlDoc,
			SAMPLE_SCORECARD_MODEL_TEMPLATE, 
			new SampleScoreCardModel(),
			variableToValues, 
			20);
	}

	protected double getMissingVarProbability() {
		return 0.01;
	}
	
	static public class SampleScoreCardModel implements ManualModelImplementation {

		public Object execute(Map<String, Object> nameToValue) {
			double score = 0.0; 

			String department = (String) nameToValue.get("department");
			Integer age = (Integer) nameToValue.get("age");
			Integer income = (Integer) nameToValue.get("income");

			// Department score
			if (department == null) {
			}
			else if (department.equals("marketing")) {
				score += 19;
			}
			else if (department.equals("engineering")) {
				score += 3;
			}
			else if (department.equals("business")) {
				score += 6;
			}
			else {
			}
						
			// Age score
			if (age == null) {
				score += -1;
			}
			else if (is_in_range(age, 0, 18)) {
				score += -3;
			}
			else if (is_in_range(age, 19, 29)) {
				// Verbose but explicit...
				score += 0;
			}
			else if (is_in_range(age, 30, 39)) {
				score += 12;
			}
			else if (age >= 40) {
				score += 18;
			}
			else {
				score += -1;
			}
			 
			// Income score
			if (income == null) {
				score += 5;
			}
			else if (income <= 1000) {
				score += 26;
			}
			else if (income > 1000 && income <= 1500) {
				score += 5;
			}
			else if (income > 1500) {
				score += -3;
			}

			return score;
	}

		private Boolean is_in_range(Integer value, Integer lower_bound, Integer upper_bound) {
			return lower_bound <= value && value <= upper_bound;			
		}
	}

	static private final String SAMPLE_SCORECARD_MODEL_TEMPLATE = "" +
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
			"		Double overallScore = 0.0;\n" + 
			"		Integer age = (Integer)nameToValue.get(\"age\");\n" + 
			"		Integer income = (Integer)nameToValue.get(\"income\");\n" + 
			"		String department = (String)nameToValue.get(\"department\");\n" + 
			"		\n" + 
			"${modelCode}\n" + 
			"		return overallScore;\n" + 
			"	}\n" + 
			"}\n"; 
}
