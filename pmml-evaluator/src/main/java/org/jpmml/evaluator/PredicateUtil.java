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

		switch(simplePredicate.getOperator()){
			case IS_MISSING:
				return Boolean.valueOf(value == null);
			case IS_NOT_MISSING:
				return Boolean.valueOf(value != null);
			default:
				break;
		}

		if(value == null){
			return null;
		}

		int order = ParameterUtil.compare(value, simplePredicate.getValue());

		SimplePredicate.Operator operator = simplePredicate.getOperator();
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
				throw new UnsupportedFeatureException(operator);
		}
	}

	static
	public Boolean evaluateCompoundPredicate(CompoundPredicate compoundPredicate, EvaluationContext context){
		List<Predicate> predicates = compoundPredicate.getContent();

		Boolean result = evaluate(predicates.get(0), context);

		switch(compoundPredicate.getBooleanOperator()){
			case AND:
			case OR:
			case XOR:
				break;
			case SURROGATE:
				if(result != null){
					return result;
				}
				break;
		}

		for(Predicate predicate : predicates.subList(1, predicates.size())){
			Boolean value = evaluate(predicate, context);

			switch(compoundPredicate.getBooleanOperator()){
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
			}
		}

		return result;
	}

	static
	public Boolean evaluateSimpleSetPredicate(SimpleSetPredicate simpleSetPredicate, EvaluationContext context){
		Object value = ExpressionUtil.evaluate(simpleSetPredicate.getField(), context);
		if(value == null){
			throw new MissingParameterException(simpleSetPredicate.getField());
		}

		Array array = simpleSetPredicate.getArray();

		SimpleSetPredicate.BooleanOperator operator = simpleSetPredicate.getBooleanOperator();
		switch(operator){
			case IS_IN:
				return ArrayUtil.isIn(array, value);
			case IS_NOT_IN:
				return ArrayUtil.isNotIn(array, value);
			default:
				throw new UnsupportedFeatureException(operator);
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