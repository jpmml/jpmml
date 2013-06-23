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

		Node trueChild = null;

		Boolean status = evaluateNode(root, context);
		if(status != null && status.booleanValue()){
			trueChild = findTrueChild(root, trail, context);
		} // End if

		if(trueChild == null){
			NoTrueChildStrategyType noTrueChildStrategy = treeModel.getNoTrueChildStrategy();

			switch(noTrueChildStrategy){
				case RETURN_NULL_PREDICTION:
					break;
				case RETURN_LAST_PREDICTION:
					trueChild = trail.peekLast();
					break;
				default:
					throw new UnsupportedFeatureException(treeModel, noTrueChildStrategy);
			}
		}

		return trueChild;
	}

	private Node findTrueChild(Node node, List<Node> trail, EvaluationContext context){
		List<Node> children = node.getNodes();

		// A "true" leaf node
		if(children.isEmpty()){
			return node;
		}

		trail.add(node);

		for(Node child : children){
			Boolean status = evaluateNode(child, context);

			if(status != null && status.booleanValue()){
				return findTrueChild(child, trail, context);
			}
		}

		// A branch node with no "true" leaf nodes
		return null;
	}

	private Boolean evaluateNode(Node node, EvaluationContext context){
		Predicate predicate = node.getPredicate();
		if(predicate == null){
			throw new InvalidFeatureException(node);
		}

		return PredicateUtil.evaluate(predicate, context);
	}
}