/*
 * Copyright (c) 2013 University of Tartu
 */
package org.jpmml.evaluator;

import java.util.*;

import org.jpmml.manager.*;

import org.dmg.pmml.*;

import com.google.common.collect.*;

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

		List<Map<FieldName, String>> input = CsvUtil.load(batch.getInput());
		List<Map<FieldName, String>> output = CsvUtil.load(batch.getOutput());

		Evaluator evaluator = (Evaluator)modelManager;

		List<Map<FieldName, Object>> table = Lists.newArrayList();

		List<FieldName> activeFields = evaluator.getActiveFields();
		List<FieldName> groupFields = evaluator.getGroupFields();
		List<FieldName> predictedFields = evaluator.getPredictedFields();
		List<FieldName> outputFields = evaluator.getOutputFields();

		List<FieldName> inputFields = Lists.newArrayList();
		inputFields.addAll(activeFields);
		inputFields.addAll(groupFields);

		for(int i = 0; i < input.size(); i++){
			Map<FieldName, String> inputRow = input.get(i);

			Map<FieldName, Object> arguments = Maps.newLinkedHashMap();

			for(FieldName inputField : inputFields){
				String inputCell = inputRow.get(inputField);

				Object inputValue = evaluator.prepare(inputField, inputCell);

				arguments.put(inputField, inputValue);
			}

			table.add(arguments);
		}

		if(groupFields.size() == 1){
			FieldName groupField = groupFields.get(0);

			table = EvaluatorUtil.groupRows(groupField, table);
		} else

		if(groupFields.size() > 1){
			throw new EvaluationException();
		} // End if

		if(output.isEmpty()){

			for(int i = 0; i < table.size(); i++){
				Map<FieldName, ?> arguments = table.get(i);

				evaluator.evaluate(arguments);
			}

			return true;
		} else

		{
			if(table.size() != output.size()){
				throw new EvaluationException();
			}

			boolean success = true;

			for(int i = 0; i < output.size(); i++){
				Map<FieldName, String> outputRow = output.get(i);

				Map<FieldName, ?> arguments = table.get(i);

				Map<FieldName, ?> result = evaluator.evaluate(arguments);

				for(FieldName predictedField : predictedFields){
					String outputCell = outputRow.get(predictedField);

					Object predictedValue = EvaluatorUtil.decode(result.get(predictedField));

					success &= acceptable(outputCell, predictedValue);
				}

				for(FieldName outputField : outputFields){
					String outputCell = outputRow.get(outputField);

					Object computedValue = result.get(outputField);

					success &= (outputCell != null ? acceptable(outputCell, computedValue) : acceptable(computedValue));
				}
			}
			return success;
		}
	}

	static
	private boolean acceptable(Object actual){
		return (actual != null);
	}

	static
	private boolean acceptable(String expected, Object actual){
		return VerificationUtil.acceptable(ParameterUtil.parse(ParameterUtil.getDataType(actual), expected), actual, BatchUtil.precision, BatchUtil.zeroThreshold);
	}

	// One part per million parts
	private static final double precision = 1d / (1000 * 1000);

	private static final double zeroThreshold = precision;
}