/*
 * Copyright (c) 2013 University of Tartu
 */
package org.jpmml.evaluator;

import java.util.*;

import org.jpmml.manager.*;

import org.apache.commons.math3.stat.descriptive.*;
import org.apache.commons.math3.stat.descriptive.moment.*;
import org.apache.commons.math3.stat.descriptive.rank.*;
import org.apache.commons.math3.stat.descriptive.summary.*;

import org.dmg.pmml.*;

public class FunctionUtil {

	private FunctionUtil(){
	}

	static
	public Object evaluate(Apply apply, List<?> values){
		Function function = getFunction(apply.getFunction());
		if(function == null){
			throw new UnsupportedFeatureException(apply);
		}

		return function.evaluate(values);
	}

	static
	public Function getFunction(String name){
		return FunctionUtil.functions.get(name);
	}

	static
	public void putFunction(String name, Function function){
		FunctionUtil.functions.put(name, function);
	}

	static
	private Boolean asBoolean(Object value){

		if(value instanceof Boolean){
			return (Boolean)value;
		}

		throw new EvaluationException();
	}

	static
	private Number asNumber(Object value){

		if(value instanceof Number){
			return (Number)value;
		}

		throw new EvaluationException();
	}

	static
	private Integer asInteger(Object value){

		if(value instanceof Integer){
			return (Integer)value;
		}

		throw new EvaluationException();
	}

	static
	private String asString(Object value){

		if(value instanceof String){
			return (String)value;
		}

		throw new EvaluationException();
	}

	static
	private DataType integerToDouble(DataType dataType){

		if((DataType.INTEGER).equals(dataType)){
			return DataType.DOUBLE;
		}

		return dataType;
	}

	private static final Map<String, Function> functions = new LinkedHashMap<String, Function>();

	public interface Function {

		Object evaluate(List<?> values);
	}

	static
	abstract
	public class ArithmeticFunction implements Function {

		abstract
		public Double evaluate(Number left, Number right);

		public Number cast(DataType dataType, Double result){
			return asNumber(ParameterUtil.cast(dataType, result));
		}

		public Number evaluate(List<?> values){

			if(values.size() != 2){
				throw new EvaluationException();
			}

			Object left = values.get(0);
			Object right = values.get(1);

			if(left == null || right == null){
				return null;
			}

			DataType dataType = ParameterUtil.getResultDataType(left, right);

			Double result;

			try {
				result = evaluate(asNumber(left), asNumber(right));
			} catch(ArithmeticException ae){
				throw new InvalidResultException(null);
			}

			return cast(dataType, result);
		}
	}

	static {
		putFunction("+", new ArithmeticFunction(){

			@Override
			public Double evaluate(Number left, Number right){
				return Double.valueOf(left.doubleValue() + right.doubleValue());
			}
		});

		putFunction("-", new ArithmeticFunction(){

			@Override
			public Double evaluate(Number left, Number right){
				return Double.valueOf(left.doubleValue() - right.doubleValue());
			}
		});

		putFunction("*", new ArithmeticFunction(){

			@Override
			public Double evaluate(Number left, Number right){
				return Double.valueOf(left.doubleValue() * right.doubleValue());
			}
		});

		putFunction("/", new ArithmeticFunction(){

			@Override
			public Number cast(DataType dataType, Double result){
				return super.cast(integerToDouble(dataType), result);
			}

			@Override
			public Double evaluate(Number left, Number right){
				return Double.valueOf(left.doubleValue() / right.doubleValue());
			}
		});
	}

	static
	abstract
	public class AggregateFunction implements Function {

		abstract
		public StorelessUnivariateStatistic createStatistic();

		public Number cast(DataType dataType, Double result){
			return asNumber(ParameterUtil.cast(dataType, result));
		}

		public Number evaluate(List<?> values){
			StorelessUnivariateStatistic statistic = createStatistic();

			DataType dataType = null;

			for(Object value : values){

				if(value == null){
					continue;
				}

				statistic.increment(asNumber(value).doubleValue());

				if(dataType != null){
					dataType = ParameterUtil.getResultDataType(dataType, ParameterUtil.getDataType(value));
				} else

				{
					dataType = ParameterUtil.getDataType(value);
				}
			}

			if(statistic.getN() == 0){
				throw new EvaluationException();
			}

			return cast(dataType, statistic.getResult());
		}
	}

	static {
		putFunction("min", new AggregateFunction(){

			@Override
			public Min createStatistic(){
				return new Min();
			}
		});

		putFunction("max", new AggregateFunction(){

			@Override
			public Max createStatistic(){
				return new Max();
			}
		});

		putFunction("avg", new AggregateFunction(){

			@Override
			public Mean createStatistic(){
				return new Mean();
			}

			@Override
			public Number cast(DataType dataType, Double result){
				return super.cast(integerToDouble(dataType), result);
			}
		});

		putFunction("sum", new AggregateFunction(){

			@Override
			public Sum createStatistic(){
				return new Sum();
			}
		});

		putFunction("product", new AggregateFunction(){

			@Override
			public Product createStatistic(){
				return new Product();
			}
		});
	}

