/*
 * Copyright (c) 2013 University of Tartu
 */
package org.jpmml.evaluator;

import java.util.*;

import org.jpmml.manager.*;

import org.dmg.pmml.*;

import com.google.common.base.*;
import com.google.common.base.Predicate;
import com.google.common.collect.*;

public class GeneralRegressionModelEvaluator extends GeneralRegressionModelManager implements Evaluator {

	public GeneralRegressionModelEvaluator(PMML pmml){
		super(pmml);
	}

	public GeneralRegressionModelEvaluator(PMML pmml, GeneralRegressionModel generalRegressionModel){
		super(pmml, generalRegressionModel);
	}

	@Override
	public Object prepare(FieldName name, Object value){
		return ParameterUtil.prepare(getDataField(name), getMiningField(name), value);
	}

	@Override
	public Map<FieldName, ?> evaluate(Map<FieldName, ?> arguments){
		GeneralRegressionModel generalRegressionModel = getModel();
		if(!generalRegressionModel.isScorable()){
			throw new InvalidResultException(generalRegressionModel);
		}

		Map<FieldName, ?> predictions;

		ModelManagerEvaluationContext context = new ModelManagerEvaluationContext(this, arguments);

		MiningFunctionType miningFunction = generalRegressionModel.getFunctionName();
		switch(miningFunction){
			case REGRESSION:
				predictions = evaluateRegression(context);
				break;
			case CLASSIFICATION:
				predictions = evaluateClassification(context);
				break;
			default:
				throw new UnsupportedFeatureException(generalRegressionModel, miningFunction);
		}

		return OutputUtil.evaluate(predictions, context);
	}

	private Map<FieldName, ?> evaluateRegression(ModelManagerEvaluationContext context){
		GeneralRegressionModel generalRegressionModel = getModel();

		Map<FieldName, ?> arguments = getArguments(context);

		Map<String, Map<String, Row>> ppMatrixMap = parsePPMatrix();
		if(ppMatrixMap.size() != 1 || !ppMatrixMap.containsKey(null)){
			throw new InvalidFeatureException(getPPMatrix());
		}

		Map<String, Row> parameterPredictorRows = ppMatrixMap.get(null);

		Map<String, List<PCell>> paramMatrixMap = parseParamMatrix();
		if(paramMatrixMap.size() != 1 || !paramMatrixMap.containsKey(null)){
			throw new InvalidFeatureException(getParamMatrix());
		}

		Iterable<PCell> parameterCells = paramMatrixMap.get(null);

		Double result = computeDotProduct(parameterCells, parameterPredictorRows, arguments);

		GeneralRegressionModel.ModelType modelType = generalRegressionModel.getModelType();
		switch(modelType){
			case REGRESSION:
				break;
			case GENERAL_LINEAR:
			case GENERALIZED_LINEAR:
				result = computeLink(result, context);
				break;
			default:
				throw new UnsupportedFeatureException(generalRegressionModel, modelType);
		}

		return TargetUtil.evaluateRegression(result, context);
	}

