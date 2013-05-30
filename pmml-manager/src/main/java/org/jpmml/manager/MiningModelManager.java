/*
 * Copyright (c) 2012 University of Tartu
 */
package org.jpmml.manager;

import java.util.*;

import org.dmg.pmml.*;

abstract
public class MiningModelManager extends ModelManager<MiningModel> {

	private MiningModel miningModel = null;


	public MiningModelManager(){
	}

	public MiningModelManager(PMML pmml){
		this(pmml, find(pmml.getContent(), MiningModel.class));
	}

	public MiningModelManager(PMML pmml, MiningModel miningModel){
		super(pmml);

		this.miningModel = miningModel;
	}

	@Override
	public MiningModel getModel(){
		ensureNotNull(this.miningModel);

		return this.miningModel;
	}

	/**
	 * @throws ModelManagerException If the Model already exists
	 *
	 * @see #getModel()
	 */
	public MiningModel createModel(MiningFunctionType miningFunction){
		ensureNull(this.miningModel);

		this.miningModel = new MiningModel(new MiningSchema(), miningFunction);

		List<Model> content = getPmml().getContent();
		content.add(this.miningModel);

		return this.miningModel;
	}

	/**
	 * @throws ModelManagerException If the Segmentation does not exist
	 */
	public Segmentation getSegmentation(){
		MiningModel miningModel = getModel();

		Segmentation segmentation = miningModel.getSegmentation();
		ensureNotNull(segmentation);

		return segmentation;
	}

	/**
	 * @throws ModelManagerException If the Segmentation already exists
	 */
	public Segmentation createSegmentation(MultipleModelMethodType multipleModelMethod){
		MiningModel miningModel = getModel();

		Segmentation segmentation = miningModel.getSegmentation();
		ensureNull(segmentation);

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
}