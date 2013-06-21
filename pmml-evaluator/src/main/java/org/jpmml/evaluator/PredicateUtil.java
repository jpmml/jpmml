/*
 * Copyright (c) 2011 University of Tartu
 */
package org.jpmml.evaluator;

import java.util.*;

import org.jpmml.manager.*;

import org.dmg.pmml.*;

public class PredicateUtil {

	private PredicateUtil(){
	}

	/**
	 * @return The {@link Boolean} value of the predicate, or <code>null</code> if the value is unknown
	 */
	static
	public Boolean evaluate(Predicate predicate, EvaluationContext context){

		if(predicate instanceof SimplePredicate){
			return evaluateSimplePredicate((SimplePredicate)predicate, context);
		} else

		if(predicate instanceof CompoundPredicate){
			return evaluateCompoundPredicate((CompoundPredicate)predicate, context);
		} else

		if(predicate instanceof SimpleSetPredicate){
			return evaluateSimpleSetPredicate((SimpleSetPredicate)predicate, context);
		} else

		if(predicate instanceof True){
			return evaluateTrue((True)predicate);
		} else

		if(predicate instanceof False){
			return evaluateFalse((False)predicate);
		} else

		{
			throw new UnsupportedFeatureException(predicate);
		}
	}

	static
	public Boolean evaluateSimplePredicate(SimplePredicate simplePredicate, EvaluationContext context){
		Object value = ExpressionUtil.evaluate(simplePredicate.getField(), context);

		SimplePredicate.Operator operator = simplePredicate.getOperator();
		switch(operator){
			case IS_MISSING:
				return Boolean.valueOf(value == null);
			case IS_NOT_MISSING:
				return Boolean.valueOf(value != null);
			default:
				break;
		}

		// "A SimplePredicate evaluates to unknwon if the input value is missing"
		if(value == null){
			return null;
		}

		int order = ParameterUtil.compare(value, simplePredicate.getValue());

		switch(operator){
			case EQUAL:
				return Boolean.valueOf(order == 0);
			case NOT_EQUAL:
				return Boolean.valueOf(order != 0);
			case LESS_THAN:
				return Boolean.valueOf(order < 0);
			case LESS_OR_EQUAL:
				return Boolean.valueOf(order <= 0);
			case GREATER_THAN:
				return Boolean.valueOf(order > 0);
			case GREATER_OR_EQUAL:
				return Boolean.valueOf(order >= 0);
			default:
				throw new UnsupportedFeatureException(simplePredicate, operator);
		}
	}

	static
	public Boolean evaluateCompoundPredicate(CompoundPredicate compoundPredicate, EvaluationContext context){
		List<Predicate> predicates = compoundPredicate.getContent();
		if(predicates.size() < 2){
			throw new InvalidFeatureException(compoundPredicate);
		}

		Boolean result = evaluate(predicates.get(0), context);

		CompoundPredicate.BooleanOperator booleanOperator = compoundPredicate.getBooleanOperator();
		switch(booleanOperator){
			case AND:
			case OR:
			case XOR:
				break;
			case SURROGATE:
				if(result != null){
					return result;
				}
				break;
			default:
				throw new UnsupportedFeatureException(compoundPredicate, booleanOperator);
		}

		predicates = predicates.subList(1, predicates.size());

		for(Predicate predicate : predicates){
			Boolean value = evaluate(predicate, context);

			switch(booleanOperator){
				case AND:
					result = PredicateUtil.binaryAnd(result, value);
					break;
				case OR:
					result = PredicateUtil.binaryOr(result, value);
					break;
				case XOR:
					result = PredicateUtil.binaryXor(result, value);
					break;
				case SURROGATE:
					if(value != null){
						return value;
					}
					break;
				default:
					throw new UnsupportedFeatureException(compoundPredicate, booleanOperator);
			}
		}

		return result;
	}

	static
	public Boolean evaluateSimpleSetPredicate(SimpleSetPredicate simpleSetPredicate, EvaluationContext context){
		Object value = ExpressionUtil.evaluate(simpleSetPredicate.getField(), context);
		if(value == null){
			throw new MissingParameterException(simpleSetPredicate.getField(), simpleSetPredicate);
		}

		Array array = simpleSetPredicate.getArray();

		SimpleSetPredicate.BooleanOperator booleanOperator = simpleSetPredicate.getBooleanOperator();
		switch(booleanOperator){
			case IS_IN:
				return ArrayUtil.isIn(array, value);
			case IS_NOT_IN:
				return ArrayUtil.isNotIn(array, value);
			default:
				throw new UnsupportedFeatureException(simpleSetPredicate, booleanOperator);
		}
	}

	static
	public Boolean evaluateTrue(True truePredicate){
		return Boolean.TRUE;
	}

	static
	public Boolean evaluateFalse(False falsePredicate){
		return Boolean.FALSE;
	}

	static
	public Boolean binaryAnd(Boolean left, Boolean right){

		if(left == null){

			if(right == null || right.booleanValue()){
				return null;
			} else {
				return Boolean.FALSE;
			}
		} else

		if(right == null){

			if(left == null || left.booleanValue()){
				return null;
			} else {
				return Boolean.FALSE;
			}
		} else

		{
			return Boolean.valueOf(left.booleanValue() & right.booleanValue());
		}
	}

	static
	public Boolean binaryOr(Boolean left, Boolean right){

		if(left != null && left.booleanValue()){
			return Boolean.TRUE;
		} else

		if(right != null && right.booleanValue()){
			return Boolean.TRUE;
		} else

		if(left == null || right == null){
			return null;
		} else

		{
			return Boolean.valueOf(left.booleanValue() | right.booleanValue());
		}
	}

	static
	public Boolean binaryXor(Boolean left, Boolean right){

		if(left == null || right == null){
			return null;
		} else

		{
			return Boolean.valueOf(left.booleanValue() ^ right.booleanValue());
		}
	}
}