	private Map<FieldName, ? extends ClassificationMap> evaluateClassification(ModelManagerEvaluationContext context){
		GeneralRegressionModel generalRegressionModel = getModel();

		FieldName targetField = getTargetField();

		DataField dataField = getDataField(targetField);

		OpType opType = dataField.getOptype();
		switch(opType){
			case CATEGORICAL:
			case ORDINAL:
				break;
			default:
				throw new UnsupportedFeatureException(dataField, opType);
		}

		List<String> targetCategories = ParameterUtil.getValidValues(dataField);
		if(targetCategories.size() < 2){
			throw new InvalidFeatureException(dataField);
		}

		Map<FieldName, ?> arguments = getArguments(context);

		Map<String, Map<String, Row>> ppMatrixMap = parsePPMatrix();

		final
		Map<String, List<PCell>> paramMatrixMap = parseParamMatrix();

		GeneralRegressionModel.ModelType modelType = generalRegressionModel.getModelType();

		String targetReferenceCategory = generalRegressionModel.getTargetReferenceCategory();

		switch(modelType){
			case GENERAL_LINEAR:
			case GENERALIZED_LINEAR:
			case MULTINOMIAL_LOGISTIC:
				if(targetReferenceCategory == null){
					Predicate<String> filter = new Predicate<String>(){

						@Override
						public boolean apply(String string){
							return !paramMatrixMap.containsKey(string);
						}
					};

					// "The reference category is the one from DataDictionary that does not appear in the ParamMatrix"
					Set<String> targetReferenceCategories = Sets.newLinkedHashSet(Iterables.filter(targetCategories, filter));
					if(targetReferenceCategories.size() != 1){
						throw new InvalidFeatureException(getParamMatrix());
					}

					targetReferenceCategory = Iterables.getOnlyElement(targetReferenceCategories);
				}
				break;
			case ORDINAL_MULTINOMIAL:
				break;
			default:
				throw new UnsupportedFeatureException(generalRegressionModel, modelType);
		}

		if(targetReferenceCategory != null){

			// Move the element from any position to the last position
			if(targetCategories.remove(targetReferenceCategory)){
				targetCategories.add(targetReferenceCategory);
			}
		}

		ClassificationMap result = new ClassificationMap(ClassificationMap.Type.PROBABILITY);

		Double previousValue = null;

		for(int i = 0; i < targetCategories.size(); i++){
			String targetCategory = targetCategories.get(i);

			Double value;

			// Categories from the first category to the second-to-last category
			if(i < (targetCategories.size() - 1)){
				Map<String, Row> parameterPredictorRow = ppMatrixMap.get(targetCategory);
				if(parameterPredictorRow == null){
					parameterPredictorRow = ppMatrixMap.get(null);
				} // End if

				if(parameterPredictorRow == null){
					throw new InvalidFeatureException(getPPMatrix());
				}

				Iterable<PCell> parameterCells;

				switch(modelType){
					case GENERAL_LINEAR:
					case GENERALIZED_LINEAR:
					case MULTINOMIAL_LOGISTIC:
						// PCell elements must have non-null targetCategory attribute in case of multinomial categories, but can do without in case of binomial categories
						parameterCells = paramMatrixMap.get(targetCategory);
						if(parameterCells == null && targetCategories.size() == 2){
							parameterCells = paramMatrixMap.get(null);
						} // End if

						if(parameterCells == null){
							throw new InvalidFeatureException(getParamMatrix());
						}
						break;
					case ORDINAL_MULTINOMIAL:
						// "ParamMatrix specifies different values for the intercept parameter: one for each target category except one"
						List<PCell> interceptCells = paramMatrixMap.get(targetCategory);
						if(interceptCells == null || interceptCells.size() != 1){
							throw new InvalidFeatureException(getParamMatrix());
						}

						// "Values for all other parameters are constant across all target variable values"
						parameterCells = paramMatrixMap.get(null);
						if(parameterCells == null){
							throw new InvalidFeatureException(getParamMatrix());
						}

						parameterCells = Iterables.concat(interceptCells, parameterCells);
						break;
					default:
						throw new UnsupportedFeatureException(generalRegressionModel, modelType);
				}

				value = computeDotProduct(parameterCells, parameterPredictorRow, arguments);

				switch(modelType){
					case GENERAL_LINEAR:
					case GENERALIZED_LINEAR:
						value = computeLink(value, context);
						break;
					case MULTINOMIAL_LOGISTIC:
						value = Math.exp(value);
						break;
					case ORDINAL_MULTINOMIAL:
						value = computeCumulativeLink(value, context);
						break;
					default:
						throw new UnsupportedFeatureException(generalRegressionModel, modelType);
				}
			} else

			// The last category
			{
				value = 0d;

				switch(modelType){
					case GENERAL_LINEAR:
					case GENERALIZED_LINEAR:
						value = computeLink(value, context);
						break;
					case MULTINOMIAL_LOGISTIC:
						value = Math.exp(value);
						break;
					case ORDINAL_MULTINOMIAL:
						value = 1d;
						break;
					default:
						throw new UnsupportedFeatureException(generalRegressionModel, modelType);
				}
			}

			switch(modelType){
				case GENERAL_LINEAR:
				case GENERALIZED_LINEAR:
				case MULTINOMIAL_LOGISTIC:
					{
						result.put(targetCategory, value);
					}
					break;
				case ORDINAL_MULTINOMIAL:
					if(previousValue == null){
						result.put(targetCategory, value);
					} else

					{
						result.put(targetCategory, value - previousValue);
					}
					break;
				default:
					throw new UnsupportedFeatureException(generalRegressionModel, modelType);
			}

			previousValue = value;
		}

		switch(modelType){
			case GENERAL_LINEAR:
			case GENERALIZED_LINEAR:
			case MULTINOMIAL_LOGISTIC:
				result.normalizeValues();
				break;
			case ORDINAL_MULTINOMIAL:
				break;
			default:
				throw new UnsupportedFeatureException(generalRegressionModel, modelType);
		}

		return TargetUtil.evaluateClassification(Collections.singletonMap(targetField, result), context);
	}

