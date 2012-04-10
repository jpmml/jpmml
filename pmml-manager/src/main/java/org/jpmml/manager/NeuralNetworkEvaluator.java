/*
 * Copyright (c) 2012 University of Tartu
 */
package org.jpmml.manager;

import java.util.*;

import org.dmg.pmml.*;

public class NeuralNetworkEvaluator extends NeuralNetworkManager implements Evaluator {

	public NeuralNetworkEvaluator(PMML pmml){
		super(pmml);
	}

	public NeuralNetworkEvaluator(PMML pmml, NeuralNetwork neuralNetwork){
		super(pmml, neuralNetwork);
	}

	public NeuralNetworkEvaluator(NeuralNetworkManager parent){
		this(parent.getPmml(), parent.getModel());
	}

	/**
	 * @see #evaluateRegression(Map)
	 * @see #evaluateClassification(Map)
	 */
	public Map<FieldName, ?> evaluate(Map<FieldName, ?> parameters) {
		NeuralNetwork neuralNetwork = getModel();

		MiningFunctionType miningFunction = neuralNetwork.getFunctionName();
		switch(miningFunction){
			case REGRESSION:
				return evaluateRegression(parameters);
			case CLASSIFICATION:
				return evaluateClassification(parameters);
			default:
				throw new UnsupportedFeatureException(miningFunction);
		}
	}

	public Map<FieldName, Double> evaluateRegression(Map<FieldName, ?> parameters) {
		Map<FieldName, Double> result = new HashMap<FieldName, Double>();

		Map<String, Double> neuronOutputs = evaluateRaw(parameters);

		List<NeuralOutput> neuralOutputs = getOrCreateNeuralOutputs();
		for (NeuralOutput neuralOutput : neuralOutputs) {
			String id = neuralOutput.getOutputNeuron();

			Expression expression = (neuralOutput.getDerivedField()).getExpression();
			if (expression instanceof NormContinuous) {
				NormContinuous normContinuous = (NormContinuous)expression;

				FieldName field = normContinuous.getField();
				Double value = NormalizationUtil.denormalize(normContinuous, neuronOutputs.get(id));

				result.put(field, value);
			} else

			{
				throw new UnsupportedFeatureException(expression);
			}
		}

		return result;
	}

	public Map<FieldName, Map<String, Double>> evaluateClassification(Map<FieldName, ?> parameters) {
		Map<FieldName, Map<String,Double>> result = new HashMap<FieldName, Map<String, Double>>();

		Map<String, Double> neuronOutputs = evaluateRaw(parameters);

		List<NeuralOutput> neuralOutputs = getOrCreateNeuralOutputs();
		for (NeuralOutput neuralOutput: neuralOutputs) {
			String id = neuralOutput.getOutputNeuron();

			Expression expression = (neuralOutput.getDerivedField()).getExpression();
			if (expression instanceof NormDiscrete) {
				NormDiscrete normDiscrete = (NormDiscrete)expression;

				FieldName field = normDiscrete.getField();

				Map<String, Double> valuesMap = result.get(field);
				if(valuesMap == null){
					valuesMap = new HashMap<String, Double>();

					result.put(field, valuesMap);
				}

				Double value = neuronOutputs.get(id);

				valuesMap.put(normDiscrete.getValue(), value);
			} else

			{
				throw new UnsupportedFeatureException(expression);
			}
		}

		return result;
	}

	/**
	 * Evaluate neural network.
	 *
	 * @param parameters Mapping between input data fields and their values
	 *
	 * @return Mapping between Neuron ids and their outputs
	 */
	public Map<String, Double> evaluateRaw(Map<FieldName, ?> parameters) {
		Map<String, Double> result = new HashMap<String, Double>();

		List<NeuralInput> neuralInputs = getNeuralInputs();
		for (NeuralInput neuralInput: neuralInputs) {
			Double value = evaluateDerivedField(neuralInput.getDerivedField(), parameters);

			result.put(neuralInput.getId(), value);
		}

		List<NeuralLayer> neuralLayers = getNeuralLayers();
		for (NeuralLayer neuralLayer: neuralLayers) {
			List<Neuron> neurons = neuralLayer.getNeurons();

			for (Neuron neuron : neurons) {
				double z = neuron.getBias();

				List<Connection> connections = neuron.getConnections();
				for (Connection connection : connections) {
					double input = result.get(connection.getFrom());

					z += input * connection.getWeight();
				}

				double output = activation(z, neuralLayer);

				result.put(neuron.getId(), output);
			}

			normalizeNeuronOutputs(neuralLayer, result);
		}

		return result;
	}

