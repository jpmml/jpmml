/*
 * Copyright (c) 2012 University of Tartu
 */
package org.jpmml.evaluator;

import java.util.*;

import org.jpmml.manager.*;

import org.dmg.pmml.*;

public class ArrayUtil {

	private ArrayUtil(){
	}

	static
	public Boolean isIn(ArrayType array, Object value){
		List<String> values = tokenize(array);

		boolean result = values.contains(String.valueOf(value));

		return Boolean.valueOf(result);
	}

	static
	public Boolean isNotIn(ArrayType array, Object value){
		List<String> values = tokenize(array);

		boolean result = !values.contains(String.valueOf(value));

		return Boolean.valueOf(result);
	}

	static
	public List<String> tokenize(ArrayType array){
		List<String> result;

		ArrayType.Type type = array.getType();
		switch(type){
			case INT:
			case REAL:
				result = tokenize(array.getContent(), false);
				break;
			case STRING:
				result = tokenize(array.getContent(), true);
				break;
			default:
				throw new UnsupportedFeatureException(type);
		}

		Number n = array.getN();
		if(n != null && n.intValue() != result.size()){
			throw new EvaluationException();
		}

		return result;
	}

	static
	private List<String> tokenize(String string, boolean enableQuotes){
		List<String> result = new ArrayList<String>();

		StringBuffer sb = new StringBuffer();

		boolean quoted = false;

		tokens:
		for(int i = 0; i < string.length(); i++){
			char c = string.charAt(i);

			if(quoted){

				if(c == '\\' && i < (string.length() - 1)){
					c = string.charAt(i + 1);

					if(c == '\"'){
						sb.append('\"');

						i++;
					} else

					{
						sb.append('\\');
					}

					continue tokens;
				} // End if

				sb.append(c);

				if(c == '\"'){
					result.add(createToken(sb, enableQuotes));

					quoted = false;
				}
			} else

			{
				if(c == '\"' && enableQuotes){

					if(sb.length() > 0){
						result.add(createToken(sb, enableQuotes));
					}

					sb.append('\"');

					quoted = true;
				} else

				if(Character.isWhitespace(c)){

					if(sb.length() > 0){
						result.add(createToken(sb, enableQuotes));
					}
				} else

				{
					sb.append(c);
				}
			}
		}

		if(sb.length() > 0){
			result.add(createToken(sb, enableQuotes));
		}

		return result;
	}

	static
	private String createToken(StringBuffer sb, boolean enableQuotes){
		String result;

		if(sb.length() > 1 && (sb.charAt(0) == '\"' && sb.charAt(sb.length() - 1) == '\"') && enableQuotes){
			result = sb.substring(1, sb.length() - 1);
		} else

		{
			result = sb.substring(0, sb.length());
		}

		sb.setLength(0);

		return result;
	}
}