	private Double computeDotProduct(Iterable<PCell> parameterCells, Map<String, Row> parameterPredictorRows, Map<FieldName, ?> arguments){
		double sum = 0d;

		for(PCell parameterCell : parameterCells){
			Double x;

			Row parameterPredictorRow = parameterPredictorRows.get(parameterCell.getParameterName());
			if(parameterPredictorRow != null){
				x = parameterPredictorRow.evaluate(arguments);
			} else

			// The row is empty
			{
				x = 1d;
			} // End if

			if(x == null){
				continue;
			}

			sum += (x.doubleValue() * parameterCell.getBeta());
		}

		return sum;
	}

	private Double computeLink(Double value, EvaluationContext context){
		GeneralRegressionModel generalRegressionModel = getModel();

		Double a = getValue(DataType.DOUBLE, context.getArgument(generalRegressionModel.getOffsetVariable()), generalRegressionModel.getOffsetValue());
		if(a == null){
			a = 0d;
		}

		Integer b = getValue(DataType.INTEGER, context.getArgument(generalRegressionModel.getTrialsVariable()), generalRegressionModel.getTrialsValue());
		if(b == null){
			b = 1;
		}

		Double d = generalRegressionModel.getLinkParameter();

		LinkFunctionType linkFunction = generalRegressionModel.getLinkFunction();
		if(linkFunction == null){
			throw new InvalidFeatureException(generalRegressionModel);
		}

		switch(linkFunction){
			case ODDSPOWER:
			case POWER:
				if(d == null){
					throw new InvalidFeatureException(generalRegressionModel);
				}
				break;
			default:
				break;
		}

		switch(linkFunction){
			case CLOGLOG:
				return (1d - Math.exp(-Math.exp(value + a))) * b;
			case IDENTITY:
				return (value + a) * b;
			case LOG:
				return Math.exp(value + a) * b;
			case LOGC:
				return (1d - Math.exp(value + a)) * b;
			case LOGIT:
				return (1d / (1d + Math.exp(-(value + a)))) * b;
			case LOGLOG:
				return Math.exp(-Math.exp(-(value + a))) * b;
			case ODDSPOWER:
				if(d < 0d || d > 0d){
					return (1d / (1d + Math.pow(1d + d * (value + a), -(1d / d)))) * b;
				}
				return (1d / (1d + Math.exp(-(value + a)))) * b;
			case POWER:
				if(d < 0d || d > 0d){
					return Math.pow(value + a, 1d / d) * b;
				}
				return Math.exp(value + a) * b;
			default:
				throw new UnsupportedFeatureException(generalRegressionModel, linkFunction);
		}
	}

