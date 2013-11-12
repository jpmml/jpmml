/*
 * Copyright, KNIME.com AG, Zurich, Switzerland
 */
package org.jpmml.evaluator;

import java.util.*;
import java.util.concurrent.*;

import org.jpmml.manager.*;

import org.dmg.pmml.*;

import com.google.common.base.*;
import com.google.common.cache.*;
import com.google.common.collect.*;

public class NearestNeighborModelEvaluator extends ModelEvaluator<NearestNeighborModel> {

	public NearestNeighborModelEvaluator(PMML pmml){
		this(pmml, find(pmml.getModels(), NearestNeighborModel.class));
	}

	public NearestNeighborModelEvaluator(PMML pmml, NearestNeighborModel nearestNeighborModel){
		super(pmml, nearestNeighborModel);
	}

	@Override
	public String getSummary(){
		return "k-Nearest neighbors model";
	}

	@Override
	public Map<FieldName, ?> evaluate(Map<FieldName, ?> arguments){
		NearestNeighborModel nearestNeighborModel = getModel();
		if(!nearestNeighborModel.isScorable()){
			throw new InvalidResultException(nearestNeighborModel);
		}

		Map<FieldName, ?> predictions;

		ModelManagerEvaluationContext context = new ModelManagerEvaluationContext(this, arguments);

		MiningFunctionType miningFunction = nearestNeighborModel.getFunctionName();
		switch(miningFunction){
			// The model contains one or more continuous and/or categorical target(s)
			case REGRESSION:
			case CLASSIFICATION:
			case MIXED:
				predictions = evaluateMixed(context);
				break;
			// The model does not contain targets
			case CLUSTERING:
				predictions = evaluateClustering(context);
				break;
			default:
				throw new UnsupportedFeatureException(nearestNeighborModel, miningFunction);
		}

		return OutputUtil.evaluate(predictions, context);
	}

	private Map<FieldName, InstanceClassificationMap> evaluateMixed(ModelManagerEvaluationContext context){
		NearestNeighborModel nearestNeighborModel = getModel();

		Table<Integer, FieldName, FieldValue> table = getTrainingInstances();

		List<InstanceResult> instanceResults = evaluate(context);

		List<InstanceResult> nearestInstanceResults = Lists.newArrayList(instanceResults);

		Comparator<InstanceResult> comparator = new Comparator<InstanceResult>(){

			@Override
			public int compare(InstanceResult left, InstanceResult right){
				return -1 * (left).compareTo(right);
			}
		};
		Collections.sort(nearestInstanceResults, comparator);

		nearestInstanceResults = nearestInstanceResults.subList(0, nearestNeighborModel.getNumberOfNeighbors());

		Function<Integer, String> function = new Function<Integer, String>(){

			@Override
			public String apply(Integer row){
				return row.toString();
			}
		};

		String idField = nearestNeighborModel.getInstanceIdVariable();
		if(idField != null){
			function = createIdentifierResolver(FieldName.create(idField), table);
		}

		Map<FieldName, InstanceClassificationMap> result = Maps.newLinkedHashMap();

		List<FieldName> predictedFields = getPredictedFields();
		for(FieldName predictedField : predictedFields){
			DataField dataField = getDataField(predictedField);

			Object value;

			OpType opType = dataField.getOptype();
			switch(opType){
				case CONTINUOUS:
					value = calculateContinuousTarget(predictedField, nearestInstanceResults, table);
					break;
				case CATEGORICAL:
					value = calculateCategoricalTarget(predictedField, nearestInstanceResults, table);
					break;
				default:
					throw new UnsupportedFeatureException(dataField, opType);
			}

			result.put(predictedField, createMeasureMap(value, instanceResults, function));
		}

		return result;
	}

	private Map<FieldName, InstanceClassificationMap> evaluateClustering(ModelManagerEvaluationContext context){
		NearestNeighborModel nearestNeighborModel = getModel();

		Table<Integer, FieldName, FieldValue> table = getTrainingInstances();

		List<InstanceResult> instanceResults = evaluate(context);

		String idField = nearestNeighborModel.getInstanceIdVariable();
		if(idField == null){
			throw new InvalidFeatureException(nearestNeighborModel);
		}

		Function<Integer, String> function = createIdentifierResolver(FieldName.create(idField), table);

		return Collections.singletonMap(getTargetField(), createMeasureMap(null, instanceResults, function));
	}

