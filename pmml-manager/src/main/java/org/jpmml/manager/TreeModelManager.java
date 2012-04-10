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

	public TreeModel createClassificationModel(){
		return createModel(MiningFunctionType.CLASSIFICATION);
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
}