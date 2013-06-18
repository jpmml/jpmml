package org.jpmml.itest;

import java.io.StringReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.dmg.pmml.FieldName;
import org.dmg.pmml.OpType;
import org.dmg.pmml.PMML;
import org.jpmml.evaluator.EvaluationException;
import org.jpmml.evaluator.Evaluator;
import org.jpmml.evaluator.MiningModelEvaluator;
import org.jpmml.evaluator.ModelEvaluatorFactory;
import org.jpmml.evaluator.RegressionModelEvaluator;
import org.jpmml.manager.IPMMLResult;
import org.jpmml.manager.ModelManager;
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
		CompiledModel compiledModel = createCompiledModel(pmmlDoc, codeTemplate, null);

		executeAndCompareOutput(0, compiledModel, evaluator, manual, variableValues);
	}

	public void testModelEvaluation(PMML pmmlDoc,
			String codeTemplate,
			ManualModelImplementation manual,
			Map<String, List<?>> variables,
			final int iterations,
			TranslationContext context) throws Exception {

		// creating evaluator
		PMMLManager pmmlManager = new PMMLManager(pmmlDoc);
		Evaluator evaluator = (Evaluator)pmmlManager.getModelManager(null, ModelEvaluatorFactory.getInstance());

		// translate and compile
		CompiledModel compiledModel = createCompiledModel(pmmlDoc, codeTemplate, context);


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
 				try {
				executeAndCompareOutput(i, compiledModel, evaluator, manual, nameToValue);
 				}
 				catch (EvaluationException ee) {
 					if (ee.getMessage().startsWith("Missing parameter ")
 							&& (evaluator instanceof RegressionModelEvaluator
 							|| evaluator instanceof MiningModelEvaluator)) {
 						// This is fine, the way we generate our test
 						// doesn't fit with the input for the regression model.
 						// So in order to keep this way of thinking, we just
 						// remove the exception for this particular case.
 					}
 					else {
 						// Otherwise, this exception is not normal, and should be sent
 						// to the user.
 						throw ee;
 					}
 				}
			}
		}
	}

	public void testModelEvaluation(PMML pmmlDoc,
			String codeTemplate,
			ManualModelImplementation manual,
			Map<String, List<?>> variables,
			final int iterations) throws Exception {
		testModelEvaluation(pmmlDoc, codeTemplate, manual, variables, iterations, null);
	}

	private CompiledModel createCompiledModel(PMML pmmlDoc, String codeTemplate, TranslationContext context) throws Exception {
		//InputStream is = getClass().getResourceAsStream("/codetemplate.vm");

		String className = "TestModel" + System.currentTimeMillis();
		if (context == null) {
			context = new TranslationContext() {
				// override missing value method, since in our template numeric variables represented with Double class
				public String getMissingValue(OpType variableType) {
					if (variableType == OpType.CONTINUOUS)
						return "null";

					return super.getMissingValue(variableType);
				}

				public String getModelResultTrackingVariable() {
					return "resultExplanation";
				}
			};
		}

		String javaSource = PmmlToJavaTranslator.generateJavaCode(pmmlDoc, className,
				new StringReader(codeTemplate),
				context);

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

		compareValues(iteration, nameToValue, value1, value2, pmmlModel.getResultExplanation(), manual.getResultExplanation(),
				false);

		// if we get here then value1==value2
		// now evaluate value3 and compare against value1
		Object value3 = null;

		try {
			value3 = evaluateModel(evaluator, nameToValue)
					.getValue(((PMMLManager) evaluator).getOutputField((ModelManager<?>) evaluator).getName());
		} catch (NoSuchElementException e) {
			value3 = null;
		} catch (Exception e) {
			value3 = null;
		}

		// Fake for the result explanation, because evaluator.getResultExplanation doesn't exist.
		compareValues(iteration, nameToValue, value2, value3, null, null, true);
	}

	protected IPMMLResult evaluateModel(Evaluator evaluator, Map<String, Object> nameToValue) {
		Map<FieldName, Object> fieldToValue = new HashMap<FieldName, Object>();
		for (Map.Entry<String, Object> entry : nameToValue.entrySet()) {
			fieldToValue.put(new FieldName(entry.getKey()), entry.getValue());
		}
		return evaluator.evaluate(fieldToValue);
	}

	private void compareValues(int iteration, Map<String, Object> nameToValue,
			Object value1,
			Object value2, String explanation1, String explanation2, boolean secondTest
			) {
		if ((value1 == null && value2 != null)
				|| (value1 != null && value2 == null)
				|| (explanation1 == null && explanation2 != null)
				|| (explanation1 != null && explanation2 == null)
				|| (value1 != null && value2 != null && !value1.equals(value2))
				|| (explanation1 != null && explanation2 != null && !explanation1.equals(explanation2))) {
			if (!(value1 != null && value2 != null && value1 instanceof Double
					&& (((((Double) value1) + 1E-6) > ((Double) value2))
					&& (((Double) value1 - 1E-6) < ((Double) value2))))) {
				logger.info((secondTest ? "Second " : "First ") + "test failed. Value1 = " + value1 + "; value2 = " + value2 + "; explanation1 = "
						+ explanation1 + "; explanation2 = " + explanation2);
				for (Map.Entry<String, Object> e : nameToValue.entrySet()) {
					logger.info(e.getKey() + " = " + e.getValue());
				}
			}
		}

		if (value1 != null) {
			if (value1 instanceof Double && value2 != null) {
				assert (((Double) value1 + 1E-6) > ((Double) value2)) && (((Double) value1 - 1E-6) < ((Double) value2));
			}
			else {
				assert value1.equals(value2);
			}
		}
		else if (value2 != null) {
			assert value2.equals(value1);
		}
		else {
			assert value1 == value2;
		}

		if (explanation1 != null) {
			assert explanation1.equals(explanation2);
		}
		else if (explanation2 != null) {
			assert explanation2.equals(explanation1);
		}
		else {
			assert explanation1 == explanation2;
		}
		//logger.info(iteration+") value1: "+value1+"; value2: "+value2);
	}

	static public interface ManualModelImplementation {
		public Object execute(Map<String, Object> nameToValue);

		public String getResultExplanation();
	}

	static public interface CompiledModel {
		public Object execute(Map<String, Object> nameToValue);

		public String getResultExplanation();
	}

}
