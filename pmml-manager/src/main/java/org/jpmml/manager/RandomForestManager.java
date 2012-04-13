/*
 * Copyright (c) 2012 University of Tartu
 */
package org.jpmml.manager;

import java.util.*;

import org.dmg.pmml.*;

public class RandomForestManager extends SegmentationModelManager {

	public RandomForestManager(){
	}

	public RandomForestManager(PMML pmml){
		super(pmml);
	}

	public RandomForestManager(PMML pmml, MiningModel miningModel){
		super(pmml, miningModel);
	}

	public String getSummary(){
		return "Random forest";
	}

	public Segment addSegment(TreeModel treeModel){
		return addSegment(new True(), treeModel);
	}

	public Segment addSegment(Predicate predicate, TreeModel treeModel){
		Segment segment = new Segment();
		segment.setPredicate(predicate);
		segment.setModel((TreeModel)treeModel);

		getSegments().add(segment);

		return segment;
	}

	static
	public boolean isRandomForest(MiningModel miningModel){
		Segmentation segmentation = miningModel.getSegmentation();

		if(segmentation != null){
			return isRandomForest(segmentation);
		}

		return false;
	}

	static
	private boolean isRandomForest(Segmentation segmentation){
		boolean result = true;

		List<Segment> segments = segmentation.getSegments();
		for(Segment segment : segments){
			result &= isTree(segment);
		}

		return result;
	}

	static
	private boolean isTree(Segment segment){
		Model model = segment.getModel();

		return (model instanceof TreeModel);
	}
}