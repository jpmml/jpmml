/*
 * Copyright (c) 2013 University of Tartu
 */
package org.jpmml.evaluator;

import java.math.*;
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

		Map<FieldName, DataType> dataTypes = new HashMap<FieldName, DataType>();

		List<FieldName> activeFields = evaluator.getActiveFields();
		for(FieldName activeField : activeFields){
			DataField dataField = evaluator.getDataField(activeField);

			dataTypes.put(activeField, dataField.getDataType());
		}

		List<FieldName> predictedFields = evaluator.getPredictedFields();
		for(FieldName predictedField : predictedFields){
			DataField dataField = evaluator.getDataField(predictedField);

			dataTypes.put(predictedField, dataField.getDataType());
		}

		boolean result = true;

		for(int i = 0; i < input.size(); i++){
			Map<FieldName, String> inputRow = input.get(i);
			Map<FieldName, String> outputRow = output.get(i);

			Map<FieldName, Object> parameters = new LinkedHashMap<FieldName, Object>();

			for(FieldName activeField : activeFields){
				DataType dataType = dataTypes.get(activeField);

				parameters.put(activeField, ParameterUtil.parse(dataType, inputRow.get(activeField)));
			}

			Map<FieldName, ?> predictions = evaluator.evaluate(parameters);

			for(FieldName predictedField : predictedFields){
				Object predictedValue = EvaluatorUtil.decode(predictions.get(predictedField));

				DataType dataType = dataTypes.get(predictedField);

				// The output data type is usually more relaxed than the input data type
				switch(dataType){
					case INTEGER:
					case FLOAT:
					case DOUBLE:
						dataType = DataType.DOUBLE;
						break;
					default:
						break;
				}

				result &= VerificationUtil.acceptable(ParameterUtil.parse(dataType, outputRow.get(predictedField)), predictedValue, BatchUtil.precision);
			}
		}

		return result;
	}

	// One part per million parts
	private static final double precision = 1d / (1000 * 1000);
}