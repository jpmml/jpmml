/*
 * Copyright (c) 2013 Villu Ruusmann
 */
package org.jpmml.evaluator;

import java.util.*;

import org.dmg.pmml.*;

import com.google.common.collect.*;

abstract
public class EvaluationContext {

	private Map<FieldName, FieldValue> fields = Maps.newLinkedHashMap();

	private List<String> warnings = Lists.newArrayList();


	abstract
	public DerivedField resolveDerivedField(FieldName name);

	abstract
	public DefineFunction resolveFunction(String name);

	/**
	 * @see #getFieldEntry(FieldName)
	 */
	public FieldValue getField(FieldName name){
		Map.Entry<FieldName, FieldValue> entry = getFieldEntry(name);
		if(entry != null){
			return entry.getValue();
		}

		return null;
	}

	public Map.Entry<FieldName, FieldValue> getFieldEntry(FieldName name){
		Map<FieldName, FieldValue> fields = getFields();

		if(fields.containsKey(name)){
			Map.Entry<FieldName, FieldValue> entry = new AbstractMap.SimpleEntry<FieldName, FieldValue>(name, fields.get(name));

			return entry;
		}

		return null;
	}

	public boolean declare(FieldName name, Object value){

		if(value instanceof FieldValue){
			return declare(name, (FieldValue)value);
		}

		return declare(name, createFieldValue(name, value));
	}

	/**
	 * @return <code>true</code> If the field was already declared, <code>false</code> otherwise.
	 */
	public boolean declare(FieldName name, FieldValue value){
		Map<FieldName, FieldValue> fields = getFields();

		boolean duplicate = fields.containsKey(name);

		fields.put(name, value);

		return duplicate;
	}

	public boolean declareAll(Map<FieldName, ?> fields){
		boolean result = false;

		Collection<? extends Map.Entry<FieldName, ?>> entries = fields.entrySet();
		for(Map.Entry<FieldName, ?> entry : entries){
			result |= declare(entry.getKey(), entry.getValue());
		}

		return result;
	}

	public FieldValue createFieldValue(FieldName name, Object value){
		return FieldValueUtil.create(value);
	}

	public void addWarning(String warning){
		List<String> warnings = getWarnings();

		warnings.add(warning);
	}

	public Map<FieldName, FieldValue> getFields(){
		return this.fields;
	}

	public List<String> getWarnings(){
		return this.warnings;
	}
}