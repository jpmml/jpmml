/*
 * Copyright (c) 2011 University of Tartu
 */
package org.jpmml.manager;

import java.util.*;

import org.dmg.pmml.*;

public class NeuralNetworkManager extends ModelManager<NeuralNetwork>  {

	private NeuralNetwork neuralNetwork = null;

	private int neuronCount = 0;


	public NeuralNetworkManager() {
	}

	public NeuralNetworkManager(PMML pmml) {
		this(pmml, find(pmml.getContent(), NeuralNetwork.class));
	}

	public NeuralNetworkManager(PMML pmml, NeuralNetwork neuralNetwork) {
		super(pmml);

		this.neuralNetwork = neuralNetwork;

		if(this.neuralNetwork != null){
			this.neuronCount = getNeuronCount();
		}
	}

	@Override
	public NeuralNetwork getModel() {
		ensureNotNull(this.neuralNetwork);

		return this.neuralNetwork;
	}

	/**
	 * @throws ModelManagerException If the Model already exists
	 *
	 * @see #getModel()
	 */
	public NeuralNetwork createModel(MiningFunctionType miningFunction, ActivationFunctionType activationFunction) {
		ensureNull(this.neuralNetwork);

		this.neuralNetwork = new NeuralNetwork(new MiningSchema(), new NeuralInputs(), miningFunction, activationFunction);

		List<Model> content = getPmml().getContent();
		content.add(this.neuralNetwork);

		return this.neuralNetwork;
	}

	public List<NeuralInput> getNeuralInputs() {
		NeuralNetwork neuralNetwork = getModel();

		return neuralNetwork.getNeuralInputs().getNeuralInputs();
	}

	public NeuralInput addNeuralInput(NormContinuous normContinuous) {
		DerivedField derivedField = new DerivedField(OpTypeType.CONTINUOUS, DataTypeType.DOUBLE);
		derivedField.setExpression(normContinuous);

		NeuralInput neuralInput = new NeuralInput(derivedField, nextId());

		getNeuralInputs().add(neuralInput);

		return neuralInput;
	}

	public List<NeuralLayer> getNeuralLayers(){
		NeuralNetwork neuralNetwork = getModel();

		return neuralNetwork.getNeuralLayers();
	}

	public NeuralLayer addNeuralLayer() {
		NeuralLayer neuralLayer = new NeuralLayer();

		getNeuralLayers().add(neuralLayer);

		return neuralLayer;
	}

	public int getNeuronCount(){
		int count = 0;

		count += (getNeuralInputs()).size();

		List<NeuralLayer> neuralLayers = getNeuralLayers();
		for(NeuralLayer neuralLayer : neuralLayers){
			count += (neuralLayer.getNeurons()).size();
		}

		return count;
	}

	public Neuron addNeuron(NeuralLayer neuralLayer, Double bias) {
		Neuron neuron = new Neuron(nextId());
		neuron.setBias(bias);

		neuralLayer.getNeurons().add(neuron);

		return neuron;
	}

	static
	public void addConnection(NeuralInput from, Neuron to, double weight) {
		Connection connection = new Connection(from.getId(), weight);

		(to.getConnections()).add(connection);
	}

	static
	public void addConnection(Neuron from, Neuron to, double weight) {
		Connection connection = new Connection(from.getId(), weight);

		(to.getConnections()).add(connection);
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

	public NeuralOutput addNeuralOutput(Neuron neuron, NormContinuous normCountinuous) {
		DerivedField derivedField = new DerivedField(OpTypeType.CONTINUOUS, DataTypeType.DOUBLE);
		derivedField.setExpression(normCountinuous);

		NeuralOutput output = new NeuralOutput(derivedField, neuron.getId());

		getOrCreateNeuralOutputs().add(output);

		return output;
	}

	private String nextId(){
		return String.valueOf(this.neuronCount++);
	}

	/**
	 * @see #evaluateRegression(Map)
	 * @see #evaluateClassification(Map)
	 */
	@Override
	public Map<FieldName, ?> evaluate(Map<FieldName, ?> parameters) {
		NeuralNetwork neuralNetwork = getModel();

		MiningFunctionType miningFunction = neuralNetwork.getFunctionName();
		switch(miningFunction){
			case REGRESSION:
				return evaluateRegression(parameters);
			case CLASSIFICATION:
				return evaluateClassification(parameters);
			default:
				break;
		}

		throw new EvaluationException();
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
				throw new EvaluationException();
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
				throw new EvaluationException();
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
			throw new UnsupportedOperationException("Unsupported normalization method: " + normalizationMethod);
		}
	}

	private double evaluateDerivedField(DerivedField derivedField, Map<FieldName,?> parameters) {

		if (!(derivedField.getDataType()).equals(DataTypeType.DOUBLE) && !(derivedField.getOptype()).equals(OpTypeType.CONTINUOUS)) {
			throw new UnsupportedOperationException();
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
					return ((Number) parameters.get(field)).doubleValue();
				}
			}

			throw new EvaluationException("Can't handle FieldRef: " + field.getValue());
		} else

		if (expression instanceof NormContinuous) {
			NormContinuous normContinuous = (NormContinuous)expression;

			FieldName field = normContinuous.getField();
			double v = NormalizationUtil.normalize(normContinuous, (Number) parameters.get(field));
			return v;
		} else

		if (expression instanceof NormDiscrete) {
			NormDiscrete normDiscrete = (NormDiscrete)expression;

			FieldName field = normDiscrete.getField();
			Object value = parameters.get(field);
			return normDiscrete.getValue().equals(value) ? 1.0 : 0.0;
		}

		throw new EvaluationException("Can't evaluate DerivedField");
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
				throw new EvaluationException("Unsupported activation function: " + activationFunction);
		}
	}
}
