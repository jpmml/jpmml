package org.jpmml.evaluator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.dmg.pmml.DataField;
import org.dmg.pmml.FieldName;
import org.dmg.pmml.MiningModel;
import org.dmg.pmml.MultipleModelMethodType;
import org.dmg.pmml.PMML;
import org.dmg.pmml.Segment;
import org.jpmml.manager.IPMMLResult;
import org.jpmml.manager.MiningModelManager;
import org.jpmml.manager.ModelManager;
import org.jpmml.manager.PMMLResult;
import org.jpmml.manager.UnsupportedFeatureException;

public class MiningModelEvaluator extends MiningModelManager implements Evaluator {

	private HashMap<Segment, Integer> segmentToId = new HashMap<Segment, Integer>();
	private Integer segmentMaxId = 0;

	public MiningModelEvaluator(PMML pmml) {
		super(pmml);
	}

	public MiningModelEvaluator(PMML pmml, MiningModel miningModel) {
		super(pmml, miningModel);
	}

	public Object prepare(FieldName name, Object value){
		return ParameterUtil.prepare(getDataField(name), getMiningField(name), value);
	}

	public MiningModelEvaluator(MiningModelManager parent){
		this(parent.getPmml(), parent.getModel());
	}

	/** Work for vote. Each value is at least > 0.0. Return the key of the pair
	* that has the biggest value.
	*/
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

	/**
	 * Allow to get a unique id for each segment. It is useful
	 * because we can't rely on the id from the pmml that might contain a dot,
	 * or that can be anything.
	 *
	 * @param s The segment we want to identify.
	 * @return His id.
	 */
	private String getId(Segment s) {
		if (!segmentToId.containsKey(s)) {
			segmentToId.put(s, segmentMaxId++);
		}

		return "segmentNumber" + segmentToId.get(s);

	}

