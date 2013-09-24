/*
 * Copyright (c) 2009 University of Tartu
 */
package org.jpmml.manager;

import java.util.*;

import org.dmg.pmml.*;

import com.google.common.collect.*;

import static com.google.common.base.Preconditions.*;

public class TreeModelManager extends ModelManager<TreeModel> implements HasEntityRegistry<Node> {

	private TreeModel treeModel = null;


	public TreeModelManager(){
	}

	public TreeModelManager(PMML pmml){
		this(pmml, find(pmml.getModels(), TreeModel.class));
	}

	public TreeModelManager(PMML pmml, TreeModel treeModel){
		super(pmml);

		this.treeModel = treeModel;
	}

	@Override
	public String getSummary(){
		return "Tree model";
	}

	@Override
	public TreeModel getModel(){
		checkState(this.treeModel != null);

		return this.treeModel;
	}

	/**
	 * @see #getModel()
	 */
	public TreeModel createModel(MiningFunctionType miningFunction){
		checkState(this.treeModel == null);

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

		return treeModel.getNode();
	}

	@Override
	public BiMap<String, Node> getEntityRegistry(){
		BiMap<String, Node> result = HashBiMap.create();

		collectNodes(getRoot(), result);

		return result;
	}

	/**
	 * Adds a new Node to the root Node.
	 *
	 * @param id Unique identifier
	 *
	 * @return The newly added Node
	 *
	 * @see #getEntityRegistry()
	 */
	public Node addNode(String id, Predicate predicate){
		return addNode(getRoot(), id, predicate);
	}

	/**
	 * Adds a new Node to the specified Node.
	 *
	 * @param id Unique identifier
	 *
	 * @return The newly added Node
	 *
	 * @see #getEntityRegistry()
	 */
	public Node addNode(Node parentNode, String id, Predicate predicate){
		Node node = new Node();
		node.setId(id);
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

	static
	private void collectNodes(Node node, BiMap<String, Node> map){
		EntityUtil.put(node, map);

		List<Node> children = node.getNodes();
		for(Node child : children){
			collectNodes(child, map);
		}
	}
}