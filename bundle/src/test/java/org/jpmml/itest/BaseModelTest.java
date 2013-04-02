package org.jpmml.itest;

import java.io.StringReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dmg.pmml.FieldName;
import org.dmg.pmml.OpType;
import org.dmg.pmml.PMML;
import org.jpmml.evaluator.Evaluator;
import org.jpmml.evaluator.ModelEvaluatorFactory;
import org.jpmml.manager.PMMLManager;
import org.jpmml.translator.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BaseModelTest {
	private static final Logger logger = LoggerFactory.getLogger(BaseModelTest.class);
	
	protected double getMissingVarProbability() {
		return 0.1;
	}

	public void runSingleModelEvaluation(PMML pmmlDoc,
			String codeTemplate, 
			ManualModelImplementation manual,
			Map<String, Object> variableValues 
			) throws Exception {

		// creating evaluator
		PMMLManager pmmlManager = new PMMLManager(pmmlDoc);
		Evaluator evaluator = (Evaluator)pmmlManager.getModelManager(null, ModelEvaluatorFactory.getInstance());

		// translate and compile
		CompiledModel compiledModel = createCompiledModel(pmmlDoc, codeTemplate);
		
		executeAndCompareOutput(0, compiledModel, evaluator, manual, variableValues);
	}
	
	public void testModelEvaluation(PMML pmmlDoc,
			String codeTemplate, 
			ManualModelImplementation manual,
			Map<String, List<?>> variables, 
			final int iterations) throws Exception {

		// creating evaluator
		PMMLManager pmmlManager = new PMMLManager(pmmlDoc);
		Evaluator evaluator = (Evaluator)pmmlManager.getModelManager(null, ModelEvaluatorFactory.getInstance());

		// translate and compile
		CompiledModel compiledModel = createCompiledModel(pmmlDoc, codeTemplate);
		
		
		for (int i=0;i<iterations; i++) {
			// generate random variables
			Map<String, Object> nameToValue = new HashMap<String, Object>();
			for (Map.Entry<String, List<?>> e : variables.entrySet()) {
				// skip variable in 10% cases
 				if (Math.random()>getMissingVarProbability()) {
 					// use one of predefined values
 					if (e.getValue()!=null) {
 						int index = (int)(Math.random()*e.getValue().size());
 						if (index == e.getValue().size()) {
 							index--;
 						}
 						Object value = e.getValue().get(index);
 						nameToValue.put(e.getKey(), value);
 					}
 					else {
 						// otherwise generate random number between 0 and 100
 						nameToValue.put(e.getKey(), 100.0*Math.random());
 					}
 				}

				executeAndCompareOutput(i, compiledModel, evaluator, manual, nameToValue);
			}
		}		
	}
	
	private CompiledModel createCompiledModel(PMML pmmlDoc, String codeTemplate) throws Exception {
		//InputStream is = getClass().getResourceAsStream("/codetemplate.vm");
		
		String className = "TestModel" + System.currentTimeMillis();
				
		String javaSource = PmmlToJavaTranslator.generateJavaCode(pmmlDoc, className, new StringReader(codeTemplate), new TranslationContext() {
			// override missing value method, since in our template numeric variables represented with Double class
			public String getMissingValue(OpType variableType) {
				if (variableType==OpType.CONTINUOUS) return "null";
				return super.getMissingValue(variableType);
			}

		});

		//logger.info("Generated source code:\n"+javaSource);
		

		Class<?> modelClass = PmmlToJavaTranslator.createModelClass(className, "org.jpmml.itest", javaSource);
		
		return (CompiledModel)modelClass.newInstance();
	}

	public void executeAndCompareOutput(int iteration, 
			CompiledModel pmmlModel,
			Evaluator evaluator,
			ManualModelImplementation manual,
			Map<String, Object> nameToValue) {

		Object value1 = pmmlModel.execute(nameToValue);
		Object value2 = manual.execute(nameToValue);
		
		compareValues(iteration, nameToValue, value1, value2);

		// if we get here then value1==value2
		// now evaluate value3 and compare against value1
		Object value3 = evaluateModel(evaluator, nameToValue);
		compareValues(iteration, nameToValue, value1, value3);
	}
	
	protected Object evaluateModel(Evaluator evaluator, Map<String, Object> nameToValue) {
		Map<FieldName, Object> fieldToValue = new HashMap<FieldName, Object>();
		for (Map.Entry<String, Object> entry : nameToValue.entrySet()) {
			fieldToValue.put(new FieldName(entry.getKey()), entry.getValue());
		}
		return evaluator.evaluate(fieldToValue);
	}

	private void compareValues(int iteration, Map<String, Object> nameToValue, 
			Object value1,
			Object value2 
			) {
		if ((value1==null && value2!=null) 
				|| (value1!=null && value2==null) 
				|| (value1!=null && value2!=null && !value1.equals(value2))) {
			logger.info("Test failed. Value1 = " + value1 + "; value2 = " + value2);
			for (Map.Entry<String, Object> e : nameToValue.entrySet()) {
				logger.info(e.getKey() + " = " + e.getValue());
			}
		}
		
		if (value1 != null) {
			assert value1.equals(value2);
		}
		else if (value2 != null) {
			assert value2.equals(value1);
		}
		else {
			assert value1 == value2;
		}
		//logger.info(iteration+") value1: "+value1+"; value2: "+value2);
	}

	static public interface ManualModelImplementation {		
		public Object execute(Map<String, Object> nameToValue);
	}
	
	static public interface CompiledModel {
		public Object execute(Map<String, Object> nameToValue);
	}

}