	// We can convert anything to an Object type. So the cast is legitimate.
	@SuppressWarnings(value = { "unchecked" })
	public IPMMLResult evaluate(Map<FieldName, ?> parameters) {
		try {
		switch (getFunctionType()) {
			case CLASSIFICATION:
				return evaluateClassification((Map<FieldName, Object>) parameters, getOutputField(this));
			case REGRESSION:
				return evaluateRegression((Map<FieldName, Object>) parameters, getOutputField(this));
			default:
				throw new UnsupportedOperationException();
		}
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * Get a double value from the object.
	 *
	 * @param obj An object representing a double. Must be a Double,
	 * or a String representing a double.
	 * @return The value of the object in Double.
	 * @throws EvaluationException If the value is not a double nor a string.
	 */
	private Double getDouble(Object obj) throws EvaluationException {
		Double tmpRes = null;
		if (obj instanceof String) {
			tmpRes = Double.parseDouble((String) obj);
		}
		else if (obj instanceof Double){
			tmpRes = (Double) obj;
		}
		else {
			throw new EvaluationException("Received type is neither a double nor a string.");
		}

		return tmpRes;
	}

	/**
	 * Run all the models, and store the results in results,
	 * the weight in idToWeight if we are interested in them,
	 * it augments parameters in case of modelChain,
	 * and return the main result.
	 *
	 * @param parameters The set of parameters for the evaluation.
	 * @param outputField The outputField where we will store the final result.
	 * @param results The set of results.
	 * @param idToWeight The weights. Useful for regression and weighted average for example.
	 * @return The main result if any (for example in select first).
	 * @throws Exception If there is a trouble with getting the name of the outputField
	 * of a model.
	 */
	private Object runModels(Map<FieldName, Object> parameters, DataField outputField,
			TreeMap<String, Object> results, TreeMap<String, Double> idToWeight) throws Exception {

		Object result = null;

		ModelEvaluatorFactory factory = new ModelEvaluatorFactory();

		for (Segment s : getSegments()) {
			EvaluationContext context = new ModelManagerEvaluationContext(this, parameters);

			if (PredicateUtil.evaluate(s.getPredicate(), context) == true) {
				Evaluator m = (Evaluator) factory.getModelManager(getPmml(), s.getModel());
				PMMLResult tmpObj = (PMMLResult) m.evaluate(parameters);

				if (getMultipleMethodModel() == MultipleModelMethodType.MODEL_CHAIN) {
					FieldName output = getOutputField((ModelManager<?>) m).getName();
					tmpObj.merge(parameters);
					// If this is the result we are interested in, put it in result.
					if (output.equals(outputField.getName())) {
						// This cast is legitimate because getModelManager returns a modelManager that is
						// also an evaluator.
						result = tmpObj.getValue(getOutputField((ModelManager<?>) m).getName());
					}
				}
				// If there is at least one result.
				if (tmpObj != null && !tmpObj.isEmpty()) {
					// Associate the main result to the name of the segment.
					// So we won't override the previous result at each new segment.

					// If there is one result, store it in the result list.
					Object tmpRes = tmpObj.getValue(getOutputField((ModelManager<?>) m).getName());
					if (tmpRes != null) {
						results.put(getId(s), tmpRes);

						idToWeight.put(getId(s), s.getWeight());
						// In this case, we are done with the evaluation of these model. We can quit.
						if (getMultipleMethodModel() == MultipleModelMethodType.SELECT_FIRST) {
							result = results.get(getId(s));
							break;
						}
					}
				}
			}
		}

		return result;
	}

	/**
	 * Evaluate the regression.
	 *
	 * @param parameters The set of parameters.
	 * @param outputField The output field.
	 * @return The result of the evaluation.
	 * @throws Exception If there is a trouble with getting the name of the outputField
	 * of a model.
	 */
	private IPMMLResult evaluateRegression(Map<FieldName, Object> parameters, DataField outputField) throws Exception {
		assert parameters != null;

		TreeMap<String, Object> results = new TreeMap<String, Object>();
		TreeMap<String, Double> idToWeight = new TreeMap<String, Double>();

		Object result = runModels(parameters, outputField, results, idToWeight);

		switch (getMultipleMethodModel()) {
		case SELECT_FIRST:
			// result already have the right value.
			break;
		case SELECT_ALL:
			throw new UnsupportedFeatureException();
		case MODEL_CHAIN:
			// This case is to be managed before.
			break;
		case AVERAGE:
			result = new Double(0.0);
			for (Map.Entry<String, Object> e : results.entrySet()) {
				result = (Double) result + getDouble(e.getValue());
			}
			if (results.size() != 0)
				result = (Double) result / results.size();
			break;
		case WEIGHTED_AVERAGE:
			Double sumWeight = 0.0;
			result = new Double(0.0);
			for (Map.Entry<String, Object> e : results.entrySet()) {
				result = (Double) result
						+ idToWeight.get(e.getKey())
						* getDouble(e.getValue());
				sumWeight += idToWeight.get(e.getKey());
			}
			if (sumWeight != 0.0)
				result = (Double) result / sumWeight;
			break;
		case MEDIAN:
			ArrayList<Double> list = new ArrayList<Double>(results.size());
			for (Map.Entry<String, Object> e : results.entrySet()) {
				list.add(getDouble(e.getValue()));
			}
			Collections.sort(list);
			result = list.get(list.size() / 2);
			break;
		default:
			throw new EvaluationException("The method " + getMultipleMethodModel().value()
					+ " is not compatible with the regression.");
		}


		PMMLResult res = new PMMLResult();
		try {
			res.put(getOutputField(this).getName(), result);
		} catch (Exception e) {
			throw new EvaluationException(e.getMessage());
		}

		return res;
	}


	/**
	 * Evaluate the classification.
	 *
	 * @param parameters The set of parameters.
	 * @param outputField The output field.
	 * @return The result of the evaluation.
	 * @throws Exception If there is a trouble with getting the name of the outputField
	 * of a model.
	 */
	private IPMMLResult evaluateClassification(Map<FieldName, Object> parameters, DataField outputField) throws Exception {
		assert parameters != null;

		TreeMap<String, Object> results = new TreeMap<String, Object>();
		TreeMap<String, Double> idToWeight = new TreeMap<String, Double>();

		Object result = runModels(parameters, outputField, results, idToWeight);


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

		PMMLResult res = new PMMLResult();
		try {
			res.put(getOutputField(this).getName(), result);
		} catch (Exception e) {
			throw new EvaluationException(e.getMessage());
		}

		return res;
	}

	public String getResultExplanation() {
		return "";
	}
}

