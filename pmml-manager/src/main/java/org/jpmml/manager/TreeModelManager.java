/*
 * Copyright (c) 2009 University of Tartu
 */
package org.jpmml.manager;

import java.util.*;

import org.dmg.pmml.*;

public class TreeModelManager extends ModelManager<TreeModel> {

	private TreeModel treeModel = null;

	private Node root = null;


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
	 * @return The root Node
	 */
	public Node getOrCreateRoot(){

		if(this.root == null){
			TreeModel treeModel = getModel();

			this.root = treeModel.getNode();
			if(this.root == null){
				this.root = new Node();

				treeModel.setNode(this.root);
			}

			Predicate predicate = this.root.getPredicate();
			if(predicate == null){
				this.root.setPredicate(new True());
			}
		}

		return this.root;
	}

	/**
	 * Adds a new Node to the root Node.
	 *
	 * @return The newly added Node
	 *
	 * @see #getOrCreateRoot()
	 */
	public Node addNode(Predicate predicate){
		return addNode(getOrCreateRoot(), predicate);
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
		Node root = getOrCreateRoot();

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
					throw new UnsupportedFeatureException(noTrueChildStrategy);
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

		return PredicateUtil.evaluatePredicate(predicate, parameters);
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