	private Double computeCumulativeLink(Double value, EvaluationContext context){
		GeneralRegressionModel generalRegressionModel = getModel();

		Double a = getValue(DataType.DOUBLE, context.getArgument(generalRegressionModel.getOffsetVariable()), generalRegressionModel.getOffsetValue());
		if(a == null){
			a = 0d;
		}

		CumulativeLinkFunctionType cumulativeLinkFunction = generalRegressionModel.getCumulativeLink();
		if(cumulativeLinkFunction == null){
			throw new InvalidFeatureException(generalRegressionModel);
		}

		switch(cumulativeLinkFunction){
			case LOGIT:
				return 1d / (1d + Math.exp(-(value + a)));
			case CLOGLOG:
				return 1d - Math.exp(-Math.exp(value + a));
			case LOGLOG:
				return Math.exp(-Math.exp(-(value + a)));
			case CAUCHIT:
				return 0.5d + (1d / Math.PI) * Math.atan(value + a);
			default:
				throw new UnsupportedFeatureException(generalRegressionModel, cumulativeLinkFunction);
		}
	}

	private Map<FieldName, ?> getArguments(EvaluationContext context){
		GeneralRegressionModel generalRegressionModel = getModel();

		FactorList factorList = generalRegressionModel.getFactorList();
		CovariateList covariateList = generalRegressionModel.getCovariateList();

		Map<FieldName, Object> result = Maps.newLinkedHashMap();

		Iterable<Predictor> predictors = Iterables.concat(factorList.getPredictors(), covariateList.getPredictors());
		for(Predictor predictor : predictors){
			FieldName name = predictor.getName();

			Categories categories = predictor.getCategories();
			if(categories != null){
				throw new UnsupportedFeatureException(categories);
			}

			Matrix matrix = predictor.getMatrix();
			if(matrix != null){
				throw new UnsupportedFeatureException(matrix);
			}

			result.put(name, context.getArgument(name));
		}

		return result;
	}

	private Map<String, Map<String, Row>> parsePPMatrix(){
		Function<List<PPCell>, Row> rowBuilder = new Function<List<PPCell>, Row>(){

			private GeneralRegressionModel generalRegressionModel = getModel();

			private FactorList factorList = this.generalRegressionModel.getFactorList();

			private CovariateList covariateList = this.generalRegressionModel.getCovariateList();


			@Override
			public Row apply(List<PPCell> ppCells){
				Row result = new Row();

				ppCells:
				for(PPCell ppCell : ppCells){
					FieldName name = ppCell.getPredictorName();

					Predictor factor = find(this.factorList, name);
					if(factor != null){
						List<PPCell> factorCells = result.getFactorCells();

						factorCells.add(ppCell);

						continue ppCells;
					}

					Predictor covariate = find(this.covariateList, name);
					if(covariate != null){
						List<PPCell> covariateCells = result.getCovariateCells();

						covariateCells.add(ppCell);

						continue ppCells;
					}


					throw new InvalidFeatureException(ppCell);
				}

				return result;
			}

			private Predictor find(PredictorList predictorList, FieldName name){
				List<Predictor> predictors = predictorList.getPredictors();

				for(Predictor predictor : predictors){

					if((predictor.getName()).equals(name)){
						return predictor;
					}
				}

				return null;
			}
		};

		PPMatrix ppMatrix = getPPMatrix();

		ListMultimap<String, PPCell> targetCategoryMap = groupByTargetCategory(ppMatrix.getPPCells());

		Map<String, Map<String, Row>> result = Maps.newLinkedHashMap();

		Collection<Map.Entry<String, List<PPCell>>> targetCategoryEntries = (asMap(targetCategoryMap)).entrySet();
		for(Map.Entry<String, List<PPCell>> targetCategoryEntry : targetCategoryEntries){
			Map<String, Row> predictorMap = Maps.newLinkedHashMap();

			ListMultimap<String, PPCell> parameterNameMap = groupByParameterName(targetCategoryEntry.getValue());

			Collection<Map.Entry<String, List<PPCell>>> parameterNameEntries = (asMap(parameterNameMap)).entrySet();
			for(Map.Entry<String, List<PPCell>> parameterNameEntry : parameterNameEntries){
				predictorMap.put(parameterNameEntry.getKey(), rowBuilder.apply(parameterNameEntry.getValue()));
			}

			result.put(targetCategoryEntry.getKey(), predictorMap);
		}

		return result;
	}

