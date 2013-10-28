/*
 * Copyright (c) 2013 University of Tartu
 */
package org.jpmml.evaluator;

import java.io.*;
import java.util.*;

import org.dmg.pmml.*;

import com.google.common.collect.*;

abstract
public class PMMLTest {

	static
	public PMML loadPMML(Class<? extends PMMLTest> clazz) throws Exception {
		InputStream is = clazz.getResourceAsStream("/pmml/" + clazz.getSimpleName() + ".pmml");

		try {
			return IOUtil.unmarshal(is);
		} finally {
			is.close();
		}
	}

	static
	public Map<FieldName, ?> createArguments(Object... objects){
		Map<FieldName, Object> result = Maps.newLinkedHashMap();

		if(objects.length % 2 != 0){
			throw new IllegalArgumentException();
		}

		for(int i = 0; i < objects.length / 2; i++){
			Object key = objects[i * 2];
			Object value = objects[i * 2 + 1];

			result.put(toFieldName(key), value);
		}

		return result;
	}

	static
	private FieldName toFieldName(Object object){

		if(object instanceof String){
			String string = (String)object;

			return FieldName.create(string);
		}

		return (FieldName)object;
	}

	static
	public String getEntityId(Object object){

		if(object instanceof HasEntityId){
			HasEntityId hasEntityId = (HasEntityId)object;

			return hasEntityId.getEntityId();
		}

		return null;
	}
}