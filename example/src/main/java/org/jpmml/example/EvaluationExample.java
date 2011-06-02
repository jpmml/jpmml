/*
 * Copyright (c) 2011 University of Tartu
 */
package org.jpmml.example;

import java.io.*;
import java.util.*;

import org.jpmml.manager.*;

import org.dmg.pmml.*;

public class EvaluationExample {

	static
	public void main(String... args) throws Exception {

		if(args.length != 1){
			System.err.println("Usage: java " + EvaluationExample.class.getName() + " <PMML file>");

			System.exit(-1);
		}

		File pmmlFile = new File(args[0]);

		PMML pmml = CopyExample.readPmml(pmmlFile);

		evaluate(pmml);
	}

	static
	public void evaluate(PMML pmml) throws Exception {
		PMMLManager pmmlManager = new PMMLManager(pmml);

		// Load the default model
		ModelManager<?> modelManager = pmmlManager.getModelManager(null);

		Map<FieldName, ?> parameters = readParameters(modelManager);

		Object result = modelManager.evaluate(parameters);
		System.out.println("Model output: " + result);
	}

	static
	public Map<FieldName, ?> readParameters(ModelManager<?> modelManager) throws IOException {
		Map<FieldName, Object> parameters = new LinkedHashMap<FieldName, Object>();

		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

		try {
			List<FieldName> names = modelManager.getFields(FieldUsageTypeType.ACTIVE);
			System.out.println("Model input " + names.size() + " parameter(s):");

			for(int i = 0; i < names.size(); i++){
				FieldName name = names.get(i);

				DataField dataField = modelManager.getDataField(name);
				System.out.print("#" + (i + 1) + " (displayName=" + dataField.getDisplayName() + ", dataType=" + dataField.getDataType()+ "): ");

				String input = reader.readLine();
				if(input == null){
					throw new EOFException();
				}

				parameters.put(name, parse(dataField.getDataType(), input));
			}
		} finally {
			reader.close();
		}

		return parameters;
	}

	static
	private Object parse(DataTypeType dataType, String string){

		switch(dataType){
			case STRING:
				return string;
			case INTEGER:
				return new Integer(string);
			case FLOAT:
				return new Float(string);
			case DOUBLE:
				return new Double(string);
			default:
				throw new IllegalArgumentException();
		}
	}
}