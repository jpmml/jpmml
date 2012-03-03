/*
 * Copyright (c) 2011 University of Tartu
 */
package org.jpmml.manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dmg.pmml.*;
import org.jpmml.manager.EvaluationException;
import org.jpmml.manager.ModelManager;

public class NeuralNetworkManager extends ModelManager<NeuralNetwork>  {

	private NeuralNetwork model;

	private int neuronCount = 0;


	public NeuralNetworkManager() {
	}

	public NeuralNetworkManager(PMML pmml) {
		this(pmml, find(pmml.getContent(), NeuralNetwork.class));
	}

	public NeuralNetworkManager(PMML pmml, NeuralNetwork model) {
		super(pmml);

		this.model = model;
	}

	@Override
	public NeuralNetwork getModel() {
		ensureNotNull(this.model);

		return this.model;
	}

	/**
	 * @throws ModelManagerException If the Model already exists
	 *
	 * @see #getModel()
	 */
	public NeuralNetwork createModel(MiningFunctionType miningFunction, ActivationFunctionType activationFunction) {
		ensureNull(this.model);

		this.model = new NeuralNetwork(new MiningSchema(), new NeuralInputs(), miningFunction, activationFunction);

		List<Model> content = getPmml().getContent();
		content.add(this.model);

		return this.model;
	}

	public NeuralInput addNeuralInput(NormContinuous norm) {
		DerivedField df = new DerivedField(OpTypeType.CONTINUOUS, DataTypeType.DOUBLE);
		df.setExpression(norm);
		String id = String.valueOf(this.neuronCount++);
		NeuralInput input = new NeuralInput(df, id);

		getModel().getNeuralInputs().getNeuralInputs().add(input);
		return input;
	}

	public List<NeuralInput> getNeuralInputs() {
		return getModel().getNeuralInputs().getNeuralInputs();
	}

	public NeuralLayer addNeuralLayer() {
		NeuralLayer layer = new NeuralLayer();
		getModel().getNeuralLayers().add(layer);
		return layer;
	}

	public Neuron addNeuron(NeuralLayer layer, double bias) {
		String id = String.valueOf(this.neuronCount++);
		Neuron neuron = new Neuron(id);
		neuron.setBias(bias);
		layer.getNeurons().add(neuron);
		return neuron;
	}

	public void addConnection(NeuralInput from, Neuron to, double weight) {
		to.getCons().add(new Connection(from.getId(), weight));
	}

	public void addConnection(Neuron from, Neuron to, double weight) {
		to.getCons().add(new Connection(from.getId(), weight));
	}

	public NeuralOutput addNeuralOutput(Neuron neuron, NormContinuous norm) {
		DerivedField df = new DerivedField(OpTypeType.CONTINUOUS, DataTypeType.DOUBLE);
		df.setExpression(norm);
		NeuralOutput output = new NeuralOutput(df, neuron.getId());
		getOrCreateNeuralOutputs().add(output);
		return output;
	}

	public List<NeuralOutput> getOrCreateNeuralOutputs() {
		if (getModel().getNeuralOutputs() == null) {
			getModel().setNeuralOutputs(new NeuralOutputs());
		}
		return getModel().getNeuralOutputs().getNeuralOutputs();
	}

	@Override
	public HashMap<FieldName,String> evaluate(Map<FieldName, ?> params) {
		HashMap<FieldName,String> result = new HashMap<FieldName, String>();
		if (getModel().getFunctionName().equals(MiningFunctionType.REGRESSION)) {
			HashMap<FieldName, Double> r = evaluateRegression(params);
			for (FieldName field: r.keySet()) {
				result.put(field, r.get(field).toString());
			}
		} else if (getModel().getFunctionName().equals(MiningFunctionType.CLASSIFICATION)) {
			HashMap<FieldName, Map<String, Double>> r = evaluateClassification(params);
			for (FieldName field: r.keySet()) {
				result.put(field, r.get(field).toString());
			}
		} else {
			throw new UnsupportedOperationException();
		}
		return result;
	}

	public HashMap<FieldName,Double> evaluateRegression(Map<FieldName, ?> params) {
		HashMap<String, Double> neuronOutputs = evaluateRaw(params);
		HashMap<FieldName,Double> result = new HashMap<FieldName, Double>();
		NeuralOutputs neuralOutputs = getModel().getNeuralOutputs();
		if (neuralOutputs != null) {
			for (NeuralOutput out: neuralOutputs.getNeuralOutputs()) {
				String id = out.getOutputNeuron();

				Expression expression = out.getDerivedField().getExpression();
				if (expression instanceof NormContinuous) {
					NormContinuous normContinuous = (NormContinuous)expression;

					FieldName field = normContinuous.getField();
					double value = NormalizationUtil.denormalize(neuronOutputs.get(id), normContinuous);
					result.put(field, value);
				}
			}
		}
		return result;
	}

