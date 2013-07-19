/*
 * Copyright (c) 2012 University of Tartu
 */
package org.jpmml.evaluator;

import java.util.*;

import org.jpmml.manager.*;

import org.dmg.pmml.*;

import com.google.common.collect.*;

public class NeuralNetworkEvaluator extends NeuralNetworkManager implements Evaluator {

	private BiMap<String, Entity> entities = null;


	public NeuralNetworkEvaluator(PMML pmml){
		super(pmml);
	}

	public NeuralNetworkEvaluator(PMML pmml, NeuralNetwork neuralNetwork){
		super(pmml, neuralNetwork);
	}

	public NeuralNetworkEvaluator(NeuralNetworkManager parent){
		this(parent.getPmml(), parent.getModel());
	}

	@Override
	public BiMap<String, Entity> getEntityRegistry(){

		if(this.entities == null){
			this.entities = super.getEntityRegistry();
		}

		return this.entities;
	}

	@Override
	public Object prepare(FieldName name, Object value){
		return ParameterUtil.prepare(getDataField(name), getMiningField(name), value);
	}

	@Override
	public Map<FieldName, ?> evaluate(Map<FieldName, ?> arguments) {
		NeuralNetwork neuralNetwork = getModel();
		if(!neuralNetwork.isScorable()){
			throw new InvalidResultException(neuralNetwork);
		}

		Map<FieldName, ?> predictions;

		ModelManagerEvaluationContext context = new ModelManagerEvaluationContext(this, arguments);

		MiningFunctionType miningFunction = neuralNetwork.getFunctionName();
		switch(miningFunction){
			case REGRESSION:
				predictions = evaluateRegression(context);
				break;
			case CLASSIFICATION:
				predictions = evaluateClassification(context);
				break;
			default:
				throw new UnsupportedFeatureException(neuralNetwork, miningFunction);
		}

		return OutputUtil.evaluate(predictions, context);
	}

	Map<FieldName, Double> evaluateRegression(EvaluationContext context) {
		Map<FieldName, Double> result = Maps.newLinkedHashMap();

		Map<String, Double> entityOutputs = evaluateRaw(context);

		List<NeuralOutput> neuralOutputs = getOrCreateNeuralOutputs();
		for (NeuralOutput neuralOutput : neuralOutputs) {
			String id = neuralOutput.getOutputNeuron();

			Expression expression = getExpression(neuralOutput.getDerivedField());
			if(expression instanceof FieldRef){
				FieldRef fieldRef = (FieldRef)expression;

				FieldName field = fieldRef.getField();

				Double value = entityOutputs.get(id);

				result.put(field, value);
			} else

			if(expression instanceof NormContinuous){
				NormContinuous normContinuous = (NormContinuous)expression;

				FieldName field = normContinuous.getField();

				Double value = NormalizationUtil.denormalize(normContinuous, entityOutputs.get(id));

				result.put(field, value);
			} else

			{
				throw new UnsupportedFeatureException(expression);
			}
		}

		return result;
	}

	Map<FieldName, NeuronClassificationMap> evaluateClassification(EvaluationContext context) {
		Map<FieldName, NeuronClassificationMap> result = Maps.newLinkedHashMap();

		Map<String, Entity> entities = getEntityRegistry();

		Map<String, Double> entityOutputs = evaluateRaw(context);

		List<NeuralOutput> neuralOutputs = getOrCreateNeuralOutputs();
		for (NeuralOutput neuralOutput : neuralOutputs) {
			String id = neuralOutput.getOutputNeuron();

			Expression expression = getExpression(neuralOutput.getDerivedField());
			if(expression instanceof NormDiscrete){
				NormDiscrete normDiscrete = (NormDiscrete)expression;

				FieldName field = normDiscrete.getField();

				NeuronClassificationMap values = result.get(field);
				if(values == null){
					values = new NeuronClassificationMap();

					result.put(field, values);
				}

				Entity entity = entities.get(id);

				Double value = entityOutputs.get(id);

				values.put(entity, normDiscrete.getValue(), value);
			} else

			{
				throw new UnsupportedFeatureException(expression);
			}
		}

		return result;
	}

	private Expression getExpression(DerivedField derivedField){
		Expression expression = derivedField.getExpression();

		if(expression instanceof FieldRef){
			FieldRef fieldRef = (FieldRef)expression;

			derivedField = resolveField(fieldRef.getField());
			if(derivedField != null){
				return getExpression(derivedField);
			}

			return fieldRef;
		}

		return expression;
	}

	/**
	 * Evaluates neural network.
	 *
	 * @return Mapping between Entity identifiers and their outputs
	 *
	 * @see NeuralInput#getId()
	 * @see Neuron#getId()
	 */
	public Map<String, Double> evaluateRaw(EvaluationContext context) {
		Map<String, Double> result = Maps.newLinkedHashMap();

		List<NeuralInput> neuralInputs = getNeuralInputs();
		for (NeuralInput neuralInput: neuralInputs) {
			DerivedField derivedField = neuralInput.getDerivedField();

			Double value = (Double)ExpressionUtil.evaluate(derivedField, context);
			if(value == null){
				throw new MissingFieldException(derivedField.getName(), derivedField);
			}

			result.put(neuralInput.getId(), value);
		}

		List<NeuralLayer> neuralLayers = getNeuralLayers();
		for (NeuralLayer neuralLayer : neuralLayers) {
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
		NeuralNetwork neuralNetwork = getModel();

		PMMLObject locatable = neuralLayer;

		NnNormalizationMethodType normalizationMethod = neuralLayer.getNormalizationMethod();
		if (normalizationMethod == null) {
			locatable = neuralNetwork;

			normalizationMethod = neuralNetwork.getNormalizationMethod();
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
			throw new UnsupportedFeatureException(locatable, normalizationMethod);
		}
	}

	private double activation(double z, NeuralLayer neuralLayer) {
		NeuralNetwork neuralNetwork = getModel();

		PMMLObject locatable = neuralLayer;

		ActivationFunctionType activationFunction = neuralLayer.getActivationFunction();
		if (activationFunction == null) {
			locatable = neuralLayer;

			activationFunction = neuralNetwork.getActivationFunction();
		}

		switch (activationFunction) {
			case THRESHOLD:
				Double threshold = neuralLayer.getThreshold();
				if (threshold == null) {
					threshold = Double.valueOf(neuralNetwork.getThreshold());
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
				throw new UnsupportedFeatureException(locatable, activationFunction);
		}
	}
}