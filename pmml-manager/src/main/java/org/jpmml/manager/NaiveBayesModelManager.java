/*
 * Copyright, KNIME.com AG, Zurich, Switzerland
 */
package org.jpmml.manager;

import java.util.*;

import org.dmg.pmml.*;

import com.google.common.collect.*;

import static com.google.common.base.Preconditions.*;

public class NaiveBayesModelManager extends ModelManager<NaiveBayesModel> {

	private NaiveBayesModel naiveBayesModel = null;


	public NaiveBayesModelManager(){
	}

	public NaiveBayesModelManager(PMML pmml){
		this(pmml, find(pmml.getContent(), NaiveBayesModel.class));
	}

	public NaiveBayesModelManager(PMML pmml, NaiveBayesModel naiveBayesModel){
		super(pmml);

		this.naiveBayesModel = naiveBayesModel;
	}

	@Override
	public String getSummary(){
		return "Naive Bayes model";
	}

	@Override
	public NaiveBayesModel getModel(){
		checkState(this.naiveBayesModel != null);

		return this.naiveBayesModel;
	}

	public List<BayesInput> getBayesInputs(){
		NaiveBayesModel naiveBayesModel = getModel();

		List<BayesInput> result = Lists.newArrayList();

		BayesInputs bayesInputs = naiveBayesModel.getBayesInputs();

		// The TargetValueStats element is not part of the PMML standard (as of PMML 4.1).
		// Therefore, every BayesInput element that deals with TargetValueStats element has to be surrounded by an Extension element.
		// Once the TargetValueStats element is incorporated into the PMML standard then it will be no longer necessary.
		List<Extension> extensions = bayesInputs.getExtensions();
		for(Extension extension : extensions){
			BayesInput bayesInput = ExtensionUtil.getExtension(extension, BayesInput.class);
			if(bayesInput == null){
				continue;
			}

			result.add(bayesInput);
		}

		result.addAll(bayesInputs.getBayesInputs());

		return result;
	}

	public BayesOutput getBayesOutput(){
		NaiveBayesModel naiveBayesModel = getModel();

		return naiveBayesModel.getBayesOutput();
	}
}