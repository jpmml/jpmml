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
			List<String> header = parseHeader(reader.readLine());

			while(true){
				List<String> body = parseBody(reader.readLine());
				if(body == null){
					break;
				}

				Map<FieldName, String> parameters = new LinkedHashMap<FieldName, String>();

				for(int i = 0; i < header.size(); i++){
					parameters.put(new FieldName(header.get(i)), body.get(i));
				}

				Object result = evaluator.evaluate(parameters);

				System.out.println("Model output: " + result);
			}
		} finally {
			reader.close();
		}
	}

	static
	private List<String> parseHeader(String line){
		String[] cells = line.split(";");

		return Arrays.asList(cells);
	}

	static
	private List<String> parseBody(String line){

		if(line == null){
			return null;
		}

		List<String> result = new ArrayList<String>();

		String[] cells = line.split(";");
		for(String cell : cells){
			result.add(cell.replace(',', '.'));
		}

		return result;
	}
}