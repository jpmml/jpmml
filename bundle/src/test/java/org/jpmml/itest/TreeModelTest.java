package org.jpmml.itest;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dmg.pmml.PMML;
import org.jpmml.manager.IOUtil;
import org.testng.annotations.Test;

@Test
public class TreeModelTest extends BaseModelTest {
	/*
	@Test
	public void testTroubledScenario() throws Exception {
		PMML pmmlDoc = IOUtil.unmarshal(getClass().getResourceAsStream("/golf_tree.xml"));
		Map<String, Object> variableToValues = new HashMap<String, Object>();
		
		variableToValues.put("humidity", 26.0);
		variableToValues.put("outlook", "sunny");
		
		runSingleModelEvaluation(pmmlDoc,
			GOLF_MODEL_TEMPLATE, 
			new GolfModel(),
			variableToValues);		
	}
	*/
	@Test
	public void testGolfModel() throws Exception {
		
		PMML pmmlDoc = IOUtil.unmarshal(getClass().getResourceAsStream("/golf_tree.xml"));
		Map<String, List<?>> variableToValues = new HashMap<String, List<?>>();
		variableToValues.put("temperature", null);
		variableToValues.put("humidity", null);		
		variableToValues.put("windy", Arrays.asList("true", "false"));
		variableToValues.put("outlook", Arrays.asList("sunny", "outcast", "rain"));
		
		testModelEvaluation(pmmlDoc,
			GOLF_MODEL_TEMPLATE, 
			new GolfModel(),
			variableToValues, 
			20);
	}

	@Test
	public void testGolfModelLastPrediction() throws Exception {
		
		PMML pmmlDoc = IOUtil.unmarshal(getClass().getResourceAsStream("/golf_tree_last_prediction.xml"));
		Map<String, List<?>> variableToValues = new HashMap<String, List<?>>();
		variableToValues.put("temperature", null);
		variableToValues.put("humidity", null);		
		variableToValues.put("windy", Arrays.asList("true", "false"));
		variableToValues.put("outlook", Arrays.asList("sunny", "outcast", "rain"));
		
		testModelEvaluation(pmmlDoc,
			GOLF_MODEL_TEMPLATE, 
			new GolfModel_LastPrediction(),
			variableToValues, 
			20);
	}
	
	protected double getMissingVarProbability() {
		return 0.01;
	}

	
	static public class GolfModel implements ManualModelImplementation {

		public Object execute(Map<String, Object> nameToValue) {
			String whatIdo = "will play"; 
			
			Double tempreture = (Double)nameToValue.get("temperature"); 
			Double humidity = (Double)nameToValue.get("humidity");  
			String windy = (String)nameToValue.get("windy"); 
			String outlook = (String)nameToValue.get("outlook");
			
			if (outlook!=null && outlook.equals("sunny")) {
				whatIdo = "will play";
				if (tempreture!=null && tempreture>50 && tempreture<90) {
					whatIdo = "will play";
					if (humidity!=null && humidity<80) {
						whatIdo = "will play";
					}
					else if (humidity!=null && humidity>80) {
						whatIdo = "no play";
					}
					else {
						whatIdo = null;
					}
				}
				else if (tempreture!=null && (tempreture>=90 || tempreture<=50)) {
					whatIdo = "no play";
				}
				else {
					whatIdo = null;
				}
			}
			else if (outlook!=null && (outlook.equals("overcast") || outlook.equals("rain"))) {
				whatIdo = "may play";
				if (tempreture!=null && tempreture>60 && tempreture<100 
					&& outlook!=null && outlook.equals("overcast")  
					&& humidity!=null && humidity<70 
					&& windy!=null && windy.equals("false")) {
					whatIdo = "may play";
				}
				else if (outlook!=null && outlook.equals("rain") 
						&& humidity!=null && humidity<70) {
					whatIdo = "no play";
				}
				else {
					whatIdo = null;
				}
			}
			else {
				whatIdo = null;
			}

				
			return whatIdo;
		}
		
	}

	static public class GolfModel_LastPrediction implements ManualModelImplementation {

		public Object execute(Map<String, Object> nameToValue) {
			String whatIdo = "will play"; 
			
			Double tempreture = (Double)nameToValue.get("temperature"); 
			Double humidity = (Double)nameToValue.get("humidity");  
			String windy = (String)nameToValue.get("windy"); 
			String outlook = (String)nameToValue.get("outlook");
			
			if (outlook!=null && outlook.equals("sunny")) {
				whatIdo = "will play";
				if (tempreture!=null && tempreture>50 && tempreture<90) {
					whatIdo = "will play";
					if (humidity!=null && humidity<80) {
						whatIdo = "will play";
					}
					else if (humidity!=null && humidity>80) {
						whatIdo = "no play";
					}
				}
				else if (tempreture!=null && (tempreture>=90 || tempreture<=50)) {
					whatIdo = "no play";
				}
			}
			else if (outlook!=null && (outlook.equals("overcast") || outlook.equals("rain"))) {
				whatIdo = "may play";
				if (tempreture!=null && tempreture>60 && tempreture<100 
					&& outlook!=null && outlook.equals("overcast")  
					&& humidity!=null && humidity<70 
					&& windy!=null && windy.equals("false")) {
					whatIdo = "may play";
				}
				else if (outlook!=null && outlook.equals("rain") 
						&& humidity!=null && humidity<70) {
					whatIdo = "no play";
				}
			}
				
			return whatIdo;
		}
		
	}

	static private final String GOLF_MODEL_TEMPLATE = "" +
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
			"		String whatIdo = null;\n" + 
			"		Double temperature = (Double)nameToValue.get(\"temperature\");\n" + 
			"		Double humidity = (Double)nameToValue.get(\"humidity\");\n" + 
			"		String windy = (String)nameToValue.get(\"windy\");\n" + 
			"		String outlook = (String)nameToValue.get(\"outlook\");\n" + 
			"		\n" + 
			"		${modelCode}\n" + 
			"		\n" + 
			"		return whatIdo;\n" + 
			"	}\n" + 
			"}\n"; 
}
