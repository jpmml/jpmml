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

	public String getSummary(){
		return "Neural network";
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
		DerivedField derivedField = new DerivedField(OpType.CONTINUOUS, DataType.DOUBLE);
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
		DerivedField derivedField = new DerivedField(OpType.CONTINUOUS, DataType.DOUBLE);
		derivedField.setExpression(normCountinuous);

		NeuralOutput output = new NeuralOutput(derivedField, neuron.getId());

		getOrCreateNeuralOutputs().add(output);

		return output;
	}

	private String nextId(){
		return String.valueOf(this.neuronCount++);
	}
}
