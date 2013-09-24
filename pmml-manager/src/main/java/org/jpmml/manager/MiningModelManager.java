/*
 * Copyright (c) 2012 University of Tartu
 */
package org.jpmml.manager;

import java.util.*;

import org.dmg.pmml.*;

import static com.google.common.base.Preconditions.*;

public class MiningModelManager extends ModelManager<MiningModel> {

	private MiningModel miningModel = null;


	public MiningModelManager(){
	}

	public MiningModelManager(PMML pmml){
		this(pmml, find(pmml.getModels(), MiningModel.class));
	}

	public MiningModelManager(PMML pmml, MiningModel miningModel){
		super(pmml);

		this.miningModel = miningModel;
	}

	@Override
	public String getSummary(){
		MiningModel miningModel = getModel();

		if(isRandomForest(miningModel)){
			return "Random forest";
		}

		return "Ensemble model";
	}

	@Override
	public MiningModel getModel(){
		checkState(this.miningModel != null);

		return this.miningModel;
	}

	/**
	 * @see #getModel()
	 */
	public MiningModel createModel(MiningFunctionType miningFunction){
		checkState(this.miningModel == null);

		this.miningModel = new MiningModel(new MiningSchema(), miningFunction);

		getModels().add(this.miningModel);

		return this.miningModel;
	}

	public Segmentation getSegmentation(){
		MiningModel miningModel = getModel();

		Segmentation segmentation = miningModel.getSegmentation();
		checkState(segmentation != null);

		return segmentation;
	}

	public Segmentation createSegmentation(MultipleModelMethodType multipleModelMethod){
		MiningModel miningModel = getModel();

		Segmentation segmentation = miningModel.getSegmentation();
		checkState(segmentation == null);

		segmentation = new Segmentation(multipleModelMethod);
		miningModel.setSegmentation(segmentation);

		return segmentation;
	}

	public Segment addSegment(Model model){
		return addSegment(new True(), model);
	}

	public Segment addSegment(Predicate predicate, Model model){
		Segment segment = new Segment();
		segment.setPredicate(predicate);
		segment.setModel(model);

		getSegments().add(segment);

		return segment;
	}

	public List<Segment> getSegments(){
		return getSegmentation().getSegments();
	}

	static
	private boolean isRandomForest(MiningModel miningModel){
		Segmentation segmentation = miningModel.getSegmentation();

		if(segmentation == null){
			return false;
		}

		List<Segment> segments = segmentation.getSegments();

		// How many trees does it take to make a forest?
		boolean result = (segments.size() > 3);

		for(Segment segment : segments){
			Model model = segment.getModel();

			result &= (model instanceof TreeModel);
		}

		return result;
	}
}