/*
 * Copyright (c) 2011 University of Tartu
 */
package org.jpmml.manager;

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
	
	public NeuralNetworkManager(PMML pmml, NeuralNetwork model) {
		super(pmml);
		this.model = model;
	}

	@Override
	public NeuralNetwork getModel() {
		if(this.model == null){
			throw new IllegalStateException();
		}
		return this.model;
	}
	
	public NeuralNetwork createModel(MiningFunctionType miningFunction, ActivationFunctionType activationFunction) {
		if(this.model != null){
			throw new IllegalStateException();
		}

		this.model = new NeuralNetwork(new MiningSchema(), new NeuralInputs(), miningFunction, activationFunction); 

		List<Model> content = getPmml().getContent();
		content.add(this.model);
    
		return this.model;
	}
	
	public NeuralInput addNeuralInput(NormContinuous norm) {
		DerivedField df = new DerivedField(OpTypeType.CONTINUOUS, DataTypeType.DOUBLE);
		df.setNormContinuous(norm);
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
		df.setNormContinuous(norm);
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
	public HashMap<FieldName,Double> evaluate(Map<FieldName, ?> params) {
		HashMap<String, Double> neuronOutputs = evaluateRaw(params);
		HashMap<FieldName,Double> result = new HashMap<FieldName, Double>();
		NeuralOutputs neuralOutputs = getModel().getNeuralOutputs();
		if (neuralOutputs != null) {
			for (NeuralOutput out: neuralOutputs.getNeuralOutputs()) {
				String id = out.getOutputNeuron();
				
				// TODO: support other expressions in DerivedField: fieldref
				NormContinuous norm = out.getDerivedField().getNormContinuous();
				if (norm == null) {
					throw new UnsupportedOperationException();
				}
				FieldName field = norm.getField();
				
				double value = NormalizationUtil.denormalize(neuronOutputs.get(id), norm);
				result.put(field, value);
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
			if (inp.getDerivedField().getOptype().equals(OpTypeType.CONTINUOUS)) {
				NormContinuous norm = inp.getDerivedField().getNormContinuous();
				FieldName inputField = norm.getField();
				Double value = (Double) params.get(inputField);
				neuronOutputs.put(neuronId, NormalizationUtil.normalize(norm, value));
			} else	{
				throw new UnsupportedOperationException();
			}
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
		}
		
		return neuronOutputs;
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