	static
	abstract
	public class MathFunction implements Function {

		abstract
		public Double evaluate(Number value);

		public Number cast(DataType dataType, Number result){
			return asNumber(ParameterUtil.cast(dataType, result));
		}

		public Number evaluate(List<?> values){

			if(values.size() != 1){
				throw new EvaluationException();
			}

			Object value = values.get(0);

			DataType dataType = ParameterUtil.getDataType(value);

			return cast(dataType, evaluate(asNumber(value)));
		}
	}

	static
	abstract
	public class FpMathFunction extends MathFunction {

		@Override
		public Number cast(DataType dataType, Number result){
			return super.cast(integerToDouble(dataType), result);
		}
	}

	static {
		putFunction("log10", new FpMathFunction(){

			@Override
			public Double evaluate(Number value){
				return Math.log10(value.doubleValue());
			}
		});

		putFunction("ln", new FpMathFunction(){

			@Override
			public Double evaluate(Number value){
				return Math.log(value.doubleValue());
			}
		});

		putFunction("exp", new FpMathFunction(){

			@Override
			public Double evaluate(Number value){
				return Math.exp(value.doubleValue());
			}
		});

		putFunction("sqrt", new FpMathFunction(){

			@Override
			public Double evaluate(Number value){
				return Math.sqrt(value.doubleValue());
			}
		});

		putFunction("abs", new MathFunction(){

			@Override
			public Double evaluate(Number value){
				return Math.abs(value.doubleValue());
			}
		});

		putFunction("pow", new Function(){

			public Number evaluate(List<?> values){

				if(values.size() != 2){
					throw new EvaluationException();
				}

				Number left = asNumber(values.get(0));
				Number right = asNumber(values.get(1));

				DataType dataType = ParameterUtil.getResultDataType(left, right);

				Double result = Math.pow(left.doubleValue(), right.doubleValue());

				return asNumber(ParameterUtil.cast(dataType, result));
			}
		});

		putFunction("threshold", new Function(){

			public Number evaluate(List<?> values){

				if(values.size() != 2){
					throw new EvaluationException();
				}

				Number left = asNumber(values.get(0));
				Number right = asNumber(values.get(1));

				DataType dataType = ParameterUtil.getResultDataType(left, right);

				Integer result = (left.doubleValue() > right.doubleValue()) ? 1 : 0;

				return asNumber(ParameterUtil.cast(dataType, result));
			}
		});

		putFunction("floor", new MathFunction(){

			@Override
			public Double evaluate(Number number){
				return Math.floor(number.doubleValue());
			}
		});

		putFunction("ceil", new MathFunction(){

			@Override
			public Double evaluate(Number number){
				return Math.ceil(number.doubleValue());
			}
		});

		putFunction("round", new MathFunction(){

			@Override
			public Double evaluate(Number number){
				return (double)Math.round(number.doubleValue());
			}
		});
	}

	static
	abstract
	public class ValueFunction implements Function {

		abstract
		public Boolean evaluate(Object value);

		public Boolean evaluate(List<?> values){

			if(values.size() != 1){
				throw new EvaluationException();
			}

			return evaluate(values.get(0));
		}
	}

	static {
		putFunction("isMissing", new ValueFunction(){

			@Override
			public Boolean evaluate(Object value){
				return Boolean.valueOf(value == null);
			}
		});

		putFunction("isNotMissing", new ValueFunction(){

			@Override
			public Boolean evaluate(Object value){
				return Boolean.valueOf(value != null);
			}
		});
	}

	static
	abstract
	public class ComparisonFunction implements Function {

		abstract
		public Boolean evaluate(int diff);

		public <C extends Comparable<C>> Boolean evaluate(C left, C right){
			return evaluate((left).compareTo(right));
		}

		@SuppressWarnings (
			value = {"rawtypes", "unchecked"}
		)
		public Boolean evaluate(List<?> values){

			if(values.size() != 2){
				throw new EvaluationException();
			}

			Object left = values.get(0);
			Object right = values.get(1);

			if(left == null || right == null){
				throw new EvaluationException();
			} // End if

			// Cast operands to common data type before comparison
			if(!(left.getClass()).equals(right.getClass())){
				DataType dataType = ParameterUtil.getResultDataType(left, right);

				left = ParameterUtil.cast(dataType, left);
				right = ParameterUtil.cast(dataType, right);
			}

			return evaluate((Comparable)left, (Comparable)right);
		}
	}

