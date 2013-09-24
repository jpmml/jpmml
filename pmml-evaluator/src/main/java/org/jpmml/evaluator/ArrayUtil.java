/*
 * Copyright (c) 2012 University of Tartu
 */
package org.jpmml.evaluator;

import java.util.*;

import org.jpmml.manager.*;

import org.dmg.pmml.*;

import com.google.common.base.*;
import com.google.common.collect.*;

public class ArrayUtil {

	private ArrayUtil(){
	}

	static
	public int getSize(Array array){
		Integer n = array.getN();
		if(n != null){
			return n.intValue();
		}

		List<String> context = getContent(array);

		return context.size();
	}

	static
	public List<String> getContent(Array array){
		List<String> content = array.getContent();

		if(content == null){
			content = parse(array);

			array.setContent(content);
		}

		return content;
	}

	static
	public List<? extends Number> getNumberContent(Array array){
		Array.Type type = array.getType();

		switch(type){
			case INT:
				return getIntContent(array);
			case REAL:
				return getRealContent(array);
			default:
				break;
		}

		throw new TypeCheckException(Number.class, null);
	}

	static
	public List<Integer> getIntContent(Array array){
		Function<String, Integer> transformer = new Function<String, Integer>(){

			@Override
			public Integer apply(String string){
				return Integer.valueOf(string);
			}
		};

		return Lists.transform(getContent(array), transformer);
	}

	static
	public List<Double> getRealContent(Array array){
		Function<String, Double> transformer = new Function<String, Double>(){

			@Override
			public Double apply(String string){
				return Double.valueOf(string);
			}
		};

		return Lists.transform(getContent(array), transformer);
	}

	static
	public List<String> parse(Array array){
		List<String> result;

		Array.Type type = array.getType();
		switch(type){
			case INT:
			case REAL:
				result = tokenize(array.getValue(), false);
				break;
			case STRING:
				result = tokenize(array.getValue(), true);
				break;
			default:
				throw new UnsupportedFeatureException(array, type);
		}

		Integer n = array.getN();
		if(n != null && n.intValue() != result.size()){
			throw new InvalidFeatureException(array);
		}

		return result;
	}

	static
	public List<String> tokenize(String string, boolean enableQuotes){
		List<String> result = Lists.newArrayList();

		StringBuilder sb = new StringBuilder();

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

		return Collections.unmodifiableList(result);
	}

	static
	private String createToken(StringBuilder sb, boolean enableQuotes){
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