	private List<InstanceResult> evaluate(ModelManagerEvaluationContext context){
		NearestNeighborModel nearestNeighborModel = getModel();

		List<FieldValue> values = Lists.newArrayList();

		KNNInputs knnInputs = nearestNeighborModel.getKNNInputs();
		for(KNNInput knnInput : knnInputs){
			FieldValue value = ExpressionUtil.evaluate(knnInput.getField(), context);

			values.add(value);
		}

		ComparisonMeasure comparisonMeasure = nearestNeighborModel.getComparisonMeasure();

		Measure measure = comparisonMeasure.getMeasure();

		if(MeasureUtil.isSimilarity(measure)){
			return evaluateSimilarity(comparisonMeasure, knnInputs.getKNNInputs(), values);
		} else

		if(MeasureUtil.isDistance(measure)){
			return evaluateDistance(comparisonMeasure, knnInputs.getKNNInputs(), values);
		} else

		{
			throw new UnsupportedFeatureException(measure);
		}
	}

	private List<InstanceResult> evaluateSimilarity(ComparisonMeasure comparisonMeasure, List<KNNInput> knnInputs, List<FieldValue> values){
		List<InstanceResult> result = Lists.newArrayList();

		BitSet flags = MeasureUtil.toBitSet(values);

		Map<Integer, BitSet> flagMap = getValue(NearestNeighborModelEvaluator.instanceFlagCache);

		Set<Integer> rowKeys = flagMap.keySet();
		for(Integer rowKey : rowKeys){
			BitSet instanceFlags = flagMap.get(rowKey);

			Double similarity = MeasureUtil.evaluateSimilarity(comparisonMeasure, knnInputs, flags, instanceFlags);

			result.add(new InstanceResult.Similarity(rowKey, similarity));
		}

		return result;
	}

	private List<InstanceResult> evaluateDistance(ComparisonMeasure comparisonMeasure, List<KNNInput> knnInputs, List<FieldValue> values){
		List<InstanceResult> result = Lists.newArrayList();

		Double adjustment = MeasureUtil.calculateAdjustment(values);

		Map<Integer, List<FieldValue>> valueMap = getValue(NearestNeighborModelEvaluator.instanceValueCache);

		Set<Integer> rowKeys = valueMap.keySet();
		for(Integer rowKey : rowKeys){
			List<FieldValue> instanceValues = valueMap.get(rowKey);

			Double distance = MeasureUtil.evaluateDistance(comparisonMeasure, knnInputs, values, instanceValues, adjustment);

			result.add(new InstanceResult.Distance(rowKey, distance));
		}

		return result;
	}

	private Double calculateContinuousTarget(FieldName name, List<InstanceResult> instanceResults, Table<Integer, FieldName, FieldValue> table){
		NearestNeighborModel nearestNeighborModel = getModel();

		double sum = 0d;

		ContinuousScoringMethodType continuousScoringMethod = nearestNeighborModel.getContinuousScoringMethod();

		for(InstanceResult instanceResult : instanceResults){
			FieldValue value = table.get(instanceResult.getId(), name);
			if(value == null){
				throw new MissingFieldException(name);
			}

			Number number = value.asNumber();

			switch(continuousScoringMethod){
				case AVERAGE:
					sum += number.doubleValue();
					break;
				case WEIGHTED_AVERAGE:
					sum += instanceResult.getWeight(nearestNeighborModel.getThreshold()) * number.doubleValue();
					break;
				default:
					throw new UnsupportedFeatureException(nearestNeighborModel, continuousScoringMethod);
			}
		}

		return (sum / instanceResults.size());
	}

