/*
 * Copyright (c) 2013 University of Tartu
 */
package org.jpmml.evaluator;

import org.dmg.pmml.*;

class RuleClassificationMap extends EntityClassificationMap<SimpleRule> {

	RuleClassificationMap(){
		super(Type.CONFIDENCE);
	}

	RuleClassificationMap(SimpleRule rule){
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
}