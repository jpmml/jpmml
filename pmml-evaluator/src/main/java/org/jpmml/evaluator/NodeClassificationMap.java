/*
 * Copyright (c) 2013 University of Tartu
 */
package org.jpmml.evaluator;

import org.dmg.pmml.*;

import com.google.common.annotations.*;

@Beta
public class NodeClassificationMap extends EntityClassificationMap<Node> {

	protected NodeClassificationMap(){
		super(Type.PROBABILITY);
	}

	protected NodeClassificationMap(Node node){
		super(Type.PROBABILITY, node);
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