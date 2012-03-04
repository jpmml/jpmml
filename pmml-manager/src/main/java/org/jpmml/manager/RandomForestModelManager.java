/*
 * Copyright (c) 2012 University of Tartu
 */
package org.jpmml.manager;

import java.util.*;

import org.dmg.pmml.*;

public class RandomForestModelManager extends SegmentationModelManager {

	public RandomForestModelManager(){
	}

	public RandomForestModelManager(PMML pmml){
		super(pmml);
	}

	public RandomForestModelManager(PMML pmml, MiningModel miningModel){
		super(pmml, miningModel);
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

	/**
	 * @see #evaluateRegression(Map)
	 */
	@Override
	public Object evaluate(Map<FieldName, ?> parameters){
		MiningModel model = getModel();

		MiningFunctionType miningFunction = model.getFunctionName();
		switch(miningFunction){
			case REGRESSION:
				return evaluateRegression(parameters);
			default:
				break;
		}

		throw new EvaluationException();
	}

	public Double evaluateRegression(Map<FieldName, ?> parameters){
		Segmentation segmentation = getSegmentation();

		double sum = 0;
		double weightedSum = 0;

		int count = 0;

		List<Segment> segments = getSegments();
		for(Segment segment : segments){
			Predicate predicate = segment.getPredicate();

			Boolean selectable = PredicateUtil.evaluatePredicate(predicate, parameters);
			if(selectable == null){
				throw new EvaluationException();
			} // End if

			if(!selectable.booleanValue()){
				continue;
			}

			TreeModel treeModel = (TreeModel)segment.getModel();
			if(treeModel == null){
				throw new EvaluationException();
			}

			TreeModelManager treeModelManager = new TreeModelManager(getPmml(), treeModel);

			String score = treeModelManager.evaluate(parameters);
			if(score == null){
				throw new EvaluationException();
			}

			Double value = Double.valueOf(score);

			sum += value.doubleValue();
			weightedSum += (segment.getWeight() * value.doubleValue());

			count++;
		}

		MultipleModelMethodType multipleModelMethod = segmentation.getMultipleModelMethod();
		switch(multipleModelMethod){
			case SUM:
				return sum;
			case AVERAGE:
				return (sum / count);
			case WEIGHTED_AVERAGE:
				return (weightedSum / count); // XXX
			default:
				break;
		}

		throw new EvaluationException();
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