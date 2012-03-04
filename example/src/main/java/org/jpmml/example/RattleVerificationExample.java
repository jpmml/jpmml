/*
 * Copyright (c) 2012 University of Tartu
 */
package org.jpmml.example;

import java.io.*;
import java.math.*;
import java.util.*;

import org.jpmml.manager.*;

import org.dmg.pmml.*;

/**
 * Sample application for verifying Rattle (R Data Miner) training results, which expects three input files:
 * <ol>
 *   <li> PMML file - The predictive model, eg. Iris_rf.xml (Rattle tab &quot;Model&quot;)
 *   <li> Training CSV file - The training dataset, eg. Iris.csv (Rattle tab &quot;Data&quot;)
 *   <li> Training score CSV file - The results of re-evaluating the predictive model with the training dataset, eg. Iris_train_score_idents.csv (Rattle tab &quot;Evaluate&quot;)
 * </ol>
 */
public class RattleVerificationExample {

	static
	public void main(String... args) throws Exception {

		if(args.length != 3){
			System.out.println("Usage: java " + RattleVerificationExample.class.getName() + " <PMML file> <Training CSV file> <Training score CSV file>");

			System.exit(-1);
		}

		File pmmlFile = new File(args[0]);

		PMML pmml = CopyExample.readPmml(pmmlFile);

		File trainingFile = new File(args[1]);
		File trainingResultsFile = new File(args[2]);

		List<List<String>> trainingTable = readCSV(trainingFile);
		List<List<String>> trainingScoreTable = readCSV(trainingResultsFile);

		if(trainingTable.size() != trainingScoreTable.size()){
			throw new IllegalArgumentException();
		}

		score(pmml, trainingTable, trainingScoreTable);
	}

	static
	public void score(PMML pmml, List<List<String>> table, List<List<String>> scoreTable) throws Exception {
		PMMLManager pmmlManager = new PMMLManager(pmml);

		ModelManager<?> modelManager = pmmlManager.getModelManager(null);

		List<FieldName> names = modelManager.getFields(FieldUsageTypeType.ACTIVE);

		Map<FieldName, DataField> nameDataFields = new LinkedHashMap<FieldName, DataField>();
		for(FieldName name : names){
			nameDataFields.put(name, modelManager.getDataField(name));
		}

		List<FieldName> scoreNames = modelManager.getFields(FieldUsageTypeType.PREDICTED);
		if(scoreNames.size() != 1){
			throw new IllegalArgumentException();
		}

		FieldName scoreName = scoreNames.get(0);
		DataField scoreDataField = modelManager.getDataField(scoreName);

		List<String> headerRow = table.get(0);

		for(int i = 1; i < table.size(); i++){
			Map<FieldName, Object> parameters = new LinkedHashMap<FieldName, Object>();

			List<String> bodyRow = table.get(i);

			for(int j = 0; j < headerRow.size(); j++){
				FieldName name = new FieldName(headerRow.get(j));
				DataField dataField = nameDataFields.get(name);

				if(dataField == null){
					continue;
				}

				parameters.put(name, ParameterUtil.parse(dataField, bodyRow.get(j)));
			}

			Object result = modelManager.evaluate(parameters);

			List<String> scoreBodyRow = scoreTable.get(i);

			Object scoreResult = ParameterUtil.parse(scoreDataField, scoreBodyRow.get(1));

			boolean equal = checkEquality(result, scoreResult, 6);
			if(!equal){
				System.err.println("Line " + i + ": value " + result + ", expected score value " + scoreResult);
			}
		}
	}

	static
	private boolean checkEquality(Object left, Object right, int scale){

		if(left == null || right == null){
			return (left == right);
		} // End if

		if(left instanceof Number && right instanceof Number){
			BigDecimal leftDecimal = new BigDecimal(left.toString());
			BigDecimal rightDecimal = new BigDecimal(right.toString());

			leftDecimal = leftDecimal.setScale(scale, RoundingMode.HALF_UP);
			rightDecimal = rightDecimal.setScale(scale, RoundingMode.HALF_UP);

			return (leftDecimal).compareTo(rightDecimal) == 0;
		} else

		{
			String leftString = (String)left;
			String rightString = (String)right;

			return (leftString).equals(rightString);
		}
	}

	static
	private List<List<String>> readCSV(File file) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(file));

		try {
			List<List<String>> table = new ArrayList<List<String>>();

			while(true){
				String line = reader.readLine();
				if(line == null || (line).equals("")){
					break;
				}

				List<String> row = Arrays.asList(line.split(","));
				table.add(row);
			}

			return table;
		} finally {
			reader.close();
		}
	}
}