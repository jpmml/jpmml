/*
 * Copyright (c) 2011 University of Tartu
 */
package org.jpmml.manager;

import java.util.*;

import org.dmg.pmml.*;

public class NeuralNetworkManager extends ModelManager<NeuralNetwork> {

	private NeuralNetwork neuralNetwork = null;


	public NeuralNetworkManager() {
	}

	public NeuralNetworkManager(PMML pmml) {
		this(pmml, find(pmml.getContent(), NeuralNetwork.class));
	}

	public NeuralNetworkManager(PMML pmml, NeuralNetwork neuralNetwork) {
		super(pmml);

		this.neuralNetwork = neuralNetwork;
	}

	public String getSummary(){
		return "Neural network";
	}

	@Override
	public NeuralNetwork getModel() {
		ensureNotNull(this.neuralNetwork);

		return this.neuralNetwork;
	}

	/**
	 * @see #getModel()
	 */
	public NeuralNetwork createModel(MiningFunctionType miningFunction, ActivationFunctionType activationFunction) {
		ensureNull(this.neuralNetwork);

		this.neuralNetwork = new NeuralNetwork(new MiningSchema(), new NeuralInputs(), miningFunction, activationFunction);

		getModels().add(this.neuralNetwork);

		return this.neuralNetwork;
	}

	public List<NeuralInput> getNeuralInputs() {
		NeuralNetwork neuralNetwork = getModel();

		return neuralNetwork.getNeuralInputs().getNeuralInputs();
	}

	public NeuralInput addNeuralInput(String id, NormContinuous normContinuous) {
		DerivedField derivedField = new DerivedField(OpType.CONTINUOUS, DataType.DOUBLE);
		derivedField.setExpression(normContinuous);

		NeuralInput neuralInput = new NeuralInput(derivedField, id);

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

	static
	public Neuron addNeuron(NeuralLayer neuralLayer, String id, Double bias) {
		Neuron neuron = new Neuron(id);
		neuron.setBias(bias);

		(neuralLayer.getNeurons()).add(neuron);

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
		DerivedField derivedField = new DerivedField(OpType.CONTINUOUS, DataType.DOUBLE);
		derivedField.setExpression(normCountinuous);

		NeuralOutput output = new NeuralOutput(derivedField, neuron.getId());

		getOrCreateNeuralOutputs().add(output);

		return output;
	}
}
