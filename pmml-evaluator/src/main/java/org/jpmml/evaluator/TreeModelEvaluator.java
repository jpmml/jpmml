/*
 * Copyright (c) 2012 University of Tartu
 */
package org.jpmml.evaluator;

import java.util.*;

import org.jpmml.manager.*;

import org.dmg.pmml.*;

import com.google.common.collect.*;

public class TreeModelEvaluator extends TreeModelManager implements Evaluator {

	private BiMap<String, Node> entities = null;


	public TreeModelEvaluator(PMML pmml){
		super(pmml);
	}

	public TreeModelEvaluator(PMML pmml, TreeModel treeModel){
		super(pmml, treeModel);
	}

	@Override
	public BiMap<String, Node> getEntityRegistry(){

		if(this.entities == null){
			this.entities = super.getEntityRegistry();
		}

		return this.entities;
	}

	@Override
	public FieldValue prepare(FieldName name, Object value){
		return ArgumentUtil.prepare(getDataField(name), getMiningField(name), value);
	}

	@Override
	public Map<FieldName, ?> evaluate(Map<FieldName, ?> arguments){
		TreeModel treeModel = getModel();
		if(!treeModel.isScorable()){
			throw new InvalidResultException(treeModel);
		}

		Node node;

		ModelManagerEvaluationContext context = new ModelManagerEvaluationContext(this);
		context.pushFrame(arguments);

		MiningFunctionType miningFunction = treeModel.getFunctionName();
		switch(miningFunction){
			case REGRESSION:
			case CLASSIFICATION:
				node = evaluateTree(context);
				break;
			default:
				throw new UnsupportedFeatureException(treeModel, miningFunction);
		}

		NodeClassificationMap values = null;

		if(node != null){
			values = createNodeClassificationMap(node);
		}

		Map<FieldName, ? extends ClassificationMap> predictions = TargetUtil.evaluateClassification(values, context);

		return OutputUtil.evaluate(predictions, context);
	}

	private Node evaluateTree(ModelManagerEvaluationContext context){
		TreeModel treeModel = getModel();

		Node root = getRoot();
		if(root == null){
			throw new InvalidFeatureException(treeModel);
		}

		LinkedList<Node> trail = Lists.newLinkedList();

		NodeResult result = new NodeResult(null);

		Boolean status = evaluateNode(root, context);
		if(status == null){
			result = handleMissingValue(root, trail, context);
		} else

		if(status.booleanValue()){
			result = handleTrue(root, trail, context);
		} // End if

		if(result == null){
			throw new MissingResultException(root);
		}

		Node node = result.getNode();

		if(node != null || result.isFinal()){
			return node;
		}

		NoTrueChildStrategyType noTrueChildStrategy = treeModel.getNoTrueChildStrategy();
		switch(noTrueChildStrategy){
			case RETURN_NULL_PREDICTION:
				return null;
			case RETURN_LAST_PREDICTION:
				return lastPrediction(root, trail);
			default:
				throw new UnsupportedFeatureException(treeModel, noTrueChildStrategy);
		}
	}

	private NodeResult handleMissingValue(Node node, LinkedList<Node> trail, EvaluationContext context){
		TreeModel treeModel = getModel();

		MissingValueStrategyType missingValueStrategy = treeModel.getMissingValueStrategy();
		switch(missingValueStrategy){
			case NULL_PREDICTION:
				return new FinalNodeResult(null);
			case LAST_PREDICTION:
				return new FinalNodeResult(lastPrediction(node, trail));
			case NONE:
				return null;
			default:
				throw new UnsupportedFeatureException(treeModel, missingValueStrategy);
		}
	}

	private NodeResult handleTrue(Node node, LinkedList<Node> trail, EvaluationContext context){
		List<Node> children = node.getNodes();

		// A "true" leaf node
		if(children.isEmpty()){
			return new NodeResult(node);
		}

		trail.add(node);

		for(Node child : children){
			Boolean status = evaluateNode(child, context);

			if(status == null){
				NodeResult result = handleMissingValue(child, trail, context);
				if(result != null){
					return result;
				}
			} else

			if(status.booleanValue()){
				return handleTrue(child, trail, context);
			}
		}

		// A branch node with no "true" leaf nodes
		return new NodeResult(null);
	}

	private Node lastPrediction(Node node, LinkedList<Node> trail){

		try {
			return trail.getLast();
		} catch(NoSuchElementException nsee){
			throw new MissingResultException(node);
		}
	}

	private Boolean evaluateNode(Node node, EvaluationContext context){
		Predicate predicate = node.getPredicate();
		if(predicate == null){
			throw new InvalidFeatureException(node);
		}

		EmbeddedModel embeddedModel = node.getEmbeddedModel();
		if(embeddedModel != null){
			throw new UnsupportedFeatureException(embeddedModel);
		}

		return PredicateUtil.evaluate(predicate, context);
	}

	static
	private NodeClassificationMap createNodeClassificationMap(Node node){
		NodeClassificationMap result = new NodeClassificationMap(node);

		List<ScoreDistribution> scoreDistributions = node.getScoreDistributions();

		double sum = 0;

		for(ScoreDistribution scoreDistribution : scoreDistributions){
			sum += scoreDistribution.getRecordCount();
		} // End for

		for(ScoreDistribution scoreDistribution : scoreDistributions){
			Double value = scoreDistribution.getProbability();
			if(value == null){
				value = (scoreDistribution.getRecordCount() / sum);
			}

			result.put(scoreDistribution.getValue(), value);
		}

		return result;
	}

	static
	private class NodeResult {

		private Node node = null;


		public NodeResult(Node node){
			setNode(node);
		}

		/**
		 * @return <code>true</code> if the result should be exempt from any post-processing (eg. "no true child strategy" treatment), <code>false</code> otherwise.
		 */
		public boolean isFinal(){
			return false;
		}

		public Node getNode(){
			return this.node;
		}

		private void setNode(Node node){
			this.node = node;
		}
	}

	static
	private class FinalNodeResult extends NodeResult {

		public FinalNodeResult(Node node){
			super(node);
		}

		@Override
		public boolean isFinal(){
			return true;
		}
	}
}