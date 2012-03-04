/*
 * Copyright (c) 2012 University of Tartu
 */
package org.jpmml.manager;

import java.util.*;

import org.dmg.pmml.*;

abstract
public class SegmentationModelManager extends MiningModelManager {

	public SegmentationModelManager(){
	}

	public SegmentationModelManager(PMML pmml){
		super(pmml);
	}

	public SegmentationModelManager(PMML pmml, MiningModel miningModel){
		super(pmml, miningModel);
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

	public List<Segment> getSegments(){
		return getSegmentation().getSegments();
	}
}