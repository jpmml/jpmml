/*
 * Copyright (c) 2013 University of Tartu
 */
package org.jpmml.evaluator;

import java.util.*;

import org.dmg.pmml.*;

import com.google.common.collect.*;

abstract
public class EvaluationContext {

	private Deque<Map<FieldName, FieldValue>> stack = Queues.newArrayDeque();

	private List<String> warnings = Lists.newArrayList();


	abstract
	public DerivedField resolveField(FieldName name);

	abstract
	public DefineFunction resolveFunction(String name);

	/**
	 * @see #getArgumentEntry(FieldName)
	 */
	public FieldValue getArgument(FieldName name){
		Map.Entry<FieldName, FieldValue> entry = getArgumentEntry(name);
		if(entry != null){
			return entry.getValue();
		}

		return null;
	}

	public Map.Entry<FieldName, FieldValue> getArgumentEntry(FieldName name){
		Deque<Map<FieldName, FieldValue>> stack = getStack();

		// Iterate from first to last
		Iterator<Map<FieldName, FieldValue>> it = stack.iterator();
		while(it.hasNext()){
			Map<FieldName, FieldValue> frame = it.next();

			if(frame.containsKey(name)){
				Map.Entry<FieldName, FieldValue> entry = new AbstractMap.SimpleEntry<FieldName, FieldValue>(name, frame.get(name));

				return entry;
			}
		}

		return null;
	}

	public FieldValue createFieldValue(FieldName name, Object value){
		return FieldValueUtil.create(value);
	}

	public Map<FieldName, FieldValue> pushFrame(Map<FieldName, ?> arguments){
		Maps.EntryTransformer<FieldName, Object, FieldValue> transformer = new Maps.EntryTransformer<FieldName, Object, FieldValue>(){

			@Override
			public FieldValue transformEntry(FieldName name, Object value){

				if(value instanceof FieldValue){
					return (FieldValue)value;
				}

				return createFieldValue(name, value);
			}
		};

		Map<FieldName, FieldValue> frame = Maps.newLinkedHashMap();

		frame.putAll(Maps.transformEntries(arguments, transformer));

		getStack().push(frame);

		return frame;
	}

	public Map<FieldName, FieldValue> popFrame(){
		return getStack().pop();
	}

	public void addWarning(String warning){
		List<String> warnings = getWarnings();

		warnings.add(warning);
	}

	Deque<Map<FieldName, FieldValue>> getStack(){
		return this.stack;
	}

	public List<String> getWarnings(){
		return this.warnings;
	}
}