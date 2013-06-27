/*
 * Copyright (c) 2009 University of Tartu
 */
package org.jpmml.manager;

import java.util.*;

import org.dmg.pmml.*;

public class TreeModelManager extends ModelManager<TreeModel> implements EntityRegistry<Node> {

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

	public Map<String, Node> getEntities(){
		Map<String, Node> result = new LinkedHashMap<String, Node>();

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
	 * @see #getEntities()
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
	 * @see #getEntities()
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
	private void collectNodes(Node node, Map<String, Node> map){
		putEntity(node, map);

		List<Node> children = node.getNodes();
		for(Node child : children){
			collectNodes(child, map);
		}
	}
}