/*
 * Copyright, KNIME.com AG, Zurich, Switzerland
 */
package org.jpmml.evaluator;

import java.lang.reflect.Field;

import org.dmg.pmml.*;

public class PMMLObjectUtil {

	private PMMLObjectUtil(){
	}

	@SuppressWarnings (
		value = {"unchecked"}
	)
	static
	public <E> E getField(PMMLObject object, String name){
		Class<? extends PMMLObject> clazz = object.getClass();

		try {
			Field field = clazz.getDeclaredField(name);
			if(!field.isAccessible()){
				field.setAccessible(true);
			}

			return (E)field.get(object);
		} catch(Exception e){
			throw new EvaluationException();
		}
	}
}