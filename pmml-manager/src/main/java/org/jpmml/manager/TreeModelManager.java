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
		this(pmml, find(pmml.getContent(), TreeModel.class));
	}

	public TreeModelManager(PMML pmml, TreeModel treeModel){
		super(pmml);

		this.treeModel = treeModel;
	}

	@Override
	public TreeModel getModel(){
		ensureNotNull(this.treeModel);

		return this.treeModel;
	}

	/**
	 * @throws ModelManagerException If the Model already exists
	 *
	 * @see #getModel()
	 */
	public TreeModel createModel(MiningFunctionType miningFunction){
		ensureNull(this.treeModel);

		this.treeModel = new TreeModel(new MiningSchema(), new Node(), miningFunction);

		List<Model> content = getPmml().getContent();
		content.add(this.treeModel);

		return this.treeModel;
	}

	/**
	 * @return The root Node. Its predicate is the constant TRUE.
	 */
	public Node getOrCreateNode(){

		if(this.node == null){
			TreeModel treeModel = getModel();

			this.node = treeModel.getNode();
			if(this.node == null){
				this.node = new Node();

				treeModel.setNode(this.node);
			}

			Predicate predicate = this.node.getPredicate();
			if(predicate == null){
				this.node.setPredicate(new True());
			}
		}

		return this.node;
	}

	/**
	 * Adds a new Node to the root Node.
	 *
	 * @return The newly added Node
	 *
	 * @see #getOrCreateNode()
	 */
	public Node addNode(Predicate predicate){
		return addNode(getOrCreateNode(), predicate);
	}

	/**
	 * Adds a new Node to the specified Node.
	 *
	 * @return The newly added Node
	 */
	public Node addNode(Node parentNode, Predicate predicate){
		Node node = new Node();
		node.setPredicate(predicate);

		parentNode.getNodes().add(node);

		return node;
	}

	public ScoreDistribution getOrAddScoreDistribution(Node node, String value){
		List<ScoreDistribution> scoreDistributions = node.getScoreDistributions();

		for(ScoreDistribution scoreDistribution : scoreDistributions){

			if((scoreDistribution.getValue()).equals(value)){
				return scoreDistribution;
			}
		}

		ScoreDistribution scoreDistribution = new ScoreDistribution(value, 0);
		scoreDistributions.add(scoreDistribution);

		return scoreDistribution;
	}

	@Override
	public String evaluate(Map<FieldName, ?> parameters){
		Node node = scoreModel(parameters);

		if(node != null){
			String score = node.getScore();
			if(score != null){
				return score;
			}

			return computeScore(node);
		}

		return null;
	}

	private String computeScore(Node node){
		ScoreDistribution result = null;

		List<ScoreDistribution> scoreDistributions = node.getScoreDistributions();

		for(ScoreDistribution scoreDistribution : scoreDistributions){

			if(result == null || result.getRecordCount() < scoreDistribution.getRecordCount()){
				result = scoreDistribution;
			}
		}

		return result != null ? result.getValue() : null;
	}

	public Node scoreModel(Map<FieldName, ?> parameters){
		Node root = getOrCreateNode();

		Prediction prediction = findTrueChild(root, root, parameters); // XXX

		if(prediction.getLastTrueNode() != null && prediction.getTrueNode() != null && !(prediction.getLastTrueNode()).equals(prediction.getTrueNode())){
			return prediction.getTrueNode();
		} else

		{
			NoTrueChildStrategyType noTrueChildStrategy = getModel().getNoTrueChildStrategy();
			switch(noTrueChildStrategy){
				case RETURN_NULL_PREDICTION:
					return null;
				case RETURN_LAST_PREDICTION:
					return prediction.getLastTrueNode();
				default:
					throw new EvaluationException();
			}
		}
	}

	private Prediction findTrueChild(Node lastNode, Node node, Map<FieldName, ?> parameters){
		Boolean value = evaluateNode(node, parameters);

		if(value == null){
			throw new EvaluationException();
		} // End if

		if(value.booleanValue()){
			List<Node> children = node.getNodes();

			for(Node child : children){
				Prediction childPrediction = findTrueChild(node, child, parameters);

				if(childPrediction.getTrueNode() != null){
					return childPrediction;
				}
			}

			return new Prediction(lastNode, node);
		} else

		{
			return new Prediction(lastNode, null);
		}
	}

	private Boolean evaluateNode(Node node, Map<FieldName, ?> parameters){
		Predicate predicate = node.getPredicate();
		if(predicate == null){
			throw new EvaluationException();
		}

		return evaluatePredicate(predicate, parameters);
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
	private class Prediction {

		private Node lastTrueNode = null;

		private Node trueNode = null;


		public Prediction(Node lastTrueNode, Node trueNode){
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