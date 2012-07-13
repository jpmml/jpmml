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

		if(args.length < 2 || args.length > 3){
			System.err.println("Usage: java " + CsvEvaluationExample.class.getName() + " <PMML file> <CSV input file> <CSV output file>?");

			System.exit(-1);
		}

		File pmmlFile = new File(args[0]);

		PMML pmml = IOUtil.unmarshal(pmmlFile);

		File inputFile = new File(args[1]);
		File outputFile = (args.length > 2 ? new File(args[2]) : null);

		evaluate(pmml, inputFile, outputFile);
	}

	static
	private void evaluate(PMML pmml, File inputFile, File outputFile) throws Exception {
		PMMLManager pmmlManager = new PMMLManager(pmml);

		Evaluator evaluator = (Evaluator)pmmlManager.getModelManager(null, ModelEvaluatorFactory.getInstance());

		List<String> lines = new ArrayList<String>();

		BufferedReader reader = new BufferedReader(new FileReader(inputFile));

		try {
			String headerLine = reader.readLine();
			if(isEmpty(headerLine)){
				return;
			}

			String separator = getSeparator(headerLine);

			lines.add(headerLine + separator + "JPMML");

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

				lines.add(bodyLine + separator + result);
			}
		} finally {
			reader.close();
		}

		if(outputFile == null){
			return;
		}

		BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));

		try {

			for(String line : lines){
				writer.write(line + "\n");
			}

			writer.flush();
		} finally {
			writer.close();
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