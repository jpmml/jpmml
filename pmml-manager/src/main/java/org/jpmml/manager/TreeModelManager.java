/*
 * Copyright (c) 2009 University of Tartu
 */
package org.jpmml.manager;

import java.util.*;

import org.dmg.pmml.*;

public class TreeModelManager extends ModelManager<TreeModel> {

	private TreeModel treeModel = null;

	private Node node = null;


	public TreeModelManager(){
	}

	public TreeModelManager(PMML pmml){
		super(pmml);
	}

	public TreeModelManager(PMML pmml, TreeModel treeModel){
		super(pmml);

		this.treeModel = treeModel;
	}

	@Override
	public TreeModel getOrCreateModel(){

		if(this.treeModel == null){
			List<Model> content = getPmml().getContent();

			this.treeModel = find(content, TreeModel.class);
			if(this.treeModel == null){
				this.treeModel = new TreeModel(new MiningSchema(), new Node(), MiningFunctionType.CLASSIFICATION);

				content.add(this.treeModel);
			}
		}

		return this.treeModel;
	}

	public Node getOrCreateNode(){

		if(this.node == null){
			TreeModel treeModel = getOrCreateModel();

			this.node = treeModel.getNode();
			if(this.node == null){
				this.node = new Node();

				treeModel.setNode(this.node);
			}

			List<Predicate> content = this.node.getContent();
			if(content.isEmpty()){
				content.add(new True());
			}
		}

		return this.node;
	}


	public Node addNode(Predicate predicate){
		return addNode(getOrCreateNode(), predicate);
	}

	public Node addNode(Node parentNode, Predicate predicate){
		Node node = new Node();
		node.getContent().add(predicate);

		parentNode.getNodes().add(node);

		return node;
	}

	@Override
	public String evaluate(Map<FieldName, ?> parameters){
		Node node = scoreModel(parameters);

		if(node != null){
			return node.getScore();
		}

		return null;
	}

	public Node scoreModel(Map<FieldName, ?> parameters){
		Node root = getOrCreateNode();

		Result result = findTrueChild(root, root, parameters); // XXX
		if(result.getLastTrueNode() != null && result.getTrueNode() != null && !(result.getLastTrueNode()).equals(result.getTrueNode())){
			return result.getTrueNode();
		} else

		{
			NoTrueChildStrategyType noTrueChildStrategy = getOrCreateModel().getNoTrueChildStrategy();
			switch(noTrueChildStrategy){
				case RETURN_NULL_PREDICTION:
					return null;
				case RETURN_LAST_PREDICTION:
					return result.getLastTrueNode();
				default:
					throw new EvaluationException();
			}
		}
	}

	private Result findTrueChild(Node lastNode, Node node, Map<FieldName, ?> parameters){
		Boolean value = evaluateNode(node, parameters);

		if(value == null){
			throw new EvaluationException();
		} // End if

		if(value.booleanValue()){
			List<Node> children = node.getNodes();

			for(Node child : children){
				Result childResult = findTrueChild(node, child, parameters);

				if(childResult.getTrueNode() != null){
					return childResult;
				}
			}

			return new Result(lastNode, node);
		} else

		{
			return new Result(lastNode, null);
		}
	}

	private Boolean evaluateNode(Node node, Map<FieldName, ?> parameters){
		List<Predicate> predicates = node.getContent();

		if(predicates.size() != 1){
			throw new EvaluationException();
		}

		return evaluatePredicate(predicates.get(0), parameters);
	}

	private Boolean evaluatePredicate(Predicate predicate, Map<FieldName, ?> parameters){

		if(predicate instanceof SimplePredicate){
			return evaluateSimplePredicate((SimplePredicate)predicate, parameters);
		} else

		if(predicate instanceof CompoundPredicate){
			return evaluateCompoundPredicate((CompoundPredicate)predicate, parameters);
		} else

		if(predicate instanceof SimpleSetPredicate){
			return evaluateSimpleSetPredicate((SimpleSetPredicate)predicate, parameters);
		} else

		if(predicate instanceof True){
			return evaluateTruePredicate((True)predicate);
		} else

		if(predicate instanceof False){
			return evaluateFalsePredicate((False)predicate);
		}

		throw new EvaluationException();
	}

	private Boolean evaluateSimplePredicate(SimplePredicate simplePredicate, Map<FieldName, ?> parameters){
		Object fieldValue = getParameterValue(parameters, simplePredicate.getField(), true);

		switch(simplePredicate.getOperator()){
			case IS_MISSING:
				return Boolean.valueOf(fieldValue == null);
			case IS_NOT_MISSING:
				return Boolean.valueOf(fieldValue != null);
			default:
				break;
		}

		if(fieldValue == null){
			return null;
		}

		String value = simplePredicate.getValue();

		switch(simplePredicate.getOperator()){
			case EQUAL:
				return Boolean.valueOf(PredicateUtil.compare(fieldValue, value) == 0);
			case NOT_EQUAL:
				return Boolean.valueOf(PredicateUtil.compare(fieldValue, value) != 0);
			case LESS_THAN:
				return Boolean.valueOf(PredicateUtil.compare(fieldValue, value) < 0);
			case LESS_OR_EQUAL:
				return Boolean.valueOf(PredicateUtil.compare(fieldValue, value) <= 0);
			case GREATER_THAN:
				return Boolean.valueOf(PredicateUtil.compare(fieldValue, value) > 0);
			case GREATER_OR_EQUAL:
				return Boolean.valueOf(PredicateUtil.compare(fieldValue, value) >= 0);
			default:
				break;
		}

		throw new EvaluationException();
	}

	private Boolean evaluateCompoundPredicate(CompoundPredicate compoundPredicate, Map<FieldName, ?> parameters){
		List<Predicate> predicates = compoundPredicate.getContent();

		Boolean result = evaluatePredicate(predicates.get(0), parameters);

		switch(compoundPredicate.getBooleanOperator()){
			case AND:
			case OR:
			case XOR:
				break;
			case SURROGATE:
				if(result != null){
					return result;
				}
				break;
		}

		for(Predicate predicate : predicates.subList(1, predicates.size())){
			Boolean value = evaluatePredicate(predicate, parameters);

			switch(compoundPredicate.getBooleanOperator()){
				case AND:
					result = PredicateUtil.binaryAnd(result, value);
					break;
				case OR:
					result = PredicateUtil.binaryOr(result, value);
					break;
				case XOR:
					result = PredicateUtil.binaryXor(result, value);
					break;
				case SURROGATE:
					if(value != null){
						return value;
					}
					break;
			}
		}

		return result;
	}

	private Boolean evaluateSimpleSetPredicate(SimpleSetPredicate simpleSetPredicate, Map<FieldName, ?> parameters){
		throw new EvaluationException();
	}

	private Boolean evaluateTruePredicate(True truePredicate){
		return Boolean.TRUE;
	}

	private Boolean evaluateFalsePredicate(False falsePredicate){
		return Boolean.FALSE;
	}

	static
	private class Result {

		private Node lastTrueNode = null;

		private Node trueNode = null;


		public Result(Node lastTrueNode, Node trueNode){
			this.lastTrueNode = lastTrueNode;
			this.trueNode = trueNode;
		}

		public Node getLastTrueNode(){
			return this.lastTrueNode;
		}

		public Node getTrueNode(){
			return this.trueNode;
		}
	}
}