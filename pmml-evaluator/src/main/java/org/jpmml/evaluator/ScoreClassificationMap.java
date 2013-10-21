/*
 * Copyright, KNIME.com AG, Zurich, Switzerland
 */
package org.jpmml.evaluator;

import java.util.*;

public class ScoreClassificationMap extends ClassificationMap<String> implements HasReasonCodeRanking {

	private Number result = null;


	ScoreClassificationMap(Number result){
		super(Type.VOTE);

		setResult(result);
	}

	@Override
	public Number getResult(){
		return this.result;
	}

	private void setResult(Number result){
		this.result = result;
	}

	@Override
	public List<String> getReasonCodeRanking(){
		Type type = getType();

		if(!(Type.VOTE).equals(type)){
			throw new EvaluationException();
		}

		return getWinnerKeys();
	}
}