	static {
		putFunction("equal", new ComparisonFunction(){

			@Override
			public Boolean evaluate(int diff){
				return Boolean.valueOf(diff == 0);
			}
		});

		putFunction("notEqual", new ComparisonFunction(){

			@Override
			public Boolean evaluate(int diff){
				return Boolean.valueOf(diff != 0);
			}
		});

		putFunction("lessThan", new ComparisonFunction(){

			@Override
			public Boolean evaluate(int diff){
				return Boolean.valueOf(diff < 0);
			}
		});

		putFunction("lessOrEqual", new ComparisonFunction(){

			@Override
			public Boolean evaluate(int diff){
				return Boolean.valueOf(diff <= 0);
			}
		});

		putFunction("greaterThan", new ComparisonFunction(){

			@Override
			public Boolean evaluate(int diff){
				return Boolean.valueOf(diff > 0);
			}
		});

		putFunction("greaterOrEqual", new ComparisonFunction(){

			@Override
			public Boolean evaluate(int diff){
				return Boolean.valueOf(diff >= 0);
			}
		});
	}

	static
	abstract
	public class BinaryBooleanFunction implements Function {

		abstract
		public Boolean evaluate(Boolean left, Boolean right);

		public Boolean evaluate(List<?> values){

			if(values.size() < 2){
				throw new EvaluationException();
			}

			Boolean result = asBoolean(values.get(0));

			for(int i = 1; i < values.size(); i++){
				result = evaluate(result, asBoolean(values.get(i)));
			}

			return result;
		}
	}

	static {
		putFunction("and", new BinaryBooleanFunction(){

			@Override
			public Boolean evaluate(Boolean left, Boolean right){
				return Boolean.valueOf(left.booleanValue() & right.booleanValue());
			}
		});

		putFunction("or", new BinaryBooleanFunction(){

			@Override
			public Boolean evaluate(Boolean left, Boolean right){
				return Boolean.valueOf(left.booleanValue() | right.booleanValue());
			}
		});
	}

	static
	abstract
	public class UnaryBooleanFunction implements Function {

		abstract
		public Boolean evaluate(Boolean value);

		public Boolean evaluate(List<?> values){

			if(values.size() != 1){
				throw new EvaluationException();
			}

			return evaluate(asBoolean(values.get(0)));
		}
	}

	static {
		putFunction("not", new UnaryBooleanFunction(){

			@Override
			public Boolean evaluate(Boolean value){
				return Boolean.valueOf(!value.booleanValue());
			}
		});
	}

	static
	abstract
	public class ValueListFunction implements Function {

		abstract
		public Boolean evaluate(Object value, List<?> values);

		public Boolean evaluate(List<?> values){

			if(values.size() < 2){
				throw new EvaluationException();
			}

			return evaluate(values.get(0), values.subList(1, values.size()));
		}
	}

	static {
		putFunction("isIn", new ValueListFunction(){

			@Override
			public Boolean evaluate(Object value, List<?> values){
				return Boolean.valueOf(values.contains(value));
			}
		});

		putFunction("isNotIn", new ValueListFunction(){

			@Override
			public Boolean evaluate(Object value, List<?> values){
				return Boolean.valueOf(!values.contains(value));
			}
		});
	}

	static {
		putFunction("if", new Function(){

			public Object evaluate(List<?> values){

				if(values.size() < 2 || values.size() > 3){
					throw new EvaluationException();
				}

				Boolean flag = asBoolean(values.get(0));

				if(flag.booleanValue()){
					return values.get(1);
				} else

				{
					if(values.size() > 2){
						return values.get(2);
					}

					// XXX
					return null;
				}
			}
		});
	}

	static
	abstract
	public class StringFunction implements Function {

		abstract
		public String evaluate(String value);

		public String evaluate(List<?> values){

			if(values.size() != 1){
				throw new EvaluationException();
			}

			return evaluate(asString(values.get(0)));
		}
	}

	static {
		putFunction("uppercase", new StringFunction(){

			@Override
			public String evaluate(String value){
				return value.toUpperCase();
			}
		});

		putFunction("lowercase", new StringFunction(){

			@Override
			public String evaluate(String value){
				return value.toLowerCase();
			}
		});

		putFunction("substring", new Function(){

			public String evaluate(List<?> values){

				if(values.size() != 3){
					throw new EvaluationException();
				}

				String value = asString(values.get(0));

				int position = asInteger(values.get(1));
				int length = asInteger(values.get(2));

				if(position <= 0 || length < 0){
					throw new EvaluationException();
				}

				return value.substring(position - 1, (position + length) - 1);
			}
		});

		putFunction("trimBlanks", new StringFunction(){

			@Override
			public String evaluate(String value){
				return value.trim();
			}
		});
	}
}