/*
 * Copyright (c) 2013 University of Tartu
 */
package org.jpmml.evaluator;

import org.dmg.pmml.*;

import com.google.common.annotations.*;

@Beta
public class RuleClassificationMap extends EntityClassificationMap<SimpleRule> implements HasConfidence {

	protected RuleClassificationMap(){
		super(Type.CONFIDENCE);
	}

	protected RuleClassificationMap(SimpleRule rule){
		super(Type.CONFIDENCE, rule);
	}

	@Override
	public String getResult(){
		SimpleRule rule = getEntity();

		if(rule != null){
			return rule.getScore();
		}

		return super.getResult();
	}

	@Override
	public Double getConfidence(String value){
		return getFeature(value);
	}
}