	public HashMap<FieldName,Map<String,Double>> evaluateClassification(Map<FieldName, ?> params) {
		HashMap<String, Double> neuronOutputs = evaluateRaw(params);
		HashMap<FieldName, Map<String,Double>> result = new HashMap<FieldName, Map<String,Double>>();
		NeuralOutputs neuralOutputs = getModel().getNeuralOutputs();
		if (neuralOutputs != null) {
			for (NeuralOutput out: neuralOutputs.getNeuralOutputs()) {
				String id = out.getOutputNeuron();

				Expression expression = out.getDerivedField().getExpression();
				if (expression instanceof NormDiscrete) {
					NormDiscrete norm = (NormDiscrete)expression;

					if (!result.containsKey(norm.getField())) {
						result.put(norm.getField(), new HashMap<String, Double>());
					}
					Map<String, Double> vmap = result.get(norm.getField());
					vmap.put(norm.getValue(), neuronOutputs.get(id));
				} else {
					throw new UnsupportedOperationException();
				}
			}
		}

		return result;
	}

	/**
	 * Evaluate neural network.
	 *
	 * @param params - mapping between input data fields and their values
	 * @return mapping between neuron ids and their outputs
	 */
	public HashMap<String,Double> evaluateRaw(Map<FieldName, ?> params) {
		HashMap<String, Double> neuronOutputs = new HashMap<String, Double>();

		List<NeuralInput> inputs = getModel().getNeuralInputs().getNeuralInputs();
		for (NeuralInput inp: inputs) {
			String neuronId = inp.getId();

			double value = evaluateDerivedField(inp.getDerivedField(), params);
			neuronOutputs.put(neuronId, value);
		}

		for (NeuralLayer layer: getModel().getNeuralLayers()) {
			for (Neuron neuron: layer.getNeurons()) {
				double Z = neuron.getBias();
				for (Connection con: neuron.getCons()) {
					double inp = neuronOutputs.get(con.getFrom());
					Z += inp*con.getWeight();
				}
				double output = activation(Z, layer);
				neuronOutputs.put(neuron.getId(), output);
			}
			normalizeNeuronOutputs(layer, neuronOutputs);
		}

		return neuronOutputs;
	}

	private void normalizeNeuronOutputs(NeuralLayer layer, Map<String, Double> neuronOutputs) {
		NnNormalizationMethodType normalization = layer.getNormalizationMethod();
		if (normalization == null) {
			normalization = getModel().getNormalizationMethod();
		}
		if (normalization == NnNormalizationMethodType.NONE) {
			return;
		}

		if (normalization == NnNormalizationMethodType.SOFTMAX) {
			double sum = 0.0;
			for (Neuron neuron: layer.getNeurons()) {
				sum += Math.exp(neuronOutputs.get(neuron.getId()));
			}
			for (Neuron neuron: layer.getNeurons()) {
				double o = neuronOutputs.get(neuron.getId());
				neuronOutputs.put(neuron.getId(), Math.exp(o)/sum);
			}
		} else {
			throw new UnsupportedOperationException(normalization.name()+" normalization");
		}
	}

	private double evaluateDerivedField(DerivedField df, Map<FieldName,?> parameters) {

		if (!df.getDataType().equals(DataTypeType.DOUBLE) && !df.getOptype().equals(OpTypeType.CONTINUOUS)) {
			throw new UnsupportedOperationException();
		}

		Expression expression = df.getExpression();

		if (expression instanceof FieldRef) {
			FieldRef fieldRef = (FieldRef)expression;

			FieldName field = fieldRef.getField();

			// check refs to derived fields in local and global transformation dictionaries
			List<DerivedField> derivedFields = new ArrayList<DerivedField>();
			if (getModel().getLocalTransformations() != null) {
				derivedFields.addAll(getModel().getLocalTransformations().getDerivedFields());
			}
			if (getPmml().getTransformationDictionary() != null) {
				derivedFields.addAll(getPmml().getTransformationDictionary().getDerivedFields());
			}
			for (DerivedField i: derivedFields) {
				if (i.getName().equals(field)) {
					return evaluateDerivedField(i, parameters);
				}
			}

			// refs to a data field in the data dictionary
			for (DataField i: getDataDictionary().getDataFields()) {
				if (i.getName().equals(field)) {
					return ((Number) parameters.get(field)).doubleValue();
				}
			}

			throw new EvaluationException("Can't handle FieldRef: "+field.getValue());
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
		ActivationFunctionType activationFunction = layer.getActivationFunction();
		if (activationFunction == null) {
			activationFunction = getModel().getActivationFunction();
		}

		switch (activationFunction) {
		case THRESHOLD:
			Double threshold = layer.getThreshold();
			if (threshold == null) {
				threshold = getModel().getThreshold();
			}
			if (threshold == null) {
				throw new EvaluationException("Undefined threshold for the NeuralNetwork");
			}
			return z > threshold ? 1.0 : 0.0;
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
			throw new EvaluationException("Unsupported activation function: "+activationFunction);
		}
	}
}
