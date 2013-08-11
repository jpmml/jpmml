/*
 * Copyright (c) 2013 University of Tartu
 */
package org.jpmml.evaluator;

import java.util.*;

import org.dmg.pmml.*;

import com.google.common.collect.*;

abstract
public class EvaluationContext {

	private Deque<Map<FieldName, ?>> stack = Queues.newArrayDeque();

	private List<String> warnings = Lists.newArrayList();


	public EvaluationContext(){
	}

	public EvaluationContext(Map<FieldName, ?> arguments){
		pushFrame(arguments);
	}

	abstract
	public DerivedField resolveField(FieldName name);

	abstract
	public DefineFunction resolveFunction(String name);

	public Map<FieldName, ?> getArguments(){
		Map<FieldName, Object> result = Maps.newLinkedHashMap();

		Deque<Map<FieldName, ?>> stack = getStack();

		// Iterate from last (ie. oldest) to first (ie. newest)
		Iterator<Map<FieldName, ?>> it = stack.descendingIterator();
		while(it.hasNext()){
			Map<FieldName, ?> frame = it.next();

			result.putAll(frame);
		}

		return result;
	}

	/**
	 * @see #getArgumentEntry(FieldName)
	 */
	public Object getArgument(FieldName name){
		Map.Entry<FieldName, Object> entry = getArgumentEntry(name);
		if(entry != null){
			return entry.getValue();
		}

		return null;
	}

	public Map.Entry<FieldName, Object> getArgumentEntry(FieldName name){
		Deque<Map<FieldName, ?>> stack = getStack();

		// Iterate from first to last
		Iterator<Map<FieldName, ?>> it = stack.iterator();
		while(it.hasNext()){
			Map<FieldName, ?> frame = it.next();

			if(frame.containsKey(name)){
				Map.Entry<FieldName, Object> entry = new AbstractMap.SimpleEntry<FieldName, Object>(name, frame.get(name));

				return entry;
			}
		}

		return null;
	}

	public Map<FieldName, ?> popFrame(){
		return getStack().pop();
	}

	public void pushFrame(Map<FieldName, ?> frame){
		getStack().push(frame);
	}

	public void addWarning(String warning){
		List<String> warnings = getWarnings();

		warnings.add(warning);
	}

	Deque<Map<FieldName, ?>> getStack(){
		return this.stack;
	}

	public List<String> getWarnings(){
		return this.warnings;
	}
}