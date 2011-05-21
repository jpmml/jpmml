/*
 * Copyright (c) 2011 University of Tartu
 */
package org.jpmml.manager;

import java.math.*;

public class PredicateUtil {

	private PredicateUtil(){
	}

	static
    public int compare(Object left, String right){

    	if(left instanceof Number){
    		return (new BigDecimal(String.valueOf(left))).compareTo(new BigDecimal(right));
    	} else

    	{
    		return (String.valueOf(left)).compareTo(right);
    	}
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