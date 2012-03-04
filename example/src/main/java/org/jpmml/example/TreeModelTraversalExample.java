/*
 * Copyright (c) 2012 University of Tartu
 */
package org.jpmml.example;

import java.io.*;
import java.util.*;

import org.jpmml.manager.*;

import org.dmg.pmml.*;

public class TreeModelTraversalExample {

	static
	public void main(String... args) throws Exception {

		if(args.length != 1){
			System.out.println("Usage: java " + TreeModelTraversalExample.class + " <Input file>");

			System.exit(-1);
		}

		File pmmlFile = new File(args[0]);

		PMML pmml = IOUtil.unmarshal(pmmlFile);

		traverse(pmml);
	}

	static
	private void traverse(PMML pmml){
		TreeModelManager treeModelManager = new TreeModelManager(pmml);

		traverse(treeModelManager.getOrCreateRoot(), "");
	}

	static
	private void traverse(Node node, String indent){
		Predicate predicate = node.getPredicate();
		if(predicate == null){
			throw new IllegalArgumentException("Missing predicate");
		}

		System.out.println(indent + "if(" + format(predicate) + "){");

		List<Node> children = node.getNodes();
		for(Node child : children){
			traverse(child, indent + "\t");
		}

		if(node.getScore() != null){
			System.out.println(indent + "\t" + "return (" + node.getScore() + ");");
		}

		System.out.println(indent + "}");
	}

	static
	private String format(Predicate predicate){

		if(predicate instanceof SimplePredicate){
			return format((SimplePredicate)predicate);
		} else

		if(predicate instanceof CompoundPredicate){
			return format((CompoundPredicate)predicate);
		}

		if(predicate instanceof True){
			return "true";
		} else

		if(predicate instanceof False){
			return "false";
		}

		throw new IllegalArgumentException("Unsupported predicate " + predicate);
	}

	static
	private String format(SimplePredicate simplePredicate){
		StringBuffer sb = new StringBuffer();

		sb.append((simplePredicate.getField()).getValue());
		sb.append(' ').append(format(simplePredicate.getOperator())).append(' ');
		sb.append(simplePredicate.getValue());

		return sb.toString();
	}

	static
	private String format(SimplePredicate.Operator operator){

		switch(operator){
			case EQUAL:
				return "==";
			case NOT_EQUAL:
				return "!=";
			case LESS_THAN:
				return "<";
			case LESS_OR_EQUAL:
				return "<=";
			case GREATER_THAN:
				return ">";
			case GREATER_OR_EQUAL:
				return ">=";
			default:
				throw new IllegalArgumentException();
		}
	}

	static
	private String format(CompoundPredicate compoundPredicate){
		StringBuffer sb = new StringBuffer();

		List<Predicate> predicates = compoundPredicate.getContent();

		sb.append('(').append(format(predicates.get(0))).append(')');

		for(Predicate predicate : predicates.subList(1, predicates.size())){
			sb.append(' ').append(format(compoundPredicate.getBooleanOperator())).append(' ');
			sb.append('(').append(format(predicate)).append(')');
		}

		return sb.toString();
	}

	static
	private String format(CompoundPredicate.BooleanOperator operator){

		switch(operator){
			case AND:
				return "&";
			case OR:
				return "|";
			case XOR:
				return "^";
			default:
				throw new IllegalArgumentException();
		}
	}
}