/*
 * Copyright (c) 2013 University of Tartu
 */
package org.jpmml.evaluator;

import java.util.*;

import org.jpmml.manager.*;

import org.dmg.pmml.*;

public class BatchUtil {

	private BatchUtil(){
	}

	/**
	 * @return <code>true</code> If all evaluations succeeded, <code>false</code> otherwise.
	 */
	static
	public boolean evaluate(Batch batch) throws Exception {
		PMML pmml = IOUtil.unmarshal(batch.getModel());

		PMMLManager pmmlManager = new PMMLManager(pmml);

		ModelManager<?> modelManager = pmmlManager.getModelManager(null, ModelEvaluatorFactory.getInstance());

		if(modelManager instanceof EntityRegistry){
			EntityRegistry<?> entityRegistry = (EntityRegistry<?>)modelManager;

			// Just for kicks
			entityRegistry.getEntities();
		}

		List<Map<FieldName, String>> input = CsvUtil.load(batch.getInput());
		List<Map<FieldName, String>> output = CsvUtil.load(batch.getOutput());

		if(input.size() != output.size()){
			throw new RuntimeException();
		}

		Evaluator evaluator = (Evaluator)modelManager;

		List<FieldName> activeFields = evaluator.getActiveFields();
		List<FieldName> predictedFields = evaluator.getPredictedFields();
		List<FieldName> outputFields = evaluator.getOutputFields();

		boolean success = true;

		for(int i = 0; i < input.size(); i++){
			Map<FieldName, String> inputRow = input.get(i);
			Map<FieldName, String> outputRow = output.get(i);

			Map<FieldName, Object> parameters = new LinkedHashMap<FieldName, Object>();

			for(FieldName activeField : activeFields){
				String inputCell = inputRow.get(activeField);

				parameters.put(activeField, evaluator.prepare(activeField, inputCell));
			}

			Map<FieldName, ?> result = evaluator.evaluate(parameters);

			for(FieldName predictedField : predictedFields){
				String outputCell = outputRow.get(predictedField);

				Object predictedValue = EvaluatorUtil.decode(result.get(predictedField));

				success &= acceptable(outputCell, predictedValue);
			}

			for(FieldName outputField : outputFields){
				String outputCell = outputRow.get(outputField);

				// XXX
				if(outputCell == null){
					continue;
				}

				Object computedValue = result.get(outputField);

				success &= acceptable(outputCell, computedValue);
			}
		}

		return success;
	}

	static
	private boolean acceptable(String expected, Object actual){
		return VerificationUtil.acceptable(ParameterUtil.parse(ParameterUtil.getDataType(actual), expected), actual, BatchUtil.precision, BatchUtil.zeroThreshold);
	}

	// One part per million parts
	private static final double precision = 1d / (1000 * 1000);

	private static final double zeroThreshold = precision;
}