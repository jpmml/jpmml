/*
 * Copyright (c) 2012 University of Tartu
 */
package org.jpmml.example;

import java.io.*;
import java.util.*;

import org.jpmml.evaluator.*;
import org.jpmml.manager.*;

import org.dmg.pmml.*;

public class CsvEvaluationExample {

	static
	public void main(String[] args) throws Exception {

		if(args.length != 2){
			System.err.println("Usage: java " + CsvEvaluationExample.class.getName() + " <PMML file> <CSV file>");

			System.exit(-1);
		}

		File pmmlFile = new File(args[0]);

		PMML pmml = IOUtil.unmarshal(pmmlFile);

		File csvFile = new File(args[1]);

		evaluate(pmml, csvFile);
	}

	static
	private void evaluate(PMML pmml, File csvFile) throws Exception {
		PMMLManager pmmlManager = new PMMLManager(pmml);

		Evaluator evaluator = (Evaluator)pmmlManager.getModelManager(null, ModelEvaluatorFactory.getInstance());

		BufferedReader reader = new BufferedReader(new FileReader(csvFile));

		try {
			String headerLine = reader.readLine();
			if(isEmpty(headerLine)){
				return;
			}

			String separator = getSeparator(headerLine);

			List<String> header = parseLine(headerLine, separator);

			Map<FieldName, DataField> dataFields = new LinkedHashMap<FieldName, DataField>();

			header:
			for(int i = 0; i < header.size(); i++){
				FieldName name = new FieldName(header.get(i));

				DataField dataField = evaluator.getDataField(name);
				if(dataField == null){
					System.err.println("Ignoring column: " + name.getValue());

					continue header;
				}

				dataFields.put(name, dataField);
			}

			while(true){
				String bodyLine = reader.readLine();
				if(isEmpty(bodyLine)){
					break;
				}

				List<String> body = parseLine(bodyLine, separator);

				Map<FieldName, Object> parameters = new LinkedHashMap<FieldName, Object>();

				body:
				for(int i = 0; i < header.size(); i++){
					FieldName name = new FieldName(header.get(i));

					DataField dataField = dataFields.get(name);
					if(dataField == null){
						continue body;
					}

					parameters.put(name, ParameterUtil.parse(dataField, body.get(i)));
				}

				Object result = evaluator.evaluate(parameters);

				System.out.println("Model output: " + result);
			}
		} finally {
			reader.close();
		}
	}

	static
	private boolean isEmpty(String line){
		return line == null || (line).equals("");
	}

	static
	private String getSeparator(String line){

		if((line.split(";")).length > 1){
			return ";";
		} else

		if((line.split(",")).length > 1){
			return ",";
		}

		return ";";
	}

	static
	private List<String> parseLine(String line, String separator){
		List<String> result = new ArrayList<String>();

		String[] cells = line.split(separator);
		for(String cell : cells){
			result.add(cell.replace(',', '.'));
		}

		return result;
	}
}