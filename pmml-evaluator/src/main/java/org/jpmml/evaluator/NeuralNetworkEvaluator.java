/*
 * Copyright (c) 2012 University of Tartu
 */
package org.jpmml.evaluator;

import java.util.*;

import org.jpmml.manager.*;

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
	 * @see #evaluateRegression(EvaluationContext)
	 * @see #evaluateClassification(EvaluationContext)
	 */
	public Map<FieldName, ?> evaluate(Map<FieldName, ?> parameters) {
		NeuralNetwork neuralNetwork = getModel();

		MiningFunctionType miningFunction = neuralNetwork.getFunctionName();
		switch(miningFunction){
			case REGRESSION:
				return evaluateRegression(new EvaluationContext<NeuralNetwork>(this, parameters));
			case CLASSIFICATION:
				return evaluateClassification(new EvaluationContext<NeuralNetwork>(this, parameters));
			default:
				throw new UnsupportedFeatureException(miningFunction);
		}
	}

	public Map<FieldName, Double> evaluateRegression(EvaluationContext<NeuralNetwork> context) {
		Map<FieldName, Double> result = new HashMap<FieldName, Double>();

		Map<String, Double> neuronOutputs = evaluateRaw(context);

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

	public Map<FieldName, Map<String, Double>> evaluateClassification(EvaluationContext<NeuralNetwork> context) {
		Map<FieldName, Map<String,Double>> result = new HashMap<FieldName, Map<String, Double>>();

		Map<String, Double> neuronOutputs = evaluateRaw(context);

		List<NeuralOutput> neuralOutputs = getOrCreateNeuralOutputs();
		for (NeuralOutput neuralOutput: neuralOutputs) {
			String id = neuralOutput.getOutputNeuron();

			Expression expression = (neuralOutput.getDerivedField()).getExpression();
			if (expression instanceof FieldRef) {
				FieldRef fieldRef = (FieldRef)expression;
				DerivedField derivedField = resolve(fieldRef.getField());
				expression = derivedField.getExpression();
			} // End if

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
	public Map<String, Double> evaluateRaw(EvaluationContext<NeuralNetwork> context) {
		Map<String, Double> result = new HashMap<String, Double>();

		List<NeuralInput> neuralInputs = getNeuralInputs();
		for (NeuralInput neuralInput: neuralInputs) {
			Double value = (Double)ExpressionUtil.evaluate(neuralInput.getDerivedField(), context);
			if(value == null){
				throw new MissingParameterException(neuralInput.getDerivedField());
			}

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