	private void normalizeNeuronOutputs(NeuralLayer neuralLayer, Map<String, Double> neuronOutputs) {
		NeuralNetwork model = getModel();

		NnNormalizationMethodType normalizationMethod = neuralLayer.getNormalizationMethod();
		if (normalizationMethod == null) {
			normalizationMethod = model.getNormalizationMethod();
		} // End if

		if (normalizationMethod == NnNormalizationMethodType.NONE) {
			return;
		} else

		if (normalizationMethod == NnNormalizationMethodType.SOFTMAX) {
			List<Neuron> neurons = neuralLayer.getNeurons();

			double sum = 0.0;

			for (Neuron neuron : neurons) {
				double output = neuronOutputs.get(neuron.getId());

				sum += Math.exp(output);
			}

			for (Neuron neuron : neurons) {
				double output = neuronOutputs.get(neuron.getId());

				neuronOutputs.put(neuron.getId(), Math.exp(output) / sum);
			}
		} else

		{
			throw new UnsupportedFeatureException(normalizationMethod);
		}
	}

	private double evaluateDerivedField(DerivedField derivedField, Map<FieldName,?> parameters) {

		if (!(derivedField.getDataType()).equals(DataType.DOUBLE) && !(derivedField.getOptype()).equals(OpType.CONTINUOUS)) {
			throw new UnsupportedFeatureException(derivedField);
		}

		return evaluateExpression(derivedField.getExpression(), parameters);
	}

	private double evaluateExpression(Expression expression, Map<FieldName, ?> parameters){

		if (expression instanceof FieldRef) {
			FieldRef fieldRef = (FieldRef)expression;

			FieldName field = fieldRef.getField();

			// check refs to derived fields in local and global transformation dictionaries
			List<DerivedField> derivedFields = new ArrayList<DerivedField>();

			LocalTransformations localTransformations = getModel().getLocalTransformations();
			if (localTransformations != null) {
				derivedFields.addAll(localTransformations.getDerivedFields());
			}

			TransformationDictionary transformationDictionary = getPmml().getTransformationDictionary();
			if (transformationDictionary != null) {
				derivedFields.addAll(transformationDictionary.getDerivedFields());
			}

			for (DerivedField derivedField : derivedFields) {

				if ((derivedField.getName()).equals(field)) {
					return evaluateDerivedField(derivedField, parameters);
				}
			}

			// refs to a data field in the data dictionary
			List<DataField> dataFields = getDataDictionary().getDataFields();

			for (DataField dataField : dataFields) {

				if ((dataField.getName()).equals(field)) {
					Number value = (Number)ParameterUtil.getValue(parameters, field);

					return value.doubleValue();
				}
			}

			throw new EvaluationException();
		} else

		if (expression instanceof NormContinuous) {
			NormContinuous normContinuous = (NormContinuous)expression;

			FieldName field = normContinuous.getField();
			Number value = (Number)ParameterUtil.getValue(parameters, field);

			return NormalizationUtil.normalize(normContinuous, value.doubleValue());
		} else

		if (expression instanceof NormDiscrete) {
			NormDiscrete normDiscrete = (NormDiscrete)expression;

			FieldName field = normDiscrete.getField();
			Object value = ParameterUtil.getValue(parameters, field);

			return (normDiscrete.getValue()).equals(value) ? 1.0 : 0.0;
		} else

		{
			throw new UnsupportedFeatureException(expression);
		}
	}

	private double activation(double z, NeuralLayer layer) {
		NeuralNetwork model = getModel();

		ActivationFunctionType activationFunction = layer.getActivationFunction();
		if (activationFunction == null) {
			activationFunction = model.getActivationFunction();
		}

		switch (activationFunction) {
			case THRESHOLD:
				Double threshold = layer.getThreshold();
				if (threshold == null) {
					threshold = Double.valueOf(model.getThreshold());
				}
				return z > threshold.doubleValue() ? 1.0 : 0.0;
			case LOGISTIC:
				return 1.0 / (1.0 + Math.exp(-z));
			case TANH:
				return (1.0 - Math.exp(-2.0*z)) / (1.0 + Math.exp(-2.0*z));
			case IDENTITY:
				return z;
			case EXPONENTIAL:
				return Math.exp(z);
			case RECIPROCAL:
				return 1.0/z;
			case SQUARE:
				return z*z;
			case GAUSS:
				return Math.exp(-(z*z));
			case SINE:
				return Math.sin(z);
			case COSINE:
				return Math.cos(z);
			case ELLIOTT:
				return z/(1.0 + Math.abs(z));
			case ARCTAN:
				return Math.atan(z);
			default:
				throw new UnsupportedFeatureException(activationFunction);
		}
	}
}