	@SuppressWarnings (
		value = {"rawtypes", "unchecked"}
	)
	private Object calculateCategoricalTarget(FieldName name, List<InstanceResult> instanceResults, Table<Integer, FieldName, FieldValue> table){
		NearestNeighborModel nearestNeighborModel = getModel();

		VoteCounter<Object> counter = new VoteCounter<Object>();

		CategoricalScoringMethodType categoricalScoringMethod = nearestNeighborModel.getCategoricalScoringMethod();

		for(InstanceResult instanceResult : instanceResults){
			FieldValue value = table.get(instanceResult.getId(), name);
			if(value == null){
				throw new MissingFieldException(name);
			}

			Object object = value.getValue();

			switch(categoricalScoringMethod){
				case MAJORITY_VOTE:
					counter.increment(object);
					break;
				case WEIGHTED_MAJORITY_VOTE:
					counter.increment(object, instanceResult.getWeight(nearestNeighborModel.getThreshold()));
					break;
				default:
					throw new UnsupportedFeatureException(nearestNeighborModel, categoricalScoringMethod);
			}
		}

		Set<Object> winners = counter.getWinners();

		// "In case of a tie, the category with the largest number of cases in the training data is the winner"
		if(winners.size() > 1){
			Multiset<Object> multiset = LinkedHashMultiset.create();

			Map<Integer, FieldValue> column = table.column(name);

			Function<FieldValue, Object> function = new Function<FieldValue, Object>(){

				@Override
				public Object apply(FieldValue value){
					return value.getValue();
				}
			};
			multiset.addAll(Collections2.transform(column.values(), function));

			counter.clear();

			for(Object winner : winners){
				counter.increment(winner, (double)multiset.count(winner));
			}

			winners = counter.getWinners();

			// "If multiple categories are tied on the largest number of cases in the training data, then the category with the smallest data value (in lexical order) among the tied categories is the winner."
			if(winners.size() > 1){
				return Collections.min((Collection)winners);
			}
		}

		return Iterables.getFirst(winners, null);
	}

	private Function<Integer, String> createIdentifierResolver(final FieldName name, final Table<Integer, FieldName, FieldValue> table){
		Function<Integer, String> function = new Function<Integer, String>(){

			@Override
			public String apply(Integer row){
				FieldValue value = table.get(row, name);
				if(value == null){
					throw new MissingFieldException(name);
				}

				return value.asString();
			}
		};

		return function;
	}

	private InstanceClassificationMap createMeasureMap(Object value, List<InstanceResult> instanceResults, Function<Integer, String> function){
		NearestNeighborModel nearestNeighborModel = getModel();

		InstanceClassificationMap result;

		ComparisonMeasure comparisonMeasure = nearestNeighborModel.getComparisonMeasure();

		Measure measure = comparisonMeasure.getMeasure();

		if(MeasureUtil.isSimilarity(measure)){
			result = new InstanceClassificationMap(ClassificationMap.Type.SIMILARITY, value);
		} else

		if(MeasureUtil.isDistance(measure)){
			result = new InstanceClassificationMap(ClassificationMap.Type.DISTANCE, value);
		} else

		{
			throw new UnsupportedFeatureException(measure);
		}

		for(InstanceResult instanceResult : instanceResults){
			result.put(function.apply(instanceResult.getId()), instanceResult.getValue());
		}

		return result;
	}

	private Table<Integer, FieldName, FieldValue> getTrainingInstances(){
		NearestNeighborModel nearestNeighborModel = getModel();

		try {
			Callable<PMML> callable = new Callable<PMML>(){

				@Override
				public PMML call(){
					return getPMML();
				}
			};

			NearestNeighborModelEvaluator.pmmlCache.get(nearestNeighborModel, callable);
		} catch(ExecutionException ee){
			throw new EvaluationException();
		}

		return getValue(NearestNeighborModelEvaluator.trainingInstanceCache);
	}

