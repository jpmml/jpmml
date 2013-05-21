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
		List<Map<FieldName, String>> input = CsvUtil.load(batch.getInput());
		List<Map<FieldName, String>> output = CsvUtil.load(batch.getOutput());

		if(input.size() != output.size()){
			throw new RuntimeException();
		}

		PMML pmml = PmmlUtil.load(batch.getModel());

		PMMLManager pmmlManager = new PMMLManager(pmml);

		Evaluator evaluator = (Evaluator)pmmlManager.getModelManager(null, ModelEvaluatorFactory.getInstance());

		List<FieldName> activeFields = evaluator.getActiveFields();
		List<FieldName> predictedFields = evaluator.getPredictedFields();

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

				DataType dataType = ParameterUtil.getDataType(predictedValue);

				// The output data type could be more "relaxed" than the input data type
				switch(dataType){
					case INTEGER:
					case FLOAT:
					case DOUBLE:
						dataType = DataType.DOUBLE;
						break;
					default:
						break;
				}

				success &= VerificationUtil.acceptable(ParameterUtil.parse(dataType, outputCell), predictedValue, BatchUtil.precision);
			}
		}

		return success;
	}

	// One part per million parts
	private static final double precision = 1d / (1000 * 1000);
}