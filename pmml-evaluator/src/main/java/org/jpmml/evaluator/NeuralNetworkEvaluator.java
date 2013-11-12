/*
 * Copyright (c) 2012 University of Tartu
 */
package org.jpmml.evaluator;

import java.util.*;

import org.jpmml.manager.*;

import org.dmg.pmml.*;

import com.google.common.cache.*;
import com.google.common.collect.*;

public class NeuralNetworkEvaluator extends ModelEvaluator<NeuralNetwork> implements HasEntityRegistry<Entity> {

	public NeuralNetworkEvaluator(PMML pmml){
		this(pmml, find(pmml.getModels(), NeuralNetwork.class));
	}

	public NeuralNetworkEvaluator(PMML pmml, NeuralNetwork neuralNetwork){
		super(pmml, neuralNetwork);
	}

	@Override
	public String getSummary(){
		return "Neural network";
	}

	@Override
	public BiMap<String, Entity> getEntityRegistry(){
		return getValue(NeuralNetworkEvaluator.entityCache);
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

	private Map<FieldName, ? extends Number> evaluateRegression(ModelManagerEvaluationContext context) {
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

		return TargetUtil.evaluateRegression(result, context);
	}

	private Map<FieldName, ? extends ClassificationMap<?>> evaluateClassification(ModelManagerEvaluationContext context) {
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

		return TargetUtil.evaluateClassification(result, context);
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
	public Map<String, Double> evaluateRaw(EvaluationContext context){
		NeuralNetwork neuralNetwork = getModel();

		Map<String, Double> result = Maps.newLinkedHashMap();

		NeuralInputs neuralInputs = neuralNetwork.getNeuralInputs();
		for (NeuralInput neuralInput: neuralInputs) {
			DerivedField derivedField = neuralInput.getDerivedField();

			FieldValue value = ExpressionUtil.evaluate(derivedField, context);
			if(value == null){
				throw new MissingFieldException(derivedField.getName(), derivedField);
			}

			result.put(neuralInput.getId(), (value.asNumber()).doubleValue());
		}

		List<NeuralLayer> neuralLayers = neuralNetwork.getNeuralLayers();
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
		}

		switch(normalizationMethod){
			case NONE:
				break;
			case SIMPLEMAX:
				normalizeNeuronOutputs(neuralLayer, SIMPLEMAX_NORMALIZER, neuronOutputs);
				break;
			case SOFTMAX:
				normalizeNeuronOutputs(neuralLayer, SOFTMAX_NORMALIZER, neuronOutputs);
				break;
			default:
				throw new UnsupportedFeatureException(locatable, normalizationMethod);
		}
	}

	private void normalizeNeuronOutputs(NeuralLayer neuralLayer, Normalizer normalizer, Map<String, Double> neuronOutputs){
		List<Neuron> neurons = neuralLayer.getNeurons();

		double sum = 0;

		for(Neuron neuron : neurons){
			Double output = neuronOutputs.get(neuron.getId());

			sum += normalizer.apply(output.doubleValue());
		}

		for(Neuron neuron : neurons){
			Double output = neuronOutputs.get(neuron.getId());

			Double normalizedOutput = normalizer.apply(output.doubleValue()) / sum;

			neuronOutputs.put(neuron.getId(), normalizedOutput);
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

	public List<NeuralOutput> getOrCreateNeuralOutputs() {
		NeuralNetwork neuralNetwork = getModel();

		NeuralOutputs neuralOutputs = neuralNetwork.getNeuralOutputs();
		if(neuralOutputs == null){
			neuralOutputs = new NeuralOutputs();

			neuralNetwork.setNeuralOutputs(neuralOutputs);
		}

		return neuralOutputs.getNeuralOutputs();
	}

	private interface Normalizer {

		double apply(double value);
	}

	private static final Normalizer SIMPLEMAX_NORMALIZER = new Normalizer(){

		@Override
		public double apply(double value){
			return value;
		}
	};

	private static final Normalizer SOFTMAX_NORMALIZER = new Normalizer(){

		@Override
		public double apply(double value){
			return Math.exp(value);
		}
	};

	private static final LoadingCache<NeuralNetwork, BiMap<String, Entity>> entityCache = CacheBuilder.newBuilder()
		.weakKeys()
		.build(new CacheLoader<NeuralNetwork, BiMap<String, Entity>>(){

			@Override
			public BiMap<String, Entity> load(NeuralNetwork neuralNetwork){
				BiMap<String, Entity> result = HashBiMap.create();

				NeuralInputs neuralInputs = neuralNetwork.getNeuralInputs();
				for(NeuralInput neuralInput : neuralInputs){
					EntityUtil.put(neuralInput, result);
				}

				List<NeuralLayer> neuralLayers = neuralNetwork.getNeuralLayers();
				for(NeuralLayer neuralLayer : neuralLayers){
					List<Neuron> neurons = neuralLayer.getNeurons();

					for(Neuron neuron : neurons){
						EntityUtil.put(neuron, result);
					}
				}

				return result;
			}
		});
}