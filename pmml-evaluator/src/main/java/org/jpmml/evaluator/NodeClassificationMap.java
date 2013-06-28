/*
 * Copyright (c) 2013 University of Tartu
 */
package org.jpmml.evaluator;

import org.dmg.pmml.*;

class NodeClassificationMap extends EntityClassificationMap<Node> {

	NodeClassificationMap(Node node){
		super(node);
	}

	@Override
	public String getResult(){
		Node node = getEntity();

		String score = node.getScore();
		if(score != null){
			return score;
		}

		return super.getResult();
	}
}