	static
	private Table<Integer, FieldName, FieldValue> parseTrainingInstances(PMML pmml, NearestNeighborModel nearestNeighborModel){
		TrainingInstances trainingInstances = nearestNeighborModel.getTrainingInstances();

		TableLocator tableLocator = trainingInstances.getTableLocator();
		if(tableLocator != null){
			throw new UnsupportedFeatureException(tableLocator);
		}

		ModelManager<NearestNeighborModel> modelManager = new ModelManager<NearestNeighborModel>(pmml, nearestNeighborModel);

		String idField = nearestNeighborModel.getInstanceIdVariable();

		List<FieldLoader> fieldLoaders = Lists.newArrayList();

		InstanceFields instanceFields = trainingInstances.getInstanceFields();
		for(InstanceField instanceField : instanceFields){
			String field = instanceField.getField();
			String column = instanceField.getColumn();

			FieldName name = FieldName.create(field);

			if(idField != null && (idField).equals(field)){
				fieldLoaders.add(new IdentifierLoader(name, column));

				continue;
			}

			DataField dataField = modelManager.getDataField(name);
			MiningField miningField = modelManager.getMiningField(name);

			if(dataField != null && miningField != null){
				fieldLoaders.add(new DataFieldLoader(name, column, dataField, miningField));

				continue;
			}

			DerivedField derivedField = modelManager.resolveField(name);
			if(derivedField != null){
				fieldLoaders.add(new DerivedFieldLoader(name, column, derivedField));

				continue;
			}

			throw new InvalidFeatureException(instanceField);
		}

		Table<Integer, FieldName, FieldValue> result = HashBasedTable.create();

		InlineTable inlineTable = trainingInstances.getInlineTable();
		if(inlineTable != null){
			Table<Integer, String, String> table = InlineTableUtil.getContent(inlineTable);

			Set<Integer> rowKeys = table.rowKeySet();
			for(Integer rowKey : rowKeys){
				Map<String, String> rowValues = table.row(rowKey);

				for(FieldLoader fieldLoader : fieldLoaders){
					result.put(rowKey, fieldLoader.getName(), fieldLoader.load(rowValues));
				}
			}
		}


		KNNInputs knnInputs = nearestNeighborModel.getKNNInputs();
		for(KNNInput knnInput : knnInputs){
			FieldName name = knnInput.getField();

			DerivedField derivedField = modelManager.resolveField(name);
			if(derivedField == null){
				continue;
			}

			Set<Integer> rowKeys = result.rowKeySet();
			for(Integer rowKey : rowKeys){
				Map<FieldName, FieldValue> rowValues = result.row(rowKey);

				if(rowValues.containsKey(name)){
					continue;
				}

				ModelManagerEvaluationContext context = new ModelManagerEvaluationContext(modelManager, rowValues);

				try {
					result.put(rowKey, name, ExpressionUtil.evaluate(derivedField, context));
				} finally {
					context.popFrame();
				}
			}
		}

		return result;
	}

	static
	abstract
	private class FieldLoader {

		private FieldName name = null;

		private String column = null;


		private FieldLoader(FieldName name, String column){
			setName(name);
			setColumn(column);
		}

		abstract
		public FieldValue prepare(String value);

		public FieldValue load(Map<String, String> values){
			String value = values.get(getColumn());

			return prepare(value);
		}

		public FieldName getName(){
			return this.name;
		}

		private void setName(FieldName name){
			this.name = name;
		}

		public String getColumn(){
			return this.column;
		}

		private void setColumn(String column){
			this.column = column;
		}
	}

	static
	private class IdentifierLoader extends FieldLoader {

		private IdentifierLoader(FieldName name, String column){
			super(name, column);
		}

		@Override
		public FieldValue prepare(String value){
			return FieldValueUtil.create(DataType.STRING, OpType.CATEGORICAL, value);
		}
	}

	static
	private class DataFieldLoader extends FieldLoader {

		private DataField dataField = null;

		private MiningField miningField = null;


		private DataFieldLoader(FieldName name, String column, DataField dataField, MiningField miningField){
			super(name, column);

			setDataField(dataField);
			setMiningField(miningField);
		}

		@Override
		public FieldValue prepare(String value){
			return ArgumentUtil.prepare(getDataField(), getMiningField(), value);
		}

		public DataField getDataField(){
			return this.dataField;
		}

		private void setDataField(DataField dataField){
			this.dataField = dataField;
		}

		public MiningField getMiningField(){
			return this.miningField;
		}

		private void setMiningField(MiningField miningField){
			this.miningField = miningField;
		}
	}

	static
	private class DerivedFieldLoader extends FieldLoader {

		private DerivedField derivedField = null;


		private DerivedFieldLoader(FieldName name, String column, DerivedField derivedField){
			super(name, column);

			setDerivedField(derivedField);
		}

		@Override
		public FieldValue prepare(String value){
			return FieldValueUtil.create(getDerivedField(), value);
		}

		public DerivedField getDerivedField(){
			return this.derivedField;
		}

		private void setDerivedField(DerivedField derivedField){
			this.derivedField = derivedField;
		}
	}

	static
	abstract
	private class InstanceResult implements Comparable<InstanceResult> {

		private Integer id = null;

		private Double value = null;


		private InstanceResult(Integer id, Double value){
			setId(id);
			setValue(value);
		}

