/*
 * Copyright (c) 2011 University of Tartu
 */
package org.jpmml.manager;

import java.util.*;

import org.dmg.pmml.*;

import com.google.common.collect.*;

import static com.google.common.base.Preconditions.*;

public class NeuralNetworkManager extends ModelManager<NeuralNetwork> implements HasEntityRegistry<Entity> {

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

	@Override
	public String getSummary(){
		return "Neural network";
	}

	@Override
	public NeuralNetwork getModel() {
		checkState(this.neuralNetwork != null);

		return this.neuralNetwork;
	}

	/**
	 * @see #getModel()
	 */
	public NeuralNetwork createModel(MiningFunctionType miningFunction, ActivationFunctionType activationFunction) {
		checkState(this.neuralNetwork == null);

		this.neuralNetwork = new NeuralNetwork(new MiningSchema(), new NeuralInputs(), miningFunction, activationFunction);

		getModels().add(this.neuralNetwork);

		return this.neuralNetwork;
	}

	public List<NeuralInput> getNeuralInputs() {
		NeuralNetwork neuralNetwork = getModel();

		return (neuralNetwork.getNeuralInputs()).getNeuralInputs();
	}

	/**
	 * @param id Unique identifier
	 *
	 * @see #getEntityRegistry()
	 */
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

	@Override
	public BiMap<String, Entity> getEntityRegistry(){
		BiMap<String, Entity> result = HashBiMap.create();

		List<NeuralInput> neuralInputs = getNeuralInputs();
		for(NeuralInput neuralInput : neuralInputs){
			putEntity(neuralInput, result);
		}

		List<NeuralLayer> neuralLayers = getNeuralLayers();
		for(NeuralLayer neuralLayer : neuralLayers){
			List<Neuron> neurons = neuralLayer.getNeurons();

			for(Neuron neuron : neurons){
				putEntity(neuron, result);
			}
		}

		return result;
	}

	/**
	 * @param id Unique identifier
	 *
	 * @see #getEntityRegistry()
	 */
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
