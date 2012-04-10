/*
 * Copyright (c) 2011 University of Tartu
 */
package org.jpmml.example;

import java.math.*;
import java.util.*;

import org.jpmml.manager.*;

import org.dmg.pmml.*;

public class TreeModelManagerExample {

	static
	public void main(String... args) throws Exception {
		TreeModelEvaluator treeModelManager = createGolfingModel();

		Map<FieldName, Object> parameters = new HashMap<FieldName, Object>();
		parameters.put(new FieldName("temperature"), new BigDecimal("75"));
		parameters.put(new FieldName("humidity"), new BigDecimal("55"));
		parameters.put(new FieldName("windy"), "false");
		parameters.put(new FieldName("outlook"), "overcast");

		Node node = treeModelManager.scoreModel(parameters);

		System.out.println("Node id: " + node.getId());
		System.out.println("Node score: " + node.getScore());
	}

	static
	private TreeModelEvaluator createGolfingModel(){
		TreeModelManager treeModelManager = new TreeModelManager();

		TreeModel treeModel = treeModelManager.createClassificationModel();
		treeModel.setModelName("golfing");

		FieldName temperature = new FieldName("temperature");
		treeModelManager.addField(temperature, null, OpType.CONTINUOUS, DataType.DOUBLE, null);

		FieldName humidity = new FieldName("humidity");
		treeModelManager.addField(humidity, null, OpType.CONTINUOUS, DataType.DOUBLE, null);

		FieldName windy = new FieldName("windy");
		treeModelManager.addField(windy, null, OpType.CATEGORICAL, DataType.STRING, null);

		DataField windyData = treeModelManager.getDataField(windy);

		List<Value> windyDataValues = windyData.getValues();
		windyDataValues.add(new Value("true"));
		windyDataValues.add(new Value("false"));

		FieldName outlook = new FieldName("outlook");
		treeModelManager.addField(outlook, null, OpType.CATEGORICAL, DataType.STRING, null);

		DataField outlookData = treeModelManager.getDataField(outlook);

		List<Value> outlookDataValues = outlookData.getValues();
		outlookDataValues.add(new Value("sunny"));
		outlookDataValues.add(new Value("overcast"));
		outlookDataValues.add(new Value("rain"));

		FieldName whatIdo = new FieldName("whatIdo");
		treeModelManager.addField(whatIdo, null, OpType.CATEGORICAL, DataType.STRING, FieldUsageType.PREDICTED);

		DataField whatIdoData = treeModelManager.getDataField(whatIdo);

		List<Value> whatIdoDataValues = whatIdoData.getValues();
		whatIdoDataValues.add(new Value("will play"));
		whatIdoDataValues.add(new Value("may play"));
		whatIdoDataValues.add(new Value("no play"));

		Node n1 = treeModelManager.getOrCreateRoot();
		n1.setId("1");
		n1.setScore("will play");

		//
		// Upper half of the tree
		//

		Predicate n2Predicate = createSimplePredicate(outlook, SimplePredicate.Operator.EQUAL, "sunny");

		Node n2 = treeModelManager.addNode(n1, n2Predicate);
		n2.setId("2");
		n2.setScore("will play");

		Predicate n3Predicate = createCompoundPredicate(CompoundPredicate.BooleanOperator.SURROGATE,
			createSimplePredicate(temperature, SimplePredicate.Operator.LESS_THAN, "90"),
			createSimplePredicate(temperature, SimplePredicate.Operator.GREATER_THAN, "50")
		);

		Node n3 = treeModelManager.addNode(n2, n3Predicate);
		n3.setId("3");
		n3.setScore("will play");

		Predicate n4Predicate = createSimplePredicate(humidity, SimplePredicate.Operator.LESS_THAN, "80");

		Node n4 = treeModelManager.addNode(n3, n4Predicate);
		n4.setId("4");
		n4.setScore("will play");

		Predicate n5Predicate = createSimplePredicate(humidity, SimplePredicate.Operator.GREATER_OR_EQUAL, "80");

		Node n5 = treeModelManager.addNode(n3, n5Predicate);
		n5.setId("5");
		n5.setScore("no play");

		Predicate n6Predicate = createCompoundPredicate(CompoundPredicate.BooleanOperator.OR,
			createSimplePredicate(temperature, SimplePredicate.Operator.GREATER_OR_EQUAL, "90"),
			createSimplePredicate(temperature, SimplePredicate.Operator.LESS_OR_EQUAL, "50")
		);

		Node n6 = treeModelManager.addNode(n2, n6Predicate);
		n6.setId("6");
		n6.setScore("no play");

		//
		// Lower half of the tree
		//

		Predicate n7Predicate = createCompoundPredicate(CompoundPredicate.BooleanOperator.OR,
			createSimplePredicate(outlook, SimplePredicate.Operator.EQUAL, "overcast"),
			createSimplePredicate(outlook, SimplePredicate.Operator.EQUAL, "rain")
		);

		Node n7 = treeModelManager.addNode(n1, n7Predicate);
		n7.setId("7");
		n7.setScore("may play");

		Predicate n8Predicate = createCompoundPredicate(CompoundPredicate.BooleanOperator.AND,
			createSimplePredicate(temperature, SimplePredicate.Operator.GREATER_THAN, "60"),
			createSimplePredicate(temperature, SimplePredicate.Operator.LESS_THAN, "100"),
			createSimplePredicate(outlook, SimplePredicate.Operator.EQUAL, "overcast"),
			createSimplePredicate(humidity, SimplePredicate.Operator.LESS_THAN, "70"),
			createSimplePredicate(windy, SimplePredicate.Operator.EQUAL, "false")
		);

		Node n8 = treeModelManager.addNode(n7, n8Predicate);
		n8.setId("8");
		n8.setScore("may play");

		Predicate n9Predicate = createCompoundPredicate(CompoundPredicate.BooleanOperator.AND,
			createSimplePredicate(outlook, SimplePredicate.Operator.EQUAL, "rain"),
			createSimplePredicate(humidity, SimplePredicate.Operator.LESS_THAN, "70")
		);

		Node n9 = treeModelManager.addNode(n7, n9Predicate);
		n9.setId("9");
		n9.setScore("no play");

		return new TreeModelEvaluator(treeModelManager);
	}

	static
	private SimplePredicate createSimplePredicate(FieldName name, SimplePredicate.Operator operator, String value){
		SimplePredicate simplePredicate = new SimplePredicate(name, operator);
		simplePredicate.setValue(value);

		return simplePredicate;
	}

	static
	private CompoundPredicate createCompoundPredicate(CompoundPredicate.BooleanOperator operator, Predicate... predicates){
		CompoundPredicate compoundPredicate = new CompoundPredicate(operator);

		List<Predicate> content = compoundPredicate.getContent();
		content.addAll(Arrays.asList(predicates));

		return compoundPredicate;
	}
}