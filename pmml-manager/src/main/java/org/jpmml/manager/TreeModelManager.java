/*
 * Copyright (c) 2009 University of Tartu
 */
package org.jpmml.manager;

import java.util.*;

import org.dmg.pmml.*;

public class TreeModelManager extends ModelManager<TreeModel> {

	private TreeModel treeModel = null;


	public TreeModelManager(){
	}

	public TreeModelManager(PMML pmml){
		this(pmml, find(pmml.getContent(), TreeModel.class));
	}

	public TreeModelManager(PMML pmml, TreeModel treeModel){
		super(pmml);

		this.treeModel = treeModel;
	}

	public String getSummary(){
		return "Tree";
	}

	@Override
	public TreeModel getModel(){
		ensureNotNull(this.treeModel);

		return this.treeModel;
	}

	public TreeModel createClassificationModel(){
		return createModel(MiningFunctionType.CLASSIFICATION);
	}

	/**
	 * @see #getModel()
	 */
	public TreeModel createModel(MiningFunctionType miningFunction){
		ensureNull(this.treeModel);

		Node root = new Node();
		root.setPredicate(new True());

		this.treeModel = new TreeModel(new MiningSchema(), root, miningFunction);

		getModels().add(this.treeModel);

		return this.treeModel;
	}

	/**
	 * @return The root Node
	 */
	public Node getRoot(){
		TreeModel treeModel = getModel();

		Node root = treeModel.getNode();
		ensureNotNull(root);

		return root;
	}

	/**
	 * Adds a new Node to the root Node.
	 *
	 * @return The newly added Node
	 *
	 * @see #getRoot()
	 */
	public Node addNode(Predicate predicate){
		return addNode(getRoot(), predicate);
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
}