	private Map<String, List<PCell>> parseParamMatrix(){
		ParamMatrix paramMatrix = getParamMatrix();

		ListMultimap<String, PCell> targetCategoryCells = groupByTargetCategory(paramMatrix.getPCells());

		return asMap(targetCategoryCells);
	}

	@SuppressWarnings (
		value = {"unchecked"}
	)
	static
	private <V extends Number> V getValue(DataType dataType, Object argumentValue, V xmlValue){

		if(argumentValue != null){
			return (V)ParameterUtil.cast(dataType, argumentValue);
		} // End if

		return xmlValue;
	}

	@SuppressWarnings (
		value = {"rawtypes", "unchecked"}
	)
	static
	private <C extends ParameterCell> Map<String, List<C>> asMap(ListMultimap<String, C> multimap){
		return (Map)multimap.asMap();
	}

	static
	private <C extends ParameterCell> ListMultimap<String, C> groupByParameterName(List<C> cells){
		Function<C, String> function = new Function<C, String>(){

			@Override
			public String apply(C cell){
				return cell.getParameterName();
			}
		};

		return groupCells(cells, function);
	}

	static
	private <C extends ParameterCell> ListMultimap<String, C> groupByTargetCategory(List<C> cells){
		Function<C, String> function = new Function<C, String>(){

			@Override
			public String apply(C cell){
				return cell.getTargetCategory();
			}
		};

		return groupCells(cells, function);
	}

	static
	private <C extends ParameterCell> ListMultimap<String, C> groupCells(List<C> cells, Function<C, String> function){
		ListMultimap<String, C> result = ArrayListMultimap.create();

		for(C cell : cells){
			result.put(function.apply(cell), cell);
		}

		return result;
	}

	static
	private class Row {

		private List<PPCell> factorCells = Lists.newArrayList();

		private List<PPCell> covariateCells = Lists.newArrayList();


		public Double evaluate(Map<FieldName, ?> arguments){
			List<PPCell> factorCells = getFactorCells();
			List<PPCell> covariateCells = getCovariateCells();

			// The row is empty
			if(factorCells.isEmpty() && covariateCells.isEmpty()){
				return 1d;
			}

			Boolean match = calculateMatch(factorCells, arguments);
			Double product = calculateProduct(covariateCells, arguments);

			if(covariateCells.isEmpty()){

				if(match != null){
					return (match.booleanValue() ? 1d : 0d);
				}

				return null;
			} else

			if(factorCells.isEmpty()){

				if(product != null){
					return product;
				}

				return null;
			} else

			{
				if(match != null && product != null){
					return (match.booleanValue() ? product : 0d);
				}

				return null;
			}
		}

		public List<PPCell> getFactorCells(){
			return this.factorCells;
		}

		public List<PPCell> getCovariateCells(){
			return this.covariateCells;
		}

		static
		private Boolean calculateMatch(List<PPCell> ppCells, Map<FieldName, ?> arguments){

			for(PPCell ppCell : ppCells){
				Object value = arguments.get(ppCell.getPredictorName());
				if(value == null){
					return null;
				}

				boolean equals = ParameterUtil.equals(value, ppCell.getValue());
				if(!equals){
					return Boolean.FALSE;
				}
			}

			return Boolean.TRUE;
		}

		static
		private Double calculateProduct(List<PPCell> ppCells, Map<FieldName, ?> arguments){
			Double result = null;

			for(PPCell ppCell : ppCells){
				Object value = arguments.get(ppCell.getPredictorName());
				if(value == null){
					return null;
				}

				Double doubleValue = (Double)ParameterUtil.cast(DataType.DOUBLE, value);

				Double multiplicity = Double.valueOf(ppCell.getValue());

				if(result == null){
					result = Math.pow(doubleValue.doubleValue(), multiplicity.doubleValue());
				} else

				{
					result = result * Math.pow(doubleValue.doubleValue(), multiplicity.doubleValue());
				}
			}

			return result;
		}
	}
}