		abstract
		public double getWeight(double threshold);

		public Integer getId(){
			return this.id;
		}

		private void setId(Integer id){
			this.id = id;
		}

		public Double getValue(){
			return this.value;
		}

		private void setValue(Double value){
			this.value = value;
		}

		static
		private class Similarity extends InstanceResult {

			private Similarity(Integer id, Double value){
				super(id, value);
			}

			@Override
			public int compareTo(InstanceResult that){

				if(that instanceof Similarity){
					return ClassificationMap.Type.SIMILARITY.compare(this.getValue(), that.getValue());
				}

				throw new ClassCastException();
			}

			@Override
			public double getWeight(double threshold){
				throw new EvaluationException();
			}
		}

		static
		private class Distance extends InstanceResult {

			private Distance(Integer id, Double value){
				super(id, value);
			}

			@Override
			public int compareTo(InstanceResult that){

				if(that instanceof Distance){
					return ClassificationMap.Type.DISTANCE.compare(this.getValue(), that.getValue());
				}

				throw new ClassCastException();
			}

			@Override
			public double getWeight(double threshold){
				return 1d / (getValue() + threshold);
			}
		}
	}

	private static final Cache<NearestNeighborModel, PMML> pmmlCache = CacheBuilder.newBuilder()
		.weakKeys()
		.weakValues()
		.build();

	private static final LoadingCache<NearestNeighborModel, Table<Integer, FieldName, FieldValue>> trainingInstanceCache = CacheBuilder.newBuilder()
		.weakKeys()
		.build(new CacheLoader<NearestNeighborModel, Table<Integer, FieldName, FieldValue>>(){

			@Override
			public Table<Integer, FieldName, FieldValue> load(NearestNeighborModel nearestNeighborModel){
				PMML pmml = NearestNeighborModelEvaluator.pmmlCache.getIfPresent(nearestNeighborModel);
				if(pmml == null){
					throw new EvaluationException();
				}

				return parseTrainingInstances(pmml, nearestNeighborModel);
			}
		});

	private static final LoadingCache<NearestNeighborModel, Map<Integer, List<FieldValue>>> instanceValueCache = CacheBuilder.newBuilder()
		.weakKeys()
		.build(new CacheLoader<NearestNeighborModel, Map<Integer, List<FieldValue>>>(){

			@Override
			public Map<Integer, List<FieldValue>> load(NearestNeighborModel nearestNeighborModel){
				Map<Integer, List<FieldValue>> result = Maps.newLinkedHashMap();

				Table<Integer, FieldName, FieldValue> table = CacheUtil.getValue(nearestNeighborModel, NearestNeighborModelEvaluator.trainingInstanceCache);

				KNNInputs knnInputs = nearestNeighborModel.getKNNInputs();

				Set<Integer> rowKeys = ImmutableSortedSet.copyOf(table.rowKeySet());
				for(Integer rowKey : rowKeys){
					List<FieldValue> values = Lists.newArrayList();

					Map<FieldName, FieldValue> rowValues = table.row(rowKey);

					for(KNNInput knnInput : knnInputs){
						FieldValue value = rowValues.get(knnInput.getField());

						values.add(value);
					}

					result.put(rowKey, values);
				}

				return result;
			}
		});

	private static final LoadingCache<NearestNeighborModel, Map<Integer, BitSet>> instanceFlagCache = CacheBuilder.newBuilder()
		.weakKeys()
		.build(new CacheLoader<NearestNeighborModel, Map<Integer, BitSet>>(){

			@Override
			public Map<Integer, BitSet> load(NearestNeighborModel nearestNeighborModel){
				Map<Integer, BitSet> result = Maps.newLinkedHashMap();

				Map<Integer, List<FieldValue>> valueMap = CacheUtil.getValue(nearestNeighborModel, NearestNeighborModelEvaluator.instanceValueCache);

				Maps.EntryTransformer<Integer, List<FieldValue>, BitSet> transformer = new Maps.EntryTransformer<Integer, List<FieldValue>, BitSet>(){

					@Override
					public BitSet transformEntry(Integer key, List<FieldValue> value){
						return MeasureUtil.toBitSet(value);
					}
				};
				result.putAll(Maps.transformEntries(valueMap, transformer));

				return result;
			}
		});
}