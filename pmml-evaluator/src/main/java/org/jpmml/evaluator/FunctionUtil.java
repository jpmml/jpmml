/*
 * Copyright (c) 2013 University of Tartu
 */
package org.jpmml.evaluator;

import java.util.*;

import org.jpmml.manager.*;

public class FunctionUtil {

	private FunctionUtil(){
	}

	static
	public Object evaluate(String name, List<?> values){
		Function function = getFunction(name);
		if(function == null){
			throw new UnsupportedFeatureException(name);
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

	private static final Map<String, Function> functions = new LinkedHashMap<String, Function>();

	public interface Function {

		Object evaluate(List<?> values);
	}

	static
	abstract
	public class ArithmeticFunction implements Function {

		abstract
		public Number evaluate(Number left, Number right);

		public Number evaluate(List<?> values){

			if(values.size() != 2){
				throw new EvaluationException();
			}

			Object left = values.get(0);
			Object right = values.get(1);

			if(left == null || right == null){
				return null;
			}

			return evaluate((Number)left, (Number)right);
		}
	}

	static {
		putFunction("+", new ArithmeticFunction(){

			@Override
			public Number evaluate(Number left, Number right){
				return Double.valueOf(left.doubleValue() + right.doubleValue());
			}
		});

		putFunction("-", new ArithmeticFunction(){

			@Override
			public Number evaluate(Number left, Number right){
				return Double.valueOf(left.doubleValue() - right.doubleValue());
			}
		});

		putFunction("*", new ArithmeticFunction(){

			@Override
			public Number evaluate(Number left, Number right){
				return Double.valueOf(left.doubleValue() * right.doubleValue());
			}
		});

		putFunction("/", new ArithmeticFunction(){

			@Override
			public Number evaluate(Number left, Number right){
				return Double.valueOf(left.doubleValue() / right.doubleValue());
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

			// XXX
			if(!(left.getClass()).equals(right.getClass())){
				throw new EvaluationException();
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

	static {
		putFunction("if", new Function(){

			public Object evaluate(List<?> values){

				if(values.size() < 2 || values.size() > 3){
					throw new EvaluationException();
				}

				Boolean flag = (Boolean)values.get(0);

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
}