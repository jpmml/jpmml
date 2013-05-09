/*
 * Copyright (c) 2013 University of Tartu
 */
package org.jpmml.manager;

import java.util.*;

import org.dmg.pmml.*;

public class FieldUtil {

	private FieldUtil(){
	}

	static
	public <F extends Field> F getField(Collection<F> fields, FieldName name){

		for(F field : fields){

			if((field.getName()).equals(name)){
				return field;
			}
		}

		return null;
	}
}