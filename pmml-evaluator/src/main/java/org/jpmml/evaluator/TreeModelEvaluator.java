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

	public Object prepare(FieldName name, Object value){
		return ParameterUtil.prepare(getDataField(name), getMiningField(name), value);
	}

	public Map<FieldName, ?> evaluate(Map<FieldName, ?> parameters){
		TreeModel treeModel = getModel();
		if(!treeModel.isScorable()){
			throw new InvalidResultException(treeModel);
		}

		ModelManagerEvaluationContext context = new ModelManagerEvaluationContext(this, parameters);

		Node node = evaluateTree(context);

		NodeClassificationMap values = null;

		if(node != null){
			values = new NodeClassificationMap(node);
		}

		Map<FieldName, NodeClassificationMap> predictions = Collections.singletonMap(getTarget(), values);

		return OutputUtil.evaluate(predictions, context);
	}

	Node evaluateTree(EvaluationContext context){
		TreeModel treeModel = getModel();

		Node root = getOrCreateRoot();

		LinkedList<Node> trail = new LinkedList<Node>();

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

		return PredicateUtil.evaluate(predicate, context);
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