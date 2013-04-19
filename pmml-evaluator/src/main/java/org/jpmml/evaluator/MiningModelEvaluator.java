package org.jpmml.evaluator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

import org.dmg.pmml.FieldName;
import org.dmg.pmml.MiningModel;
import org.dmg.pmml.MultipleModelMethodType;
import org.dmg.pmml.PMML;
import org.dmg.pmml.Segment;
import org.jpmml.manager.MiningModelManager;
import org.jpmml.manager.UnsupportedFeatureException;

public class MiningModelEvaluator extends MiningModelManager implements Evaluator {
	public MiningModelEvaluator(PMML pmml) {
		super(pmml);
	}

	public MiningModelEvaluator(PMML pmml, MiningModel miningModel) {
		super(pmml, miningModel);
	}

	public MiningModelEvaluator(MiningModelManager parent){
		this(parent.getPmml(), parent.getModel());
	}

	// Work for vote. Each value is at least > 0.0. Return the key of the pair
	// that has the biggest value.
	private Object getBetterKey(Map<?, Double> map) {
		Double max = 0.0;
		Object result = null;
		for (Map.Entry<?, Double> e : map.entrySet()) {
			if (e.getValue() > max) {
				max = e.getValue();
				result = e.getKey();
			}
		}

		return result;
	}

	// Evaluate the parameters on the score card.
	public Object evaluate(Map<FieldName, ?> parameters) {
		switch (getFunctionType()) {
			case CLASSIFICATION:
				return evaluateClassification(parameters);
			case REGRESSION:
				return evaluateRegression(parameters);
			default:
				throw new UnsupportedOperationException();
		}
	}

	private Object evaluateRegression(Map<FieldName, ?> parameters) {
		Object result = null;

		TreeMap<String, Object> results = new TreeMap<String, Object>();
		TreeMap<String, Double> idToWeight = new TreeMap<String, Double>();

		ModelEvaluatorFactory factory = new ModelEvaluatorFactory();

		for (Segment s : getSegment()) {
			if (PredicateUtil.evaluatePredicate(s.getPredicate(), parameters)) {
				Evaluator m = (Evaluator) factory.getModelManager(getPmml(), s.getModel());
				results.put(s.getId(), m.evaluate(parameters));
				idToWeight.put(s.getId(), s.getWeight());
				if (getMultipleMethodModel() == MultipleModelMethodType.SELECT_FIRST) {
					result = results.get(s.getId());
					break;
				}
			}
		}

		switch (getMultipleMethodModel()) {
		case SELECT_FIRST:
			// result already have the right value.
			break;
		case SELECT_ALL:
			// FIXME: I don't know what I should do here.
			break;
		case MODEL_CHAIN:
			// This case is to be managed before.
			break;
		case AVERAGE:
			result = new Double(0.0);
			for (Map.Entry<String, Object> e : results.entrySet()) {
				result = (Double) result + (Double) e.getValue();
			}
			result = (Double) result / results.size();
			break;
		case WEIGHTED_AVERAGE:
			// FIXME: Check that this is correct.
			Double sumWeight = 0.0;
			result = new Double(0.0);
			for (Map.Entry<String, Object> e : results.entrySet()) {
				result = (Double) result + idToWeight.get(e.getKey()) * (Double) e.getValue();
				sumWeight += idToWeight.get(e.getKey());
			}
			result = (Double) result / sumWeight;
			break;
		case MEDIAN:
			ArrayList<Double> list = new ArrayList<Double>(results.size());
			for (Map.Entry<String, Object> e : results.entrySet()) {
				list.add((Double)e.getValue());
			}
			Collections.sort(list);
			result = list.get(list.size() / 2);
			break;
		default:
			throw new EvaluationException("The method " + getMultipleMethodModel().value()
					+ " is not compatible with the regression.");
		}

		return result;
	}

	private Object evaluateClassification(Map<FieldName, ?> parameters) {
		Object result = null;

		TreeMap<String, Object> results = new TreeMap<String, Object>();
		TreeMap<String, Double> idToWeight = new TreeMap<String, Double>();

		ModelEvaluatorFactory factory = new ModelEvaluatorFactory();

		for (Segment s : getSegment()) {
			if (PredicateUtil.evaluatePredicate(s.getPredicate(), parameters)) {
				Evaluator m = (Evaluator) factory.getModelManager(getPmml(), s.getModel());
				Object tmpRes = m.evaluate(parameters);
				if (tmpRes != null) {
					results.put(s.getId(), tmpRes);
					idToWeight.put(s.getId(), s.getWeight());
				}
				if (getMultipleMethodModel() == MultipleModelMethodType.SELECT_FIRST) {
					result = results.get(s.getId());
					break;
				}
			}
		}

		switch (getMultipleMethodModel()) {
		case SELECT_FIRST:
			// result already have the right value.
			break;
		case MODEL_CHAIN:
			// This case is to be managed before.
			throw new UnsupportedFeatureException("Missing implementation.");
		case MAJORITY_VOTE:
			TreeMap<Object, Double> vote = new TreeMap<Object, Double>();
			for (Map.Entry<String, Object> e : results.entrySet()) {
				if (vote.containsKey(e.getValue())) {
					// We increment our number of vote.
					vote.put(e.getValue(), vote.get(e.getValue()) + 1.0);
				}
				else {
					vote.put(e.getValue(), 1.0);
				}
			}
			result = getBetterKey(vote);
			break;
		case WEIGHTED_MAJORITY_VOTE:
			TreeMap<Object, Double> vote2 = new TreeMap<Object, Double>();
			for (Map.Entry<String, Object> e : results.entrySet()) {
				if (vote2.containsKey(e.getValue())) {
					// We increment our counter wit the weight of the segment.
					vote2.put(e.getValue(), vote2.get(e.getValue())
							+ idToWeight.get(e.getKey()));
				}
				else {
					vote2.put(e.getValue(), idToWeight.get(e.getKey()));
				}
			}
			result = getBetterKey(vote2);
			break;
		case AVERAGE:
		case WEIGHTED_AVERAGE:
		case MEDIAN:
		case MAX:
			throw new UnsupportedFeatureException("Missing implementation.");
		default:
			throw new EvaluationException("The method " + getMultipleMethodModel().value()
					+ " is not compatible with the regression.");
		}

		return result;
	}

	public String getResultExplanation() {
		return "";
	}
}

