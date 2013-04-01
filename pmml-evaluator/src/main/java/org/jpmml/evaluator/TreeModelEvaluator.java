/*
 * Copyright (c) 2012 University of Tartu
 */
package org.jpmml.evaluator;

import java.util.*;

import org.jpmml.manager.*;

import org.dmg.pmml.*;

public class TreeModelEvaluator extends TreeModelManager implements Evaluator {

	public TreeModelEvaluator(PMML pmml){
		super(pmml);
	}

	public TreeModelEvaluator(PMML pmml, TreeModel treeModel){
		super(pmml, treeModel);
	}

	public TreeModelEvaluator(TreeModelManager parent){
		this(parent.getPmml(), parent.getModel());
	}

	public Object evaluateOld(Map<FieldName, ?> parameters){
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
	
	public String evaluate(Map<FieldName, ?> parameters) {
		
		String result = null;
		Node currentNode = null;
		Node rootNode = getOrCreateRoot();
		
		Predicate rootPredicate = rootNode.getPredicate();
		Boolean predicateResult = PredicateUtil.evaluatePredicate(rootPredicate, parameters);
		
		if (predicateResult!=null) {
			if (predicateResult.booleanValue()) {
				result = rootNode.getScore();
				currentNode = rootNode;
			}
		}
		else {
			// if root evaluates to "UNKNOWN" - we are done for all missing value 
			//  strategies, except default child
			
			switch (getModel().getMissingValueStrategy()) {
				case NONE: 
				case LAST_PREDICTION:  
				case NULL_PREDICTION:  
					break;
				/* use default node if available */
				case DEFAULT_CHILD:
					
					String defaultChildId = rootNode.getDefaultChild();
					if (defaultChildId==null) {
						throw new EvaluationException("Default child is undefined");
					}
					Node defauldChild = null;
					for(Node child : rootNode.getNodes()){
						if (child.getId()!=null && child.getId().equals(defaultChildId)) {
							defauldChild = child;
							break;
						}
					}

					if (defauldChild!=null) {
						//result = defauldChild.getScore();
						currentNode = defauldChild;
					}
					else {
						throw new EvaluationException("No default child");
					}
					break;
				default:
					throw new EvaluationException("Unsupported missing value strategy: " 
								+ getModel().getMissingValueStrategy());
			}
		}

		while (currentNode!=null && currentNode.getNodes()!=null && !currentNode.getNodes().isEmpty()) {			
			
			boolean pickedNextNode = false;
			for (Node node : currentNode.getNodes()) {				
				
				Predicate predicate = node.getPredicate();
				predicateResult = PredicateUtil.evaluatePredicate(predicate, parameters);
				
				if (predicateResult!=null) {
					if (predicateResult.booleanValue()) {
						result = node.getScore();
						currentNode = node;
						pickedNextNode = true;
						break;
					}
				}
				else {
					// UNKNOWN value from predicate evaluation
					
					switch (getModel().getMissingValueStrategy()) {
						/* same as FALSE for current predicate */
						case NONE: break;
						/* abort with current prediction */
						case LAST_PREDICTION:  
							currentNode = null; 
							break;
						/* abort with null prediction */
						case NULL_PREDICTION:  
							result = null; 
							currentNode = null; break;
						/* use default node if available */
						case DEFAULT_CHILD:
							
							String defaultChildId = node.getDefaultChild();
							if (defaultChildId==null) {
								throw new EvaluationException("Default child is undefined");
							}
							Node defauldChild = null;
							for(Node child : currentNode.getNodes()){
								if (child.getId()!=null && child.getId().equals(defaultChildId)) {
									defauldChild = child;
									break;
								}
							}

							if (defauldChild!=null) {
								result = defauldChild.getScore();
								currentNode = defauldChild;
							}
							else {
								throw new EvaluationException("No default child");
							}
							break;
						default:
							throw new EvaluationException("Unsupported missing value strategy: " 
										+ getModel().getMissingValueStrategy());
					}
				}
			}	
				
			// no abort yet and no node evaluated to TRUE
			if (currentNode!=null && !pickedNextNode) {
				switch (getModel().getNoTrueChildStrategy()) {
					case RETURN_LAST_PREDICTION:
						currentNode = null;
						break;
					case RETURN_NULL_PREDICTION:
						currentNode = null;
						result = null;
						break;
				}
			}
		}
		return result;
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