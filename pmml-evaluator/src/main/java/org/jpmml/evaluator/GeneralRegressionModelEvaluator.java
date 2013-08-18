/*
 * Copyright (c) 2013 University of Tartu
 */
package org.jpmml.evaluator;

import java.util.*;

import org.jpmml.manager.*;

import org.dmg.pmml.*;

import com.google.common.base.*;
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
			default:
				throw new UnsupportedFeatureException(generalRegressionModel, miningFunction);
		}

		return OutputUtil.evaluate(predictions, context);
	}

	private Map<FieldName, ?> evaluateRegression(ModelManagerEvaluationContext context){
		GeneralRegressionModel generalRegressionModel = getModel();

		FactorList factorList = generalRegressionModel.getFactorList();
		CovariateList covariateList = generalRegressionModel.getCovariateList();

		Map<FieldName, Object> arguments = Maps.newLinkedHashMap();

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

			arguments.put(name, context.getArgument(name));
		}

		PPMatrix ppMatrix = getPPMatrix();

		Map<String, Map<String, Row>> ppMatrixMap = parsePPMatrix(ppMatrix, factorList, covariateList);
		if(ppMatrixMap.size() != 1 || !ppMatrixMap.containsKey(null)){
			throw new InvalidFeatureException(ppMatrix);
		}

		Map<String, Row> parameterPredictorRows = Iterables.getOnlyElement(ppMatrixMap.values());

		ParamMatrix paramMatrix = getParamMatrix();

		Map<String, List<PCell>> paramMatrixMap = parseParamMatrix(paramMatrix);
		if(paramMatrixMap.size() != 1 || !paramMatrixMap.containsKey(null)){
			throw new InvalidFeatureException(paramMatrix);
		}

		List<PCell> parameterCells = Iterables.getOnlyElement(paramMatrixMap.values());

		double dotProduct = 0d;

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

			dotProduct += (x.doubleValue() * parameterCell.getBeta());
		}

		Double result = Double.valueOf(dotProduct);

		GeneralRegressionModel.ModelType modelType = generalRegressionModel.getModelType();
		switch(modelType){
			case REGRESSION:
				break;
			case GENERAL_LINEAR:
			case GENERALIZED_LINEAR:
				result = computeRegressionResult(result, context);
				break;
			default:
				throw new UnsupportedFeatureException(generalRegressionModel, modelType);
		}

		return TargetUtil.evaluateRegression(result, context);
	}

	private Double computeRegressionResult(Double value, EvaluationContext context){
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

	static
	private Map<String, Map<String, Row>> parsePPMatrix(PPMatrix ppMatrix, final FactorList factorList, final CovariateList covariateList){
		Function<List<PPCell>, Row> rowBuilder = new Function<List<PPCell>, Row>(){

			private Map<FieldName, Predictor> factorRegistry = toPredictorRegistry(factorList);

			private Map<FieldName, Predictor> covariateRegistry = toPredictorRegistry(covariateList);


			@Override
			public Row apply(List<PPCell> ppCells){
				Row result = new Row();

				for(PPCell ppCell : ppCells){
					FieldName name = ppCell.getPredictorName();

					List<PPCell> predictorCells;

					if(isFactor(name)){
						predictorCells = result.getFactorCells();
					} else

					if(isCovariate(name)){
						predictorCells = result.getCovariateCells();
					} else

					{
						throw new InvalidFeatureException(ppCell);
					}

					predictorCells.add(ppCell);
				}

				return result;
			}

			private boolean isFactor(FieldName name){
				return this.factorRegistry.containsKey(name);
			}

			private boolean isCovariate(FieldName name){
				return this.covariateRegistry.containsKey(name);
			}
		};

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

	static
	private Map<String, List<PCell>> parseParamMatrix(ParamMatrix paramMatrix){
		ListMultimap<String, PCell> targetCategoryCells = groupByTargetCategory(paramMatrix.getPCells());

		return asMap(targetCategoryCells);
	}

	@SuppressWarnings (
		value = {"rawtypes", "unchecked"}
	)
	static
	private <C extends ParameterCell> Map<String, List<C>> asMap(ListMultimap<String, C> multimap){
		return (Map)multimap.asMap();
	}

	static
	private Map<FieldName, Predictor> toPredictorRegistry(PredictorList predictorList){
		Map<FieldName, Predictor> result = Maps.newLinkedHashMap();

		List<Predictor> predictors = predictorList.getPredictors();
		for(Predictor predictor : predictors){
			result.put(predictor.getName(), predictor);
		}

		return result;
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