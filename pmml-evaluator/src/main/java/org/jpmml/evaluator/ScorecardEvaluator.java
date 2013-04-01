package org.jpmml.evaluator;

import java.util.List;
import java.util.Map;

import org.dmg.pmml.Attribute;
import org.dmg.pmml.Characteristic;
import org.dmg.pmml.FieldName;
import org.dmg.pmml.PMML;
import org.dmg.pmml.Scorecard;
import org.jpmml.manager.ScoreCardModelManager;

public class ScorecardEvaluator extends ScoreCardModelManager implements Evaluator {

	
	public ScorecardEvaluator(PMML pmml) {
		super(pmml);
	}
	
	public ScorecardEvaluator(PMML pmml, Scorecard scorecard) {
		super(pmml, scorecard);
	}
	
	public ScorecardEvaluator(ScoreCardModelManager parent){
		this(parent.getPmml(), parent.getModel());
	}

	// Evaluate the parameters on the score card.
	public Object evaluate(Map<FieldName, ?> parameters) {
		Double score = 0.0;
		
		List<Characteristic> cl
			= scorecard.getCharacteristics().getCharacteristics();
		for (Characteristic c : cl) {
			List<Attribute> al = c.getAttributes(); 
			for (Attribute a : al) {
				// Evaluate the predicate.
				Boolean predicateValue = PredicateUtil
										.evaluatePredicate(a.getPredicate(),
															parameters);
				// If it is valid, and the value is true, update the score.
				if (predicateValue != null && predicateValue.booleanValue()) {
					score += a.getPartialScore();
					break;
				// FIXME: Add a missing value strategy.
				}
			}
		}

